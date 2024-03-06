package com.google.mediapipe.examples.poselandmarker.fragment

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.examples.poselandmarker.FrameProcessor
import com.google.mediapipe.examples.poselandmarker.R
import com.google.mediapipe.examples.poselandmarker.databinding.FragmentCameraBinding
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.OutputHandler
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.Executors

class CameraFragment : Fragment() {
    private lateinit var frameProcessor: FrameProcessor
    private var _fragmentCameraBinding: FragmentCameraBinding? = null
    private val fragmentCameraBinding
        get() = _fragmentCameraBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 42)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentCameraBinding =
            FragmentCameraBinding.inflate(inflater, container, false)
        return fragmentCameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraView = view.findViewById<PreviewView>(R.id.previewView)
        val outputTextView = view.findViewById<TextView>(R.id.feedbackTextView)
//        frameProcessor = FrameProcessor()
//
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener(
//            {
//                setUpCameraAndMediaPipe(cameraView, cameraProviderFuture)
//            },
//            ContextCompat.getMainExecutor(requireContext())
//        )
//
//        frameProcessor.addFeedbackListener { output ->
//            outputTextView.post {
//                outputTextView.text = output.message
//                println("Something: ${output.message}")
//            }
//        }
        fragmentCameraBinding.stopButton.setOnClickListener {
            findNavController().navigate(R.id.action_cameraFragment_to_summaryFragment)
        }
    }

    private fun setUpCameraAndMediaPipe(
        cameraView: PreviewView,
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    ) {
        val imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(cameraView.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
        setUpCamera(cameraProviderFuture, cameraView, imageAnalyzer)
        val poseLandmarker = getPoseLandmarker(requireContext())
        imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { image -> detectLiveStream(image, poseLandmarker)
        }
    }

    private fun getPoseLandmarker(context: Context): PoseLandmarker {
        val baseOptionsBuilder =
            BaseOptions.builder().setModelAssetPath("pose_landmarker_full.task")
        val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setMinPoseDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setNumPoses(1)
            .setResultListener(object :
                OutputHandler.ResultListener<PoseLandmarkerResult?, MPImage?> {
                override fun run(result: PoseLandmarkerResult?, input: MPImage?) {
                    if (result == null || input == null) return
                    frameProcessor.process(result.timestampMs(), result.landmarks())
                }
            })
            .setErrorListener { e -> println("PoseLandmarkerResult: $e") }
            .setRunningMode(RunningMode.LIVE_STREAM)
        val options = optionsBuilder.build()
        return PoseLandmarker.createFromOptions(context, options)
    }

    private fun setUpCamera(
        cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
        cameraView: PreviewView,
        imageAnalyzer: UseCase
    ) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(cameraView.display.rotation)
            .build()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(this as LifecycleOwner, CameraSelector.Builder().build(), preview, imageAnalyzer)
        preview.setSurfaceProvider(cameraView.surfaceProvider)
    }

    private fun detectLiveStream(
        imageProxy: ImageProxy,
        poseLandmarker: PoseLandmarker,
    ) {
        val frameTimeMillis = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            Bitmap.Config.ARGB_8888
        )
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height,
            matrix, true
        )
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()
        poseLandmarker.detectAsync(mpImage, frameTimeMillis)
    }
}