#include <jni.h>
#include <string>
#include "FaceTracker.h"
#include "util.h"


extern "C"
JNIEXPORT jlong JNICALL
Java_com_xl_opencv_FaceTracker_nativeSetModel(JNIEnv *env, jobject thiz, jstring model_) {

    const char *model = env->GetStringUTFChars(model_, 0);
    FaceTracker *faceTracker = new FaceTracker(model);

    env->ReleaseStringUTFChars(model_, model);

    return (jlong)faceTracker;
}




extern "C"
JNIEXPORT void JNICALL
Java_com_xl_opencv_FaceTracker_nativeSetSurfaceView(JNIEnv *env, jobject thiz, jlong m_native_obj,
                                                    jobject surface_view) {

    if (m_native_obj != 0) {
        FaceTracker *faceTracker = reinterpret_cast<FaceTracker * >(m_native_obj);
        if (!surface_view) {
            faceTracker->setSurfaceVice(0);
            return;
        }
        faceTracker->setSurfaceVice(ANativeWindow_fromSurface(env, surface_view));
    }

}


extern "C"
JNIEXPORT void JNICALL
Java_com_xl_opencv_FaceTracker_nativeStart(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    if (m_native_obj != 0) {
        FaceTracker *faceTracker = reinterpret_cast<FaceTracker * >(m_native_obj);
        faceTracker->tracker->run();
    }


}


extern "C"
JNIEXPORT void JNICALL
Java_com_xl_opencv_FaceTracker_nativeStop(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    if (m_native_obj != 0) {
        FaceTracker *faceTracker = reinterpret_cast<FaceTracker * >(m_native_obj);
        faceTracker->tracker->stop();
    }

}



extern "C"
JNIEXPORT void JNICALL
Java_com_xl_opencv_FaceTracker_nativeRelease(JNIEnv *env, jobject thiz, jlong m_native_obj) {
    // TODO: implement nativeRelease()

    if (m_native_obj != 0) {
        FaceTracker *faceTracker = reinterpret_cast<FaceTracker * >(m_native_obj);
        faceTracker->tracker->stop();
        delete faceTracker;
    }
}



extern "C"
JNIEXPORT void JNICALL
Java_com_xl_opencv_FaceTracker_nativeDetect(JNIEnv *env, jobject thiz, jlong m_native_obj,
                                            jbyteArray inputImage_, jint width, jint height,
                                            jint rotationDegrees) {
    if (m_native_obj == 0) {
        return;
    }

    FaceTracker *tracker = reinterpret_cast<FaceTracker *>(m_native_obj);
    jbyte *inputImage = env->GetByteArrayElements(inputImage_, 0);

    //I420
    Mat src(height * 3 / 2, width, CV_8UC1, inputImage);

    // 转为RGBA
    cvtColor(src, src, CV_YUV2RGBA_I420);
    //旋转
    if (rotationDegrees == 90) {
        rotate(src, src, ROTATE_90_CLOCKWISE);
    } else if (rotationDegrees == 270) {
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
    }
    //镜像问题，可以使用此方法进行垂直翻转
//    flip(src,src,1);
    Mat gray;
    cvtColor(src, gray, CV_RGBA2GRAY);
    equalizeHist(gray, gray);

    tracker->tracker->process(gray);
    std::vector<Rect> faces;
    tracker->tracker->getObjects(faces);

    for (Rect face:faces) {
        //画矩形
        rectangle(src, face, Scalar(255, 0, 255));
    }
    tracker->draw(src);


    env->ReleaseByteArrayElements(inputImage_, inputImage, 0);

}