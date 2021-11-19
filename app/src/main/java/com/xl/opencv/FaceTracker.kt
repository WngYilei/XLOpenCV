package com.xl.opencv

import android.util.Log
import android.view.Surface

class FaceTracker constructor(model: String) {

    private var mNativeObj: Long = 0

    init {
        mNativeObj = nativeSetModel(model)
    }


    fun setSurfaceView(surfaceView: Surface?) {
        nativeSetSurfaceView(mNativeObj, surfaceView)
    }


    fun detect(inputImage: ByteArray, width: Int, height: Int, rotationDegrees: Int) {
        nativeDetect(mNativeObj, inputImage, width, height, rotationDegrees)
    }


    fun start() {
        nativeStart(mNativeObj)
    }


    fun stop() {
        nativeStop(mNativeObj)
    }


    fun release() {
        nativeRelease(mNativeObj)
        mNativeObj = 0
    }


    private external fun nativeSetSurfaceView(mNativeObj: Long, surfaceView: Surface?)

    private external fun nativeSetModel(model: String): Long

    private external fun nativeRelease(mNativeObj: Long)

    private external fun nativeStop(mNativeObj: Long)

    private external fun nativeStart(mNativeObj: Long)

    private external fun nativeDetect(
        mNativeObj: Long,
        inputImage: ByteArray,
        width: Int,
        height: Int,
        rotationDegrees: Int
    )
}