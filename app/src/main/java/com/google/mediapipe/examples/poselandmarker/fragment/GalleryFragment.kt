package com.google.mediapipe.examples.poselandmarker.fragment

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.poselandmarker.FrameProcessor
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentGalleryBinding
import com.google.mediapipe.examples.poselandmarker.models.LandmarkPosition
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
    private lateinit var timestampCounterView: TextView
    private lateinit var poseLandmarker: PoseLandmarker
    private var videoProcessingJob: Job? = null
    private var strokeCountInt = 0
    private var mostCommonFeedback = "No feedback provided"
    private val fragmentGalleryBinding
        get() = _fragmentGalleryBinding!!

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            requireView().findViewById<View>(R.id.videoInfo).visibility = View.VISIBLE
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

    private val landmarkPairs = listOf(
        Pair(LandmarkPosition.LEFT_SHOULDER, LandmarkPosition.RIGHT_SHOULDER),
        Pair(LandmarkPosition.LEFT_SHOULDER, LandmarkPosition.LEFT_HIP),
        Pair(LandmarkPosition.RIGHT_SHOULDER, LandmarkPosition.RIGHT_HIP),
        Pair(LandmarkPosition.LEFT_SHOULDER, LandmarkPosition.LEFT_ELBOW),
        Pair(LandmarkPosition.RIGHT_SHOULDER, LandmarkPosition.RIGHT_ELBOW),
        Pair(LandmarkPosition.LEFT_ELBOW, LandmarkPosition.LEFT_WRIST),
        Pair(LandmarkPosition.RIGHT_ELBOW, LandmarkPosition.RIGHT_WRIST),
        Pair(LandmarkPosition.LEFT_HIP, LandmarkPosition.RIGHT_HIP),
        Pair(LandmarkPosition.LEFT_HIP, LandmarkPosition.LEFT_KNEE),
        Pair(LandmarkPosition.RIGHT_HIP, LandmarkPosition.RIGHT_KNEE),
        Pair(LandmarkPosition.LEFT_KNEE, LandmarkPosition.LEFT_ANKLE),
        Pair(LandmarkPosition.RIGHT_KNEE, LandmarkPosition.RIGHT_ANKLE),
        Pair(LandmarkPosition.LEFT_ANKLE, LandmarkPosition.LEFT_HEEL),
        Pair(LandmarkPosition.RIGHT_ANKLE, LandmarkPosition.RIGHT_HEEL),
        Pair(LandmarkPosition.LEFT_HEEL, LandmarkPosition.LEFT_FOOT_INDEX),
        Pair(LandmarkPosition.RIGHT_HEEL, LandmarkPosition.RIGHT_FOOT_INDEX),
        Pair(LandmarkPosition.LEFT_ANKLE, LandmarkPosition.LEFT_FOOT_INDEX),
        Pair(LandmarkPosition.RIGHT_ANKLE, LandmarkPosition.RIGHT_FOOT_INDEX)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding =
            FragmentGalleryBinding.inflate(inflater, container, false)
        return fragmentGalleryBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor =
            requireContext().getColor(R.color.status_bar_white)
        // Next, set the status bar icons to be dark for better contrast:
        requireActivity().window.insetsController?.setSystemBarsAppearance(
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )
        frameProcessor = FrameProcessor()
        feedbackTextView = view.findViewById(R.id.feedbackTextView)
        repCounterView = view.findViewById(R.id.strokeCountTextView)
        timestampCounterView = view.findViewById(R.id.timestampTextView)

        fragmentGalleryBinding.fabGetContent.setOnClickListener {
            getContent.launch(arrayOf("image/*", "video/*"))
        }
        loadModelInBackground()
        fragmentGalleryBinding.stopButton.setOnClickListener {
            videoProcessingJob?.cancel()
            val bundle = Bundle()
            bundle.putInt("strokeCount", strokeCountInt)
            bundle.putInt("correctStrokePercentage", frameProcessor.getCorrectStrokePercentage())
            bundle.putString("mostCommonFeedback", mostCommonFeedback)
            if (isAdded) {
                findNavController().navigate(R.id.action_galleryFragment_to_summaryFragment, bundle)
            }
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
        updateDisplayView(MediaType.VIDEO)
        //parallel processing
        videoProcessingJob = MainScope().launch {
            detectVideoFile(uri)
        }
    }

    // Accepts the URI for a video file loaded from the user's gallery and attempts to run
    // pose landmarker inference on the video. This process will evaluate every
    // frame in the video and attach the results to a bundle that will be returned.
    private suspend fun detectVideoFile(videoUri: Uri) {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val videoLengthMs =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLong()
        val firstFrame = retriever.getFrameAtTime(0)
        val width = firstFrame?.width
        val height = firstFrame?.height
        if ((videoLengthMs == null) || (width == null) || (height == null)) {
            Log.e("VideoProcessing", "Failed to retrieve video metadata")
            return
        }
        // Get the actual number of frames in the video
        val actualNumberOfFrames =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
                ?.toLong()
        // Set visibility and max value of progress bar
        fragmentGalleryBinding.progressBar.visibility = View.VISIBLE
        if (actualNumberOfFrames != null) {
            // update VIDEO_INTERVAL_MS
            updateVideoIntervalMs(videoLengthMs, actualNumberOfFrames)
            fragmentGalleryBinding.progressBar.max = actualNumberOfFrames.toInt()
        }

        (0 until actualNumberOfFrames!!)
            .forEach {
                processFrame(it, retriever)
                // Update the progress bar every 10 frames
                if (it % 10 == 0.toLong()) {
                    fragmentGalleryBinding.progressBar.progress = it.toInt()
                }
            }

        val bundle = Bundle()
        bundle.putInt("strokeCount", strokeCountInt)
        bundle.putInt("correctStrokePercentage", frameProcessor.getCorrectStrokePercentage())
        bundle.putString("mostCommonFeedback", mostCommonFeedback)
        fragmentGalleryBinding.progressBar.visibility = View.INVISIBLE
        if (isAdded) {
            findNavController().navigate(R.id.action_galleryFragment_to_summaryFragment, bundle)
        }
        retriever.release()
    }

    private suspend fun processFrame(it: Long, retriever: MediaMetadataRetriever) =
        withContext(Dispatchers.Default) {
            val timestampMs = it * VIDEO_INTERVAL_MS
            try {
                activity?.runOnUiThread {
                    if (isAdded) {
                        timestampCounterView.text =
                            getString(R.string.label_timestamp, timestampMs / 1000)
                    }
                }
                val bitmapFrame: Bitmap =
                    retriever.getFrameAtIndex(it.toInt()) ?: return@withContext
                val resizedBitmap = resizeBitmap(
                    bitmapFrame,
                    fragmentGalleryBinding.textureView.width,
                    bitmapFrame.height * fragmentGalleryBinding.textureView.width / bitmapFrame.width
                )

                val mpImage = BitmapImageBuilder(resizedBitmap).build()
                poseLandmarker.detectForVideo(mpImage, timestampMs)
                    ?.let { detectionResult: PoseLandmarkerResult ->
                        frameProcessor.process(timestampMs, detectionResult.landmarks())
                        // Convert PoseLandmarkerResult landmarks to a list of PointF
                        val points = getLandmarkPoints(detectionResult)
                        val connections = landmarkPairs.mapNotNull { (start, end) ->
                            val startPoint = points.getOrNull(start.ordinal)
                            val endPoint = points.getOrNull(end.ordinal)
                            if (startPoint != null && endPoint != null) {
                                Pair(startPoint, endPoint)
                            } else {
                                null
                            }
                        }
                        // Create a bitmap with the pose drawn on it
                        val poseBitmap = drawPoseOnBitmap(resizedBitmap, points, connections)
                        // Update the UI with the new bitmap
                        activity?.runOnUiThread {
                            val canvas = fragmentGalleryBinding.textureView.lockCanvas()
                            if (canvas != null) {
                                canvas.drawBitmap(poseBitmap, 0f, 0f, null)
                                fragmentGalleryBinding.textureView.unlockCanvasAndPost(canvas)
                            } else {
                                Log.e("GalleryFragment", "Canvas is null or has unexpected size.")
                            }
                            // Update timestamp text view
                            timestampCounterView.text =
                                getString(R.string.label_timestamp, timestampMs / 1000)
                        }
                    } ?: run {
                    Log.e("VideoProcessing", "Failed to process frame $it")
                }
            } catch (e: IllegalStateException) {
                Log.e("VideoProcessing", "Failed to retrieve frame at index: $it", e)
            }
        }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, false)
    }

    private fun getLandmarkPoints(detectionResult: PoseLandmarkerResult): List<PointF> {
        // Assuming only interested in the first pose detected.
        val landmarks = detectionResult.landmarks().firstOrNull() ?: return emptyList()
        // Convert the NormalizedLandmark list into PointF list
        return landmarks.map { landmark ->
            PointF(landmark.x(), landmark.y()) // x and y are normalized coordinates
        }
    }

    private fun drawPoseOnBitmap(
        bitmap: Bitmap,
        points: List<PointF>,
        connections: List<Pair<PointF, PointF>>
    ): Bitmap {
        val offscreenBitmap =
            Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val offscreenCanvas = Canvas(offscreenBitmap)
        offscreenCanvas.drawBitmap(bitmap, 0f, 0f, null)

        val pointPaint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
            strokeWidth = 10f
        }
        val linePaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        // Draw lines between points
        connections.forEach { (start, end) ->
            val scaledStartX = start.x * bitmap.width
            val scaledStartY = start.y * bitmap.height
            val scaledEndX = end.x * bitmap.width
            val scaledEndY = end.y * bitmap.height
            offscreenCanvas.drawLine(scaledStartX, scaledStartY, scaledEndX, scaledEndY, linePaint)
        }
        // Draw points
        points.forEach { point ->
            val scaledX = point.x * bitmap.width
            val scaledY = point.y * bitmap.height
            offscreenCanvas.drawCircle(scaledX, scaledY, 10f, pointPaint)
        }
        return offscreenBitmap
    }

    private fun updateDisplayView(mediaType: MediaType) {
        fragmentGalleryBinding.textureView.visibility =
            if (mediaType == MediaType.VIDEO) View.VISIBLE else View.GONE
        fragmentGalleryBinding.tvPlaceholder.visibility =
            if (mediaType == MediaType.UNKNOWN) View.VISIBLE else View.GONE
    }

    private fun loadMediaType(uri: Uri): MediaType {
        val mimeType = context?.contentResolver?.getType(uri)
        mimeType?.let {
            if (mimeType.startsWith("video")) return MediaType.VIDEO
        }
        return MediaType.UNKNOWN
    }

    companion object {
        fun newInstance() = GalleryFragment()
        private var VIDEO_INTERVAL_MS = 1000 / 33L
        fun updateVideoIntervalMs(videoLengthMs: Long, actualNumberOfFrames: Long) {
            VIDEO_INTERVAL_MS = videoLengthMs / actualNumberOfFrames
        }

    }
}