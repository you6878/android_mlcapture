package com.howlab.howlmlcapture

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var imageCapture: ImageCapture? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 0)
        bindCameraUseCases()
        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    //This is labelImages functions
    fun labelImages(bitmap : Bitmap){

        val options = FirebaseVisionOnDeviceImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.8f)
            .build()
        val detector = FirebaseVision.getInstance().getOnDeviceImageLabeler(options)

        detector.processImage(FirebaseVisionImage.fromBitmap(bitmap))
            .addOnSuccessListener { labels ->
                for(label in labels){
                    val text = label.text
                    val confidence = (label.confidence * 100).toInt()
                    Toast.makeText(this,"It's a $confidence% $text",Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this,exception.message,Toast.LENGTH_LONG).show()
            }
    }
    //This is textRecognize function
    fun textRecognize(bitmap: Bitmap){
        FirebaseVision.getInstance().onDeviceTextRecognizer.processImage(FirebaseVisionImage.fromBitmap(bitmap))
                .addOnSuccessListener { firebaseVisionText ->
                    for(block in firebaseVisionText.textBlocks){
                        println(block.text)
                        for(line in block.lines){
                            println(line.text)
                            for(element in line.elements){
                                println(line.text)
                            }
                        }
                    }
                }
    }

    fun takePhoto() {
        imageCapture?.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = imageProxyToBitmap(image)
                //textRecognize(bitmap)
                labelImages(bitmap)
                super.onCaptureSuccess(image)
            }
        })
    }

    fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {

        val buffer = imageProxy.planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        //Rotate bitmap
        val matrix = Matrix()
        matrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

    }

    fun bindCameraUseCases() {
        val rotation = 0
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                    .setTargetRotation(rotation)
                    .build()

            imageCapture = ImageCapture.Builder()
                    .setTargetResolution(Size(960, 1280))
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .setTargetRotation(rotation)
                    .build()


            cameraProvider.unbindAll()

            val camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            preview.setSurfaceProvider(viewFinder.createSurfaceProvider(camera.cameraInfo))
        }, ContextCompat.getMainExecutor(this))
    }
}
