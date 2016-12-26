package com.jh.numberplatedetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import org.opencv.android.JavaCameraView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.List;

/**
 * Created by Johannes on 23.12.2016.
 */

public class RotatedCameraView extends JavaCameraView {

    public RotatedCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }


    public RotatedCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public List<Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }


    public void setResolution(int width, int height) {
        disconnectCamera();
        mMaxHeight = height;
        mMaxWidth = width;
        connectCamera(getWidth(), getHeight());
    }


    public Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }
}
