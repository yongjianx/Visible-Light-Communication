package ht.activity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.skyworthclub.visible_light_communication.R;
import com.example.skyworthclub.visible_light_communication.xyj_utils.MyUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ht.bean.Coordinate;
import ht.bean.LedLine;

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
//        mImageView = findViewById(R.id.image);
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
//        Log.d("htout", "resmat:" + dst.rows() + " " + dst.cols());
        return dst;
    }

    private void isLed2(Mat mat, List<Coordinate> X, List<Coordinate> Y, List<Integer> S) {
        int row = mat.rows();
        int col = mat.cols();
        int ii = 0;
        Log.d(TAG, "mat row:" + row + " col:" + col);
        double[] temp1 = new double[col];
        Log.d(TAG, "temp" + temp1[0]);

        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j ++) {
//                Log.d(TAG, "mat[i][j]:" + mat.get(i, j)[0]);
                if (mat.get(j, i)[0] != 0) {
                    temp1[i] = 1;
                    break;
                }
            }
        }

        while (temp1[ii] == 0) {
            ii++;
        }
        int xMin = ii;
        while (temp1[ii] != 0 && ii < col - 1) {
            ii++;
        }
        int xMax = ii;

        Mat mat1 = mat.submat(0, row, xMin, xMax);
        Log.d(TAG, "mat1 row:" + mat1.rows() + " col:" + mat1.cols());
        double[] temp2 = new double[row];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < mat1.cols(); j ++) {
                if (mat1.get(i, j)[0] != 0) {
                    temp2[i] = 1;
                    break;
                }
            }
        }
        ii = 0;
        while (temp2[ii] == 0) {
            ii++;
        }
        int yMin = ii;
        while (temp2[ii] != 0 && ii < row - 1) {
            ii++;
        }
        int yMax = ii;
        while (temp2[ii] == 0 && ii < row - 1) {
            ii++;
        }

        if (ii == row - 1) {
            if(yMin - 10 > 0) {
                yMin -= 10;
            }
            if(yMax + 10 < row - 1) {
                yMax += 10;
            }
            for (int i = xMin; i <= xMax; i++) {
                for (int j = yMin; j <= yMax; j ++) {
                    mat.put(j, i, 0);
                }
            }
        } else {
            Mat mat2 = mat.submat(yMin, yMax, 0, col);
            double[] temp3 = new double[col];
            ii = 0;
            for (int i = 0; i < col; i++) {
                for (int j = 0; j < mat2.rows(); j ++) {
//                Log.d(TAG, "mat[i][j]:" + mat.get(i, j)[0]);
                    if (mat2.get(j, i)[0] != 0) {
                        temp3[i] = 1;
                        break;
                    }
                }
            }
            while (temp1[ii] == 0) {
                ii++;
            }
            xMin = ii;
            while (temp1[ii] != 0 && ii < col - 1) {
                ii++;
            }
            xMax = ii;
            if(yMin - 10 > 0) {
                yMin -= 10;
            }
            if(yMax + 10 < row - 1) {
                yMax += 10;
            }
            for (int i = xMin; i <= xMax; i++) {
                for (int j = yMin; j < yMax; j ++) {
                    mat.put(j, i, 0);
                }
            }
        }

        Coordinate x = new Coordinate(xMin, xMax, Math.round((xMin + xMax) / 2));
        Coordinate y = new Coordinate(yMin, yMax, Math.round((yMin + yMax) / 2));
        X.add(x);
        Y.add(y);
        S.add(Math.round(Math.abs(xMax - xMin) * Math.round(Math.abs(yMax - yMin))));
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

        handlePicture();

        mCamera = ((CameraControlView)mCameraView).getCamera();
    }

    private void handlePicture() {
        Mat resMat = new Mat();
        Mat disMat = new Mat();
        //获得图片并转换成矩阵
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.led);
        Utils.bitmapToMat(bitmap, resMat);

//        Log.d("htout", "aaa" + resMat.width() + " " + resMat.height());
//        Size size = new Size(resMat.width() * 0.8, resMat.height() * 0.8);
//        Imgproc.resize(resMat, resMat, size);

        Log.d("htout", "aaa");
        //灰度化
        Imgproc.cvtColor(resMat, disMat, Imgproc.COLOR_RGB2GRAY);
        //二值化
        Imgproc.threshold(disMat, disMat, 12, 255, Imgproc.THRESH_TOZERO);
        Imgproc.threshold(disMat, disMat, 12, 255, Imgproc.THRESH_BINARY);
        //形态学闭运算
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(40, 40));
        Imgproc.morphologyEx(disMat, disMat, Imgproc.MORPH_CLOSE, kernel);

//        //腐蚀
//        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(10, 10));
//        Imgproc.erode(disMat, disMat, kernel);
//        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, new Size(10, 10));
//        Imgproc.erode(disMat, disMat, kernel);
//
//        //膨胀
//        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(10, 10));
//        Imgproc.dilate(disMat, disMat, kernel);
//        kernel=Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, new Size(10, 10));
//        Imgproc.dilate(disMat, disMat, kernel);

        //灰度化
        Imgproc.cvtColor(resMat, resMat, Imgproc.COLOR_RGB2GRAY);
        //二值化
        Imgproc.threshold(resMat, resMat, 12, 255, Imgproc.THRESH_TOZERO);
        Imgproc.threshold(resMat, resMat, 12, 255, Imgproc.THRESH_BINARY);

        Log.d("htout", "bbb");

        List<Mat> img = new ArrayList<Mat>();
        List<Coordinate> X = new ArrayList<Coordinate>();
        List<Coordinate> Y = new ArrayList<Coordinate>();
        List<Integer> S = new ArrayList<Integer>();
        getLed(resMat, disMat, img, X, Y, S);

        Log.d("htout", "ccc");
        Log.d(TAG, "width:" + disMat.cols() + " height:" + disMat.rows());
//        Bitmap newBitmap = Bitmap.createBitmap(disMat.cols(), disMat.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(disMat, newBitmap);
//        mImageView.setImageBitmap(newBitmap);

        //检测led条纹数
//        mLedLineList = MyUtils.LED_Pre_Process(img);
        mLedLineList = getLedLineCount(img);
        isCollinear(X, Y);
        getLocation();
        Log.e("TAG", "img的大小："+img.size());
//        img.add(MyUtils.HoughPrecess(img.get(img.size()-1)));
//
//        Bitmap led = Bitmap.createBitmap(img.get(img.size()-3).cols(), img.get(img.size()-3).rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(img.get(img.size()-3), led);
//        mDivideImg.setImageBitmap(led);
    }

    private void getLed(Mat resMat, Mat disMat, List<Mat> img, List<Coordinate> X, List<Coordinate> Y, List<Integer> S) {
        int count = 1;
//        isLed2(disMat, X, Y, S);
//        while (!isAllBlack(disMat)) {
//            isLed2(disMat, X, Y, S);
//            count++;
//            Log.d(TAG, "count:" + count);
//        }
        divideLed(disMat, X, Y, S);
        for (int i = 0; i < 3; i++) {
            Mat mat = resMat.submat(Y.get(i).getMin(), Y.get(i).getMax(), X.get(i).getMin(), X.get(i).getMax());
            Log.d("htout", "divide2:" + X.get(i).getMin() + " " + Y.get(i).getMin() + " " + mat.cols());
            img.add(mat);
        }
    }

    /**
     * 分割 Led
     **/
    private void divideLed(Mat mat, List<Coordinate> X, List<Coordinate> Y, List<Integer> S) {
        int row = mat.rows();
        int col = mat.cols();
        //当前样点颜色
        int curColor;
        //上一采样点颜色
        int lastColor = 255;
        //当前采样点是否属于某一条白色条纹内的点
        boolean isAWhiteLine = false;
        //阈值，当采样点的颜色连续都为同一颜色的次数超过此阈值时，则判定为一条条纹
        int threhold = 10;
        //采样点与上一采样点颜色相同的次数
        int sameCount = 0;
        int xMin = 0;
        int xMax = 0;
        int yMid = 0;
        List<LedLine> ledLineList = new ArrayList<LedLine>();
        //抽样采点，间隔为4
        for (int i = 0; i < row; i += 4) {
            for (int j = 0; j < col; j += 4) {
                curColor = (int) mat.get(i, j)[0];
                if (isAWhiteLine) {
                    //当前处于白色条纹状态，接下来需寻找黑色条纹状态

                    if (curColor == 255) {
                        sameCount = 0;
                    }
                    //连续采样点颜色为黑色
                    if (lastColor == curColor && curColor == 0) {
                        sameCount++;
                    }
                    //超过阈值，判定为黑色条纹
                    if (sameCount > threhold) {
                        isAWhiteLine = false;
                        //记录这条黑色条纹之上的白色条纹的位置
                        xMax = j - 4 * (threhold);
                        int r = (xMax - xMin) / 2;
                        int fixR = r - 8;
                        int time = 0;
                        //调整白色条纹纵坐标位置
                        while (true) {
                            if (yMid - fixR < 0 || yMid + fixR > mat.rows()) {
                                break;
                            }
                            if (mat.get(yMid - fixR, xMin + r)[0] == 255 && mat.get(yMid + fixR, xMin + r)[0] == 255) {
                                break;
                            } else if (mat.get(yMid - fixR, xMin + r)[0] == 255 && mat.get(yMid + fixR, xMin + r)[0] == 0) {
                                yMid--;
                            } else if (mat.get(yMid - fixR, xMin + r)[0] == 0 && mat.get(yMid + fixR, xMin + r)[0] == 255) {
                                yMid++;
                            } else {
                                break;
                            }
                            time++;
                            if (time >= 20) {
                                break;
                            }
                        }
                        LedLine ledLine = new LedLine(xMin, xMax, yMid);
                        ledLineList.add(ledLine);
                    }
                    lastColor = curColor;
                } else {
                    if (curColor == 0) {
                        sameCount = 0;
                    }
                    if (lastColor == curColor && curColor == 255) {
                        sameCount++;
                    }
                    if (sameCount >= threhold) {
                        isAWhiteLine = true;
                        //记录此时白色条纹最左侧的位置即纵坐标位置
                        xMin = j - 4 * (threhold + 1);
                        if (xMin < 0) {
                            xMin = 0;
                        }
                        yMid = i;
                    }
                    lastColor = curColor;
                }
            }
        }
        //根据条纹的长度排序
        Collections.sort(ledLineList);
        List<Integer> xMinList = new ArrayList<Integer>();
        //去重，去除条纹最左侧坐标相近的条纹
        for (int i = 0; i < ledLineList.size(); i++) {
            LedLine ledLine = ledLineList.get(i);
            int xmin = ledLine.getXMin() - 3;
            int xmax = ledLine.getXMax() + 3;
            int ymid = ledLine.getYMid();
            int ymin = ymid - (xmax - xmin) / 2 - 10;
            int ymax = ymid + (xmax - xmin) / 2 + 10;
            boolean isXMinExit = false;
            for (int j = 0; j < xMinList.size(); j++) {
                int exitXMin = xMinList.get(j);
                if (Math.abs(exitXMin - xmin) < 100) {
                    isXMinExit = true;
//                    if (xmin < exitXMin) {
//                        xMinList.add(j, xmin);
//                    }
                    break;
                }
            }
            if (!isXMinExit) {
                Log.d("htout", "divide:" + xmin + " " + xmax + " " + ymin + " " + ymax);
                Coordinate x = new Coordinate(xmin, xmax, Math.round((xmin + xmax) / 2));
                Coordinate y = new Coordinate(ymin, ymax, Math.round((ymin + ymax) / 2));
                X.add(x);
                Y.add(y);
                S.add(Math.round(Math.abs(xmax - xmin) * Math.round(Math.abs(ymax - ymin))));
                xMinList.add(xmin);
            }
        }
        Log.d("htout", "S size:" + S.size());
    }

    private boolean isAllBlack(Mat mat) {
        for (int i = 0; i < mat.rows()/4; i++) {
            for (int j = 0; j < mat.cols()/4; j ++) {
                if (mat.get(4 * i, 4 * j)[0] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 计算 led 条纹数
     * @param imgs
     * @return
     */
    private ArrayList<Integer> getLedLineCount(List<Mat> imgs) {
        ArrayList<Integer> ledLineCountList = new ArrayList<Integer>();
        int threhold = 2;
        int lastColor = 0;
        int curColor = 0;
        for (Mat img : imgs) {
            boolean isAWhiteLine = false;
            //取最中间一列
            int middleIndex = img.rows() / 2;
            int sameCount = 0;
            int count = 0;
            for (int i = 0; i < img.cols(); i++) {
                curColor = (int) img.get(i, middleIndex)[0];
                if (isAWhiteLine) {
                    if (curColor == 255) {
                        sameCount = 0;
                    }
                    if (lastColor == curColor && curColor == 0) {
                        sameCount++;
                    }
                    if (sameCount > threhold) {
                        isAWhiteLine = false;
                    }
                    lastColor = curColor;
                } else {
                    if (curColor == 0) {
                        sameCount = 0;
                    }
                    if (lastColor == curColor && curColor == 255) {
                        sameCount++;
                    }
                    if (sameCount >= threhold) {
                        isAWhiteLine = true;
                        count++;
                    }
                    lastColor = curColor;
                }

            }

            ledLineCountList.add(count);
            Log.d("htout", "ledlinecount:" + count);
        }
        return ledLineCountList;
    }

    //判断三个LED是否共线
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
