package com.example.skyworthclub.visible_light_communication.xyj_activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.skyworthclub.visible_light_communication.R;

import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.support.v4.math.MathUtils.clamp;

public class Camera2Activity extends AppCompatActivity{
    private final static String TAG = "Camera2Activity";

    private static final int MAX_PREVIEW_WIDTH = 1920;//Camera2 API 保证的最大预览宽高
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private static final int STATE_PREVIEW = 0;//显示相机预览
    private static final int STATE_WAITING_LOCK = 1;//焦点锁定中
    private static final int STATE_WAITING_PRE_CAPTURE = 2;//拍照中
    private static final int STATE_WAITING_NON_PRE_CAPTURE = 3;//其它状态
    private static final int STATE_PICTURE_TAKEN = 4;//拍照完毕
    private int mState = STATE_PREVIEW;
    private TextureView textureView;
//    private Button btn;


    //线程
    private HandlerThread handlerThread;
    private Handler handler;
    private CameraCharacteristics characteristics;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraDevice cameraDevice;

    private Size mPreviewSize;
    private Rect mActiveArraySize;
    private Rect rect = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera2);
        init();

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera2();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraDevice.close();
        cameraDevice = null;
    }

    private void init(){
        //HandlerThread为android的准用类，继承自Thread
        handlerThread = new HandlerThread("CAMERA2");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        textureView = findViewById(R.id.camera2_surface);
//        btn = findViewById(R.id.camera2_btn);
    }

    private void openCamera2(){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //获取可用相机设备列表
            String[] CameraIdList = cameraManager.getCameraIdList();
            Log.e(TAG, "相机设备列表："+CameraIdList);

            //通过CameraCharacteristics设置相机的功能,必须检查是否支持
            characteristics = cameraManager.getCameraCharacteristics(CameraIdList[1]);

            getParams();
            cameraManager.openCamera(CameraIdList[1], mCameraDeviceStateCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            try {
                cameraDevice = camera;
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void startPreview(CameraDevice camera) throws CameraAccessException {
        SurfaceTexture texture = textureView.getSurfaceTexture();
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            //预览模式
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        //target是预览界面
        mPreviewBuilder.addTarget(surface);
        //List为preview和ImageReader的surface,此处仅添加了preview
        camera.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, handler);
    }

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            autoFocus(session);

//            fixedFocus(session);

        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {}
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {

        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult){
            checkState(session, partialResult);
        }
    };

    /*
    获取摄像头的参数
     */
    int minmin;
    private void getParams(){
        //获取设备等级  LEGACY=2 < LIMITED=0 < FULL=1 < LEVEL_3=3. 越靠右边权限越大
        Log.e(TAG, "设备等级："+characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL));

        try{
            //获取最小焦距参数
            float minimumLens = characteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE);
//            float num = (((float) i) * minimumLens / 100);
            Log.e(TAG, "最小焦距："+minimumLens);
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "没有最小焦距");
        }
        //曝光增益
        Range<Integer> range1 = characteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
        int maxmax = range1.getUpper();
        minmin = range1.getLower();
        Log.e(TAG, "最小最大曝光增益："+minmin+" "+maxmax);

        try {
            //曝光时间
            Range<Long> range = characteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
            long max = range.getUpper();
            long min = range.getLower();
//        long ae = ((i * (max - min)) / 100 + min);
            Log.e(TAG, "最小最大曝光时间:" + min + " " + max);
        }catch (Exception e){
            e.printStackTrace();
        }

        //支持的STREAM CONFIGURATION
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //摄像头支持的预览Size数组
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);

        //获取最佳的预览长款
        Size vedioSize = chooseVideoSize(sizes);
        mPreviewSize = chooseOptimalSize(sizes, textureView.getWidth(), textureView.getHeight(), vedioSize);
        Log.e(TAG, "mPreviewSize:"+ mPreviewSize);

        for(Size oneSize :sizes)
        {
            Log.i("支持的预览分辨率：", String.valueOf(oneSize.getHeight())+" "+String.valueOf(oneSize.getWidth()));
        }
//        mPreviewSize = sizes[0];

        //真正接收光线的区域,成像的区域是该参数指定的区域
        mActiveArraySize = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        Log.e(TAG, "成像区域参数activeArraySize:"+mActiveArraySize);
    }

    /*
    自动对焦
     */
    private void autoFocus(CameraCaptureSession session){
        try {
            //自动对焦模式
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            //曝光度设置
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, minmin);
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            //session.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mHandler);
            session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, handler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*
    手动对焦
     */
    private void clickFocus(CameraCaptureSession session){
        //指定自动对焦区域
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        //自动嚗光区域
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[] {new MeteringRectangle(rect, 1000)});
        //指定自动对焦模式为 CONTROL_AF_MODE_AUTO 模式
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
        //对焦的状态修改为开始对焦
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        //触发连续获取图像数据
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        //通知mCaptureCallback等待锁定
        mState = STATE_WAITING_LOCK;

        try {
            session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, "setRepeatingRequest failed, " + e.getMessage());
        }
    }

    /*
    固定焦距
     */
    private void fixedFocus(CameraCaptureSession session){
        try{
            //关闭自动对焦模式
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);

//            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, minmin);
            //设置焦距值
            mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float)60);
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, -12);

            session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, handler);
        }
        catch (CameraAccessException e){
            e.printStackTrace();
        }
    }

    /*
    这个函数根据长宽比，选择只支持长宽比为4:3的分辨率，同时宽小于1080p
    @params choices 预览参数
     */
    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    /*
    这个函数选择长比宽为aspectRotio一样的分辨率，同时如果长宽大于指定的宽高，就选用中间最小的一个，否则选用choices[0]
    @params choices 预览参数
    @params width height TextureView的长宽
    @params aspectRatio 4:3的长宽比例的尺寸分辨率
     */
    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        //选择合适的长宽比的分辨率
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }

        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        }
        else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /*
     * 比较两者大小
     */
    private static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }

    private void checkState(CameraCaptureSession session, CaptureResult result) {
        switch (mState) {
            case STATE_PREVIEW:
                // NOTHING
                break;
            case STATE_WAITING_LOCK:
                int afState = result.get(CaptureResult.CONTROL_AF_STATE);
                Log.e(TAG, "afState的值："+afState);

                if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                        || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

                    try {
                        session.setRepeatingRequest(mPreviewBuilder.build(), null, handler);
                    } catch (CameraAccessException e) {
                        Log.e(TAG, "setRepeatingRequest failed, errMsg: " + e.getMessage());
                    }
                }
                break;
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // 先取相对于view上面的坐标
//        double x = event.getX(), y = event.getY(), tmp;
//        Log.e(TAG, "onTouch  x:"+x+"  y:"+y);
//
//        Canvas canvas = new Canvas();
//        Paint p = new Paint();
//        p.setColor(Color.BLACK);
//        p.setStrokeWidth(1);
//        textureView.lockCanvas();
//        canvas.drawRect((float) x-30, (float) y-30, (float)x+30, (float) y+30, p);
//        textureView.unlockCanvasAndPost(canvas);
//
//        // 取出来的图像如果有旋转角度的话，则需要将宽高交换下
//        int realPreviewWidth = mPreviewSize.getWidth(), realPreviewHeight = mPreviewSize.getHeight();
//        int mDisplayRotate = getWindowManager().getDefaultDisplay().getRotation();
//        if (90 == mDisplayRotate || 270 == mDisplayRotate) {
//            realPreviewWidth = mPreviewSize.getHeight();
//            realPreviewHeight = mPreviewSize.getWidth();
//        }
//
//        // 计算摄像头取出的图像相对于view放大了多少，以及有多少偏移
//        double imgScale = 1.0, verticalOffset = 0, horizontalOffset = 0;
//        if (realPreviewHeight * textureView.getWidth() > realPreviewWidth * textureView.getHeight()) {
//            imgScale = textureView.getWidth() * 1.0 / realPreviewWidth;
//            verticalOffset = (realPreviewHeight - textureView.getHeight() / imgScale) / 2;
//        } else {
//            imgScale = textureView.getHeight() * 1.0 / realPreviewHeight;
//            horizontalOffset = (realPreviewWidth - textureView.getWidth() / imgScale) / 2;
//        }
//
//        // 将点击的坐标转换为图像上的坐标
//        x = x / imgScale + horizontalOffset;
//        y = y / imgScale + verticalOffset;
//        if (90 == mDisplayRotate) {
//            tmp = x;
//            x = y;
//            y = mPreviewSize.getHeight() - tmp;
//        } else if (270 == mDisplayRotate) {
//            tmp = x;
//            x = mPreviewSize.getWidth() - y;
//            y = tmp;
//        }
//
//        // 计算取到的图像相对于裁剪区域的缩放系数，以及位移
//        Rect cropRegion = mPreviewBuilder.get(CaptureRequest.SCALER_CROP_REGION);
//        if (null == cropRegion) {
//            Log.e(TAG, "can't get crop region");
//            cropRegion = mActiveArraySize;
//        }
//
//        int cropWidth = cropRegion.width(), cropHeight = cropRegion.height();
//        if (mPreviewSize.getHeight() * cropWidth > mPreviewSize.getWidth() * cropHeight) {
//            imgScale = cropHeight * 1.0 / mPreviewSize.getHeight();
//            verticalOffset = 0;
//            horizontalOffset = (cropWidth - imgScale * mPreviewSize.getWidth()) / 2;
//        } else {
//            imgScale = cropWidth * 1.0 / mPreviewSize.getWidth();
//            horizontalOffset = 0;
//            verticalOffset = (cropHeight - imgScale * mPreviewSize.getHeight()) / 2;
//        }
//
//        // 将点击区域相对于图像的坐标，转化为相对于成像区域的坐标
//        x = x * imgScale + horizontalOffset + cropRegion.left;
//        y = y * imgScale + verticalOffset + cropRegion.top;
//
//        //按照对焦区域为成像区域的0.1倍计算对焦的矩形
//        double tapAreaRatio = 0.1;
//        rect.left = clamp((int) (x - tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
//        rect.right = clamp((int) (x + tapAreaRatio / 2 * cropRegion.width()), 0, cropRegion.width());
//        rect.top = clamp((int) (y - tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
//        rect.bottom = clamp((int) (y + tapAreaRatio / 2 * cropRegion.height()), 0, cropRegion.height());
//        Log.e(TAG, "图像的rect:"+rect);
//
//        return true;
//    }
}


