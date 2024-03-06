package com.google.mediapipe.examples.poselandmarker.fragment

import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.poselandmarker.FrameProcessor
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentGalleryBinding
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.ScheduledExecutorService
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 * Use the [GalleryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GalleryFragment : Fragment() {
    enum class MediaType {
        VIDEO,
        UNKNOWN
    }

    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null
    private lateinit var frameProcessor: FrameProcessor
    private lateinit var feedbackTextView: TextView
    private lateinit var repCounterView: TextView
    private lateinit var fpsCounterView: TextView
    private lateinit var poseLandmarker: PoseLandmarker
    private var strokeCountInt = 0
    private var mostCommonFeedback = "No feedback provided"
    private lateinit var mediaPlayer: MediaPlayer  // Initialize MediaPlayer here
    private val fragmentGalleryBinding
        get() = _fragmentGalleryBinding!!

    /** Blocking ML operations are performed using this executor */
    private lateinit var backgroundExecutor: ScheduledExecutorService
    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            // Handle the returned Uri
            uri?.let { mediaUri ->
                when (val mediaType = loadMediaType(mediaUri)) {
                    MediaType.VIDEO -> runDetectionOnVideo(mediaUri)
                    MediaType.UNKNOWN -> {
                        updateDisplayView(mediaType)
                        Toast.makeText(
                            requireContext(),
                            "Unsupported data type.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding =
            FragmentGalleryBinding.inflate(inflater, container, false)
        return fragmentGalleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frameProcessor = FrameProcessor()
        feedbackTextView = view.findViewById(R.id.feedbackTextView)
        repCounterView = view.findViewById(R.id.repsCountTextView)
        fpsCounterView = view.findViewById(R.id.ipsTextView)
        fragmentGalleryBinding.fabGetContent.setOnClickListener {
            getContent.launch(arrayOf("image/*", "video/*"))
        }
        loadModelInBackground()
        fragmentGalleryBinding.stopButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt("strokeCount", strokeCountInt)
            bundle.putInt("correctStrokePercentage", frameProcessor.getCorrectStrokePercentage())
            bundle.putString("mostCommonFeedback", mostCommonFeedback)
            findNavController().navigate(R.id.action_galleryFragment_to_summaryFragment, bundle)
        }
        frameProcessor.addFeedbackListener { output ->
            activity?.runOnUiThread {
                val feedbackMessage = output.message
                val formattedMessage = getString(R.string.feedback_message, feedbackMessage)
                feedbackTextView.text = formattedMessage
            }
        }
        frameProcessor.addStrokeCountListener { output ->
            activity?.runOnUiThread {
                val strokeCount = output.message
                strokeCountInt = strokeCount.trim().toInt()
                val counter = getString(R.string.count, strokeCount)
                repCounterView.text = counter
            }
        }
        frameProcessor.addMostCommonFeedbackListener { feedback ->
            activity?.runOnUiThread {
                mostCommonFeedback = feedback
            }
        }
        // Instantiate MediaPlayer here
        mediaPlayer = MediaPlayer()
        mediaPlayer.isLooping = false
    }

    private fun loadModelInBackground() {
        // Start a new thread to load the model
        Thread {
            val baseOptionsBuilder =
                BaseOptions.builder().setModelAssetPath("pose_landmarker_lite.task")
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinPoseDetectionConfidence(0.4f)
                .setMinTrackingConfidence(0.4f)
                .setMinPosePresenceConfidence(0.4f)
                .setNumPoses(1)
                .setRunningMode(RunningMode.VIDEO)

            val options = optionsBuilder.build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        }.start()
    }

    private fun runDetectionOnVideo(uri: Uri) {
        // Play video on a texture view
        var startTimeMillis: Long
        mediaPlayer.setDataSource(requireContext(), uri)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            val surface = Surface(fragmentGalleryBinding.textureView.surfaceTexture)
            mediaPlayer.setSurface(surface)
            mediaPlayer.start()
            startTimeMillis = System.currentTimeMillis() // Record the start time
            // Add a flag to indicate whether the video is playing
            var isVideoPlaying = true
            // 2 different timestamps variable used to prevent error received frame with timestamp smaller
            // than timestamp in processed video frame from MediaPipe
            var timestamp: Long
            var frameNumber = 0L
            // Set a listener to update the flag when the video is completed
            mediaPlayer.setOnCompletionListener {
                isVideoPlaying = false
            }
            // Start a new thread to run pose landmarker on the video
            // This will take read the current frame from the video and run pose landmarker on it,
            // in an endless loop.
            thread {
                var previousBitmap: Bitmap? = null
                while (isVideoPlaying && ::poseLandmarker.isInitialized) {
                    // val frame = fragmentGalleryBinding.textureView.bitmap ?: continue
                    val frame = fragmentGalleryBinding.textureView.bitmap
                    if (frame != null) {
                        // Convert the video frame to ARGB_8888 which is required by the MediaPipe
                        val argb8888Frame =
                            if (frame.config == Bitmap.Config.ARGB_8888) frame
                            else frame.copy(Bitmap.Config.ARGB_8888, false)
                        previousBitmap?.recycle()
                        previousBitmap = argb8888Frame
                        // Convert the input Bitmap object to an MPImage object to run inference
                        val mpImage = BitmapImageBuilder(argb8888Frame).build()
                        frameNumber++
                        timestamp = if (isVideoPlaying) mediaPlayer.currentPosition.toLong() else 0L
                        val elapsedTimeSeconds =
                            (System.currentTimeMillis() - startTimeMillis) / 1000.0
                        val fps = frameNumber / elapsedTimeSeconds
                        activity?.runOnUiThread {
                            fpsCounterView.text = String.format("FPS: %d", fps.toInt())
                        }
                        // Run pose landmarker using MediaPipe Pose Landmarker API with frameNumber as timestamp
                        poseLandmarker.detectForVideo(mpImage, frameNumber)
                            ?.let { detectionResult: PoseLandmarkerResult ->
                                frameProcessor.process(
                                    timestamp,// real timestamp from video frame
                                    detectionResult.landmarks()
                                )
                            } ?: {
                            error("ResultBundle could not be returned" + " in detectVideoFile")
                        }
                    } else {
                        // Log a message or perform some other action
                        Log.d("VideoProcessing", "No new frame to process")
                        break
                    }
                }
                previousBitmap?.recycle()
                if (::poseLandmarker.isInitialized) {
                    poseLandmarker.close()
                }
                mediaPlayer.release()
            }
        }
    }

    private fun updateDisplayView(mediaType: MediaType) {
        fragmentGalleryBinding.textureView.visibility =
            if (mediaType == MediaType.VIDEO) View.VISIBLE else View.GONE
        fragmentGalleryBinding.tvPlaceholder.visibility =
            if (mediaType == MediaType.UNKNOWN) View.VISIBLE else View.GONE
    }

    // Check the type of media that user selected.
    private fun loadMediaType(uri: Uri): MediaType {
        val mimeType = context?.contentResolver?.getType(uri)
        mimeType?.let {
            if (mimeType.startsWith("video")) return MediaType.VIDEO
        }
        return MediaType.UNKNOWN
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release the MediaPlayer when it's no longer needed
        mediaPlayer.release()
    }

    companion object {
        fun newInstance() = GalleryFragment()
    }
}
