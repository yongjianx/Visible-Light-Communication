package com.example.skyworthclub.visible_light_communication.xyj_activity;

import android.graphics.Rect;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.skyworthclub.visible_light_communication.R;


import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {
    private final static String TAG = "CameraActivity";
    private Camera camera;
    private Camera.Parameters parameters;
    private boolean isPreview = true;
    private SurfaceView surfaceView;
    private Button camera_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camera);
        init();
    }

    private void init(){
        surfaceView = findViewById(R.id.camera_surface);
//        camera_btn = findViewById(R.id.camera_btn);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.setFixedSize(800, 600);
        //设置屏幕常亮
        holder.setKeepScreenOn(true);
        //设置surfaceView不维护自己的缓冲区,而是等待屏幕的渲染引擎将内容推送到用户面前
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceCallback());
    }

    private class SurfaceCallback implements SurfaceHolder.Callback{
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (camera == null){
                //打开前置摄像头
                camera = Camera.open(1);
            }
            try {
//                Display display = getWindow().getWindowManager().getDefaultDisplay();
//                Point size = new Point();
//                display.getSize(size);
//                int screenWidth = size.x;
//                int screenHeight = size.y;
//
//                /**获得最佳分辨率，注意此时要传的width和height是指横屏时的,所以要颠倒一下**/
//                int[] bestResolution = getBestResolution(parameters, screenHeight, screenWidth);
//                parameters.setPreviewSize(bestResolution[0], bestResolution[1]);
//                parameters.setRotation(90);

                initCamera();
                //预览方向
                camera.setDisplayOrientation(90);
                camera.setParameters(parameters);

                //通过SurfaceView显示取景画面
                camera.setPreviewDisplay(holder);
                //开始预览
                camera.startPreview();
//                camera.cancelAutoFocus();
                //设置是否预览参数为真
                isPreview = true;

                surfaceView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.e(TAG, "onTouch");
                        Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY(), 1f);
                        Rect meteringRect = calculateTapArea(event.getRawX(), event.getRawY(), 1.5f);

                        if (parameters.getMaxNumFocusAreas() > 0) {
                            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                            focusAreas.add(new Camera.Area(focusRect, 1000));

                            parameters.setFocusAreas(focusAreas);
                        }

                        if (parameters.getMaxNumMeteringAreas() > 0) {
                            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                            meteringAreas.add(new Camera.Area(meteringRect, 1000));

                            parameters.setMeteringAreas(meteringAreas);
                        }
                        try{
                            camera.setParameters(parameters);
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                        return true;
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success){
                        Log.e(TAG, "autoFocus");
                        initCamera();
                        camera.cancelAutoFocus();
                    }
                }
            });
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if(camera != null){
                if(isPreview){
                    //如果正在预览
                    camera.stopPreview();
                    camera.release();
                }
            }
        }
    }

    /*
    摄像头参数初始化
     */
    private void initCamera(){
        //得到摄像头的参数
        parameters = camera.getParameters();

        //获取预览的各种分辨率
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        //获取摄像头支持的各种分辨率
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        for (int i = 0; i < supportedPreviewSizes.size(); i++) {
            Log.e("TAG","预览的各种分辨率"+supportedPreviewSizes.get(i).width+"----"+supportedPreviewSizes.get(i).height);
        }
        for (int i = 0; i < supportedPictureSizes.size(); i++) {
            Log.e("TAG","摄像头支持的各种分辨率"+supportedPictureSizes.get(i).width+"----"+supportedPictureSizes.get(i).height);
        }
        for (int i = 0; i < supportedFocusModes.size(); i++) {
            Log.e("TAG","摄像头支持的对焦模式"+supportedFocusModes.get(i)+"----");
        }
//        for (int i=0; i<supportedFlashModes.size(); i++){
//            Log.e(TAG, "摄像头支持的刷新模式："+supportedFlashModes.get(i)+"----");
//        }

        //设置预览照片的分辨率
        parameters.setPreviewSize(800, 480);
//        //设置帧率*1000
//        parameters.setPreviewFpsRange(2, 5);
//        //设置照片的格式
//        parameters.setPictureFormat(PixelFormat.JPEG);
//        //设置照片的质量
//        parameters.setJpegQuality(85);
//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//        //连续对焦模式
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        //设置照片的分辨率
        parameters.setPictureSize(800, 480);
        parameters.setExposureCompensation(parameters.getMinExposureCompensation());
    }

    private int[] getBestResolution(Camera.Parameters parameters, int width, int height) {
        int[] bestResolution = new int[2];//int数组，用来存储最佳宽度和最佳高度
        int bestResolutionWidth = -1;//最佳宽度
        int bestResolutionHeight = -1;//最佳高度

        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();//获得设备所支持的分辨率列表
        int difference = 99999;//最小差值，初始化市需要设置成一个很大的数

        //遍历sizeList，找出与期望分辨率差值最小的分辨率
        for (int i = 0; i < sizeList.size(); i++) {
            int differenceWidth = Math.abs(width - sizeList.get(i).width);//求出宽的差值
            int differenceHeight = Math.abs(height - sizeList.get(i).height);//求出高的差值

            //如果它们两的和，小于最小差值
            if ((differenceWidth + differenceHeight) < difference) {
                difference = (differenceWidth + differenceHeight);//更新最小差值
                bestResolutionWidth = sizeList.get(i).width;//赋值给最佳宽度
            }
            Log.d("htout", "width:" + bestResolutionWidth + " height:" + bestResolutionHeight);
        }

        //最后将最佳宽度和最佳高度添加到数组中
        bestResolution[0] = bestResolutionWidth;
        bestResolution[1] = bestResolutionHeight;
        return bestResolution;//返回最佳分辨率数组
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();

        int centerX = (int) (x /parameters.getPictureSize().width * 2000 - 1000);
        int centerY = (int) (y / parameters.getPreviewSize().height * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);

        return new Rect(left, top, right, bottom);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
