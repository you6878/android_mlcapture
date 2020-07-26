package com.howlab.howlmlcapture

import android.content.Context
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class LuminosityAnalyzer(var context: Context) : ImageAnalysis.Analyzer {

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setWidth(image.width)
            .setHeight(image.height)
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setRotation(FirebaseVisionImageMetadata.ROTATION_270)
            .build()
        var bufferImage = FirebaseVisionImage.fromByteBuffer(buffer,metadata)
        scanBarcodes(bufferImage)
        image.close()

    }

    fun scanBarcodes(firebaseVisionImage: FirebaseVisionImage){
        val options = FirebaseVisionBarcodeDetectorOptions.Builder()
            .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE,FirebaseVisionBarcode.FORMAT_AZTEC)
            .build()
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options)

        detector.detectInImage(firebaseVisionImage).addOnSuccessListener { barcodes ->
            for(barcode in barcodes){
                var rawValue = barcode.rawValue
                var valueType = barcode.valueType
                var valueTypeString : String? = null
                when(valueType){
                    FirebaseVisionBarcode.TYPE_WIFI ->{
                        valueTypeString = "WIFI"
                    }
                    FirebaseVisionBarcode.TYPE_URL ->{
                        valueTypeString = "URL"
                    }
                }
                Toast.makeText(context,"valueType : $valueTypeString, rawValue : $rawValue",Toast.LENGTH_LONG).show()
            }
        }
    }

}