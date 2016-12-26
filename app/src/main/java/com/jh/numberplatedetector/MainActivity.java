package com.jh.numberplatedetector;

import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "MainActivity";

    private RotatedCameraView cameraView;

    // These variables are used (at the moment) to fix camera orientation from 270 degree to 0 degree
    Mat matGray;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    cameraView.enableView();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    public MainActivity() {
        super();
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        cameraView = (RotatedCameraView) findViewById(R.id.camera_view);
        cameraView.setVisibility(SurfaceView.VISIBLE);
        cameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (cameraView != null)
            cameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (cameraView != null)
            cameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        matGray = new Mat(height, width, CvType.CV_8UC1);

        List<Size> resolutions = cameraView.getResolutionList();
        cameraView.setResolution(800, 600);
    }


    @Override
    public void onCameraViewStopped() {
        matGray.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        matGray = inputFrame.gray();
        Mat matRgba = inputFrame.rgba();

//        byte[] zeros = new byte[300];
//        matGray.put(300, 498, zeros);

        Range rowRange = new Range();
        Range colRange = new Range();
//        NumberPlateExtractor.calculateCropOffset(matGray, rowRange, colRange);
//        Mat sub = matGray.submat(rowRange, colRange);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat binary = new Mat();
        Mat edges = new Mat();
        Rect rect = NumberPlateExtractor.extract(matGray, binary, edges, contours);

        Mat matContours = new Mat();
        Imgproc.cvtColor(edges, matContours, Imgproc.COLOR_GRAY2BGR);
//        for (int i = Math.min(contours.size(), 150); i < Math.min(contours.size(), 300); i++) {
//            Imgproc.drawContours(matContours, contours, i, new Scalar(100, 60, 200), 2);
//        }

        if (rect != null) {
            Scalar color = new Scalar(255, 60, 255, 255);
            rect.x += colRange.start;
            rect.y += rowRange.start;
            Imgproc.rectangle(matRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), color, 2);
        }

        return matRgba;
        //return matContours;
    }
}
