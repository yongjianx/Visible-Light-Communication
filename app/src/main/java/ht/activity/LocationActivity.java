package ht.activity;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.example.skyworthclub.visible_light_communication.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class LocationActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "LocationActivity";
//    static {
//        OpenCVLoader.initDebug();
//    }

    private CameraBridgeViewBase mCameraView;
    private Mat mRgba;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_location);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        //获得灰度图
        Mat gray = inputFrame.gray();
        //图像翻转
        Core.flip(gray, gray, 1);
        //目标图像
        Mat disMat = new Mat();
        //二值化
        Imgproc.threshold(gray, disMat, 120, 255, Imgproc.THRESH_TOZERO);
        Imgproc.threshold(gray, disMat, 120, 255, Imgproc.THRESH_BINARY);
        //形态学闭运算
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20));
        Imgproc.morphologyEx(disMat, disMat, Imgproc.MORPH_CLOSE, kernel);

        //腐蚀
        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(50, 50));
        Imgproc.erode(disMat, disMat, kernel);
        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(50, 50));
        Imgproc.erode(disMat, disMat, kernel);

        //膨胀
        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(50, 50));
        Imgproc.dilate(disMat, disMat, kernel);
        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(50, 50));
        Imgproc.dilate(disMat, disMat, kernel);

        Camera camera = ((CameraControlView)mCameraView).getCamera();
        Camera.Parameters parameters = camera.getParameters();
        Log.d(TAG, "焦距：" + parameters.getFocalLength());
        Log.d(TAG, "曝光时间:" + parameters.getExposureCompensation());

        return disMat;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.disableView();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ((CameraControlView)mCameraView).focusOnTouch(event);
        Log.d(TAG, "touch");
        return true;
    }
}
