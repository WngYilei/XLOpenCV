package com.xl.opencv

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.SurfaceHolder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.xl.opencv.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer, SurfaceHolder.Callback {

    private lateinit var binding: ActivityMainBinding
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    lateinit var faceTracker: FaceTracker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //申请摄像头权限
        //申请摄像头权限
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            100
        )


        //
        val path = Utils.copyAsset2Dir(this, "lbpcascade_frontalface.xml")

        faceTracker = FaceTracker(path);
        faceTracker.start()

        binding.surfaceView.holder.addCallback(this)
        /**
         *  CameraX
         */
        /**
         * CameraX
         */
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                bindAnalysis(cameraProvider)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))


    }

    private fun bindAnalysis(cameraProvider: ProcessCameraProvider) {
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), this)
        cameraProvider.unbindAll()
        //绑定生命周期
        cameraProvider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis
        )

    }


    companion object {
        // Used to load the 'opencv' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    override fun analyze(image: ImageProxy) {
        val bytes = Utils.getDataFromImage(image)

        faceTracker.detect(bytes, image.width, image.height, image.imageInfo.rotationDegrees)

        image.close()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        faceTracker.setSurfaceView(holder.surface)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        faceTracker.setSurfaceView(null)
    }


    override fun onDestroy() {
        super.onDestroy()
        faceTracker.release()
    }
}