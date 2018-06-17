package com.example.skyworthclub.visible_light_communication.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.skyworthclub.visible_light_communication.ImageProcessing.ImageProcess;
import com.example.skyworthclub.visible_light_communication.R;
import com.example.skyworthclub.visible_light_communication.camera.CameraControlView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.example.skyworthclub.visible_light_communication.utils.Coordinate;
import com.example.skyworthclub.visible_light_communication.utils.DaemonThreadFactory;
import com.example.skyworthclub.visible_light_communication.utils.LedLine;

public class LocationActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private static final String TAG = "LocationActivity";
//    static {
//        OpenCVLoader.initDebug();
//    }

    private CameraBridgeViewBase mCameraView;
    private Mat mRgba;
    private Camera mCamera;
    private ImageView mImageView;
    private ImageView mDivideImg;


    int[][] XY = new int[3][2];
    int[][] xy = new int[3][2];
    private List<Integer> mLedLineList;

    //线程池
    ExecutorService service;
    FutureTask<Integer> futureTask;

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
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//
        mImageView = findViewById(R.id.image);
//        mDivideImg = findViewById(R.id.divide_image);
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setCvCameraViewListener(this);
        mCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);

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
//        mRgba = inputFrame.rgba();
//        //获得灰度图
//        Mat gray = inputFrame.gray();
//        //图像翻转
//        Core.flip(gray, gray, 1);
//        //目标图像
//        Mat disMat = new Mat();

//        Camera camera = ((CameraControlView)mCameraView).getCamera();
//        Camera.Parameters parameters = camera.getParameters();
//        Log.d(TAG, "焦距：" + parameters.getFocalLength());
//        Log.d(TAG, "曝光时间:" + parameters.getExposureCompensation());

//        isLed2(disMat);

        Mat resMat = inputFrame.rgba();
        Mat dst = new Mat();
        Mat rotateMat = Imgproc.getRotationMatrix2D(new Point(resMat.rows()/2,resMat.cols()/2), -90, 1);
        Imgproc.warpAffine(resMat, dst, rotateMat, dst.size());
        return dst;
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

        //创建线程池
//        service = Executors.newCachedThreadPool(new DaemonThreadFactory());
//        service.execute(new Runnable() {
//            @Override
//            public void run() {
//                handlePicture();
//            }
//        });

        handlePicture();

        mCamera = ((CameraControlView)mCameraView).getCamera();
    }

    private void handlePicture() {
        Mat resMat = new Mat();
        Mat disMat = new Mat();
        List<Mat> imgs = new ArrayList<Mat>();
        List<Coordinate> X = new ArrayList<Coordinate>();
        List<Coordinate> Y = new ArrayList<Coordinate>();
        List<Integer> S = new ArrayList<Integer>();

        //获得图片并转换成矩阵
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.led);
        Utils.bitmapToMat(bitmap, resMat);
        //图像预处理
        ImageProcess.preProcess(resMat, disMat);
        //分割led
        ImageProcess.divideLed(disMat, X, Y, S);
        //将分割后的led图像保存
        for (int i = 0; i < 3; i++) {
            Mat mat = resMat.submat(Y.get(i).getMin(), Y.get(i).getMax(), X.get(i).getMin(), X.get(i).getMax());
            imgs.add(mat);
        }
        Bitmap bitmap1 = Bitmap.createBitmap(imgs.get(0).cols(), imgs.get(0).rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgs.get(0), bitmap1);
        mImageView.setImageBitmap(bitmap1);
        //遍历分割后的led图像，检测每个led图像的条纹数
        mLedLineList = ImageProcess.getLedLineCount(imgs);

//        for (final Mat img:imgs){
//
//            Callable<Integer> callable = new Callable<Integer>() {
//                @Override
//                public Integer call() throws Exception {
//                    return ImageProcess.getLedLineCount(img);
//                }
//            };
//            futureTask = new FutureTask<Integer>(callable){
//                @Override
//                protected void done() {
//                    try{
//                        mLedLineList.add(futureTask.get());
//                    }catch (InterruptedException e){
//                        e.printStackTrace();
//                    }catch (ExecutionException e){
//                        e.printStackTrace();
//                    }
//
//                }
//            };
//
//            service.execute(futureTask);
//
//        }

        /*
        等待计算完led条纹数才能继续下面的步骤
         */
        //判断三个LED是否共线
        isCollinear(X, Y);
        //计算坐标
        getLocation();
    }

    private void isCollinear(List<Coordinate> X, List<Coordinate> Y){
        if ((X.get(0).getMiddle()==X.get(2).getMiddle())&&(Y.get(0).getMiddle()==Y.get(2).getMiddle())){
            X.remove(2);
            Y.remove(2);
        }else if((X.get(0).getMiddle()==X.get(1).getMiddle())&&(Y.get(0).getMiddle()==Y.get(1).getMiddle())){
            X.remove(1);
            Y.remove(1);
        }else if((X.get(1).getMiddle()==X.get(2).getMiddle())&&(Y.get(1).getMiddle()==Y.get(2).getMiddle())){
            X.remove(1);
            Y.remove(1);
        }

        for(int i = 0;i<3;i++){
            xy[i][0] = X.get(i).getMiddle()/3;
            xy[i][1] = Y.get(i).getMiddle()/3;
        }

        int lines[][] = {{6, 8, 11, 14, 17},{7, 9, 13, 16, 18}};
        int RealXY[][] = {{-330,330,0,330,-330},{-330,-330,0,330,330}};

        Log.d("htout", "length:" + lines.length);

        int Lines[] = new int[mLedLineList.size()];
        for (int i = 0; i < mLedLineList.size(); i++) {
            Lines[i] = mLedLineList.get(i);
            Log.d("htout", "lines" + i + ":" + Lines[i]);
        }

        for(int i = 0;i<3;i++){
            for(int j = 0; j < 5 ; j++){
                if(Lines[i]>=lines[0][j]&&Lines[i]<=lines[1][j]){
                    XY[i][0] = RealXY[0][j];
                    XY[i][1] = RealXY[1][j];
                }
            }
        }
    }

    private void getLocation(){
        int X[] = new int[3];
        int Y[] = new int[3];

        double f = 2.9;//摄像机焦距

        //透镜焦点在image sensor上的位置
        int center_x = 417;
        int center_y = 341; //透镜焦点在image sensor上的位置

        //图像中任意两个LED之间的距离
        double d_12 = Math.sqrt(Math.abs(Math.pow(xy[0][0]-xy[1][0],2) + Math.pow(xy[0][1]-xy[1][1],2)))*3.2e-3;
        double d_13 = Math.sqrt(Math.abs(Math.pow(xy[0][0]-xy[2][0],2) + Math.pow(xy[0][1]-xy[2][1],2)))*3.2e-3;
        double d_23 = Math.sqrt(Math.abs(Math.pow(xy[1][0]-xy[2][0],2) + Math.pow(xy[1][1]-xy[2][1],2)))*3.2e-3;

        //世界坐标中任意两个LED之间的距离
        double D_12 = Math.sqrt(Math.pow(XY[0][0]-XY[1][0],2) + Math.pow(XY[0][1]-XY[1][1],2));
        double D_13 = Math.sqrt(Math.pow(XY[0][0]-XY[2][0],2) + Math.pow(XY[0][1]-XY[2][1],2));
        double D_23 = Math.sqrt(Math.pow(XY[1][0]-XY[2][0],2) + Math.pow(XY[1][1]-XY[2][1],2));

        //相机与LED间的垂直距离
        double H = ( D_12/d_12 + D_13/d_13 + D_23/d_23 )/3*f;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 2; j++) {
                Log.d("htout", "xy:" + i + " " + j + " " + xy[i][j]);
            }
        }

        Log.d("htout", "aaa:" + " " + D_12 + " " + D_13 + " " + D_23 + " " + xy[0][0] + " " + xy[1][0] + " " + xy[0][1] + " " + xy[1][1]);

        //计算水平方向上摄像头到3个LED的距离
        double d_1 = Math.sqrt(Math.pow(xy[0][0]-center_x,2) + Math.pow(xy[0][1]-center_y,2))*3.2e-3;
        double d_2 = Math.sqrt(Math.pow(xy[1][0]-center_x,2) + Math.pow(xy[1][1]-center_y,2))*3.2e-3;
        double d_3 = Math.sqrt(Math.pow(xy[2][0]-center_x,2) + Math.pow(xy[2][1]-center_y,2))*3.2e-3;

        double D_1 = H/f*d_1;
        double D_2 = H/f*d_2;
        double D_3 = H/f*d_3;

        Log.d("htout", "bba:" + D_1 + " " + D_2 + " " + D_3 + " " + H);

        for(int i = 0 ; i < 3 ; i++){
            X[i] = XY[i][0];
            Y[i] = XY[i][1];
            Log.d("htout", "X:" + X[i] + "$" + Y[i]);
        }

        double r1 = Math.pow(D_1,2);
        double r2 = Math.pow(D_2,2);
        double r3 = Math.pow(D_3,2);
        double x1 = Math.pow(X[0],2);
        double x2 = Math.pow(X[1],2);
        double x3 = Math.pow(X[2],2);
        double y1 = Math.pow(Y[0],2);
        double y2 = Math.pow(Y[1],2);
        double y3 = Math.pow(Y[2],2);
        Log.d("htout", "ccc:" + r1 + " " + r2 + " " + r3);


        double a1 = 2*(X[0]-X[2]);
        double b1 = 2*(Y[0]-Y[2]);
        double c1 = x3 - x1 + y3 - y1 - r3 + r1;
        double a2 = 2*(X[1]-X[2]);
        double b2 = 2*(Y[1]-Y[2]);
        double c2 = x3 - x2 + y3 - y2 - r3 + r2;

        double XX = (c2 * b1 - c1 * b2) / (a1*b2 - a2 * b1);
        double YY = (c2 * a1 - c1 * a2) / (a2*b1 - a1 * b2);

        double xx = XX / 10;
        double yy = YY / 10;
        double zz = 150 - H / 10;

//        y = ((X[1]-X[0])*r3 - (X[1]-X[2])*r1 + (X[0]-X[2])*r2
//                + (X[1]-X[2])*x1 - (X[1]-X[0])*x3 - (X[0]-X[2])*x2
//                + (X[1]-X[2])*y1 - (X[1]-X[0])*y3 - (X[0]-X[2])*y2)
//                / (2*(Y[0]*X[1]-Y[1]*X[0]-Y[0]*X[2]+Y[2]*X[0]-Y[2]*X[1]+Y[1]*X[2]));
//
//        x = ((Y[1]-Y[0])*r3 - (Y[1]-Y[2])*r1 + (Y[0]-Y[2])*r2
//                + (Y[1]-Y[2])*y1 - (Y[1]-Y[0])*y3 - (Y[0]-Y[2])*y2
//                + (Y[1]-Y[2])*x1 - (Y[1]-Y[0])*x3 - (Y[0]-Y[2])*x2)
//                / (2*(X[0]*Y[1]-X[1]*Y[0]-X[0]*Y[2]+X[2]*Y[0]-X[2]*Y[1]+X[1]*Y[2]));

        Toast.makeText(this, "x:" + xx + " y:" + yy, Toast.LENGTH_SHORT).show();
        Log.d("htout", "x:" + xx + "y:" + yy);

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
