package com.example.skyworthclub.visible_light_communication.xyj_utils;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skyworthclub on 2018/2/4.
 */

public class MyUtils {
    //图像二值化的阈值范围
    private static final double minVal = 12;
    private static final double maxVal = 255;

    public static Mat LED_Pre_Process(Mat img) {
        //目的输出矩阵
        Mat dst = new Mat();
//        Mat temp = new Mat();

        //图像灰度化
//        Imgproc.cvtColor(img, temp, Imgproc.COLOR_RGB2GRAY);
        //二值化
//        Imgproc.threshold(temp, temp, minVal, maxVal, Imgproc.THRESH_BINARY);
        //细化算法
        dst = MyUtils.ImgThin(img);

        return dst;

    }

    /*
    图像细化算法
    @param srcimage 单通道、二值化后的图
    单通道，二值化的图像矩阵，访问像素点时返回double[]数组
     */
    public static Mat ImgThin(Mat srcimage)
    {
        //需要删除的像素点的索引
        List<Point> deleteList = new ArrayList<>();
        //记录3*3矩阵每个点的数据状态，0表示黑点，1表示白点
        int[] dataArray = new int[9];
        //矩阵大小
        int height = srcimage.rows();
        int width = srcimage.cols();

        while (true) {
            for (int j = 1; j < (height - 1); j++) {
                //矩阵相邻的三行
                Mat data_last = srcimage.row(j - 1);
                Mat data = srcimage.row(j);
                Mat data_next = srcimage.row(j + 1);

                for (int i = 1; i < (width - 1); i++) {
                    if (data.get(0, i)[0] == 255) {
                        //3*3矩阵中间的元素
                        dataArray[0] = 1;
                        if (data_last.get(0, i)[0] == 255) dataArray[1] = 1;
                        else dataArray[1] = 0;
                        if (data_last.get(0, i + 1)[0] == 255) dataArray[2] = 1;
                        else dataArray[2] = 0;
                        if (data.get(0, i + 1)[0] == 255) dataArray[3] = 1;
                        else dataArray[3] = 0;
                        if (data_next.get(0, i + 1)[0] == 255) dataArray[4] = 1;
                        else dataArray[4] = 0;
                        if (data_next.get(0, i)[0] == 255) dataArray[5] = 1;
                        else dataArray[5] = 0;
                        if (data_next.get(0, i - 1)[0] == 255) dataArray[6] = 1;
                        else dataArray[6] = 0;
                        if (data.get(0, i - 1)[0] == 255) dataArray[7] = 1;
                        else dataArray[7] = 0;
                        if (data_last.get(0, i - 1)[0] == 255) dataArray[8] = 1;
                        else dataArray[8] = 0;

                        //白点总数
                        int whitePointTotal = 0;
                        for (int k = 1; k < 9; k++) {
                            whitePointTotal = whitePointTotal + dataArray[k];
                        }
                        //条件一：2<= ... <=6
                        if ((whitePointTotal >= 2) && (whitePointTotal <= 6)) {
                            int ap = 0;
                            if ((dataArray[1] == 0) && (dataArray[2] == 1)) ap++;
                            if ((dataArray[2] == 0) && (dataArray[3] == 1)) ap++;
                            if ((dataArray[3] == 0) && (dataArray[4] == 1)) ap++;
                            if ((dataArray[4] == 0) && (dataArray[5] == 1)) ap++;
                            if ((dataArray[5] == 0) && (dataArray[6] == 1)) ap++;
                            if ((dataArray[6] == 0) && (dataArray[7] == 1)) ap++;
                            if ((dataArray[7] == 0) && (dataArray[8] == 1)) ap++;
                            if ((dataArray[8] == 0) && (dataArray[1] == 1)) ap++;
                            //条件二：01模式个数为1
                            if (ap == 1) {
                                //条件三：p2*p4*p6 = 0
                                //条件四：p4*p6*p8 = 0
                                if ((dataArray[1] * dataArray[5] * dataArray[7] == 0) &&
                                        (dataArray[3] * dataArray[5] * dataArray[7] == 0)) {
                                    //记录像素点的索引值
                                    deleteList.add(new Point(i, j));
                                }
                            }
                        }
                    }
                }
            }
            if (deleteList.size() == 0) break;
            for (int i = 0; i < deleteList.size(); i++) {
                Point tem;
                tem = deleteList.get(i);
                //细化，删除白点
                srcimage.put((int) tem.y, (int) tem.x, 0);
            }
            deleteList.clear();

            for (int j = 1; j < (height - 1); j++) {
                Mat data_last = srcimage.row(j - 1);
                Mat data = srcimage.row(j);
                Mat data_next = srcimage.row(j + 1);
                for (int i = 1; i < (width - 1); i++) {
                    if (data.get(0, i)[0] == 255) {
                        dataArray[0] = 1;
                        if (data_last.get(0, i)[0] == 255) dataArray[1] = 1;
                        else dataArray[1] = 0;
                        if (data_last.get(0, i + 1)[0] == 255) dataArray[2] = 1;
                        else dataArray[2] = 0;
                        if (data.get(0, i + 1)[0] == 255) dataArray[3] = 1;
                        else dataArray[3] = 0;
                        if (data_next.get(0, i + 1)[0] == 255) dataArray[4] = 1;
                        else dataArray[4] = 0;
                        if (data_next.get(0, i)[0] == 255) dataArray[5] = 1;
                        else dataArray[5] = 0;
                        if (data_next.get(0, i - 1)[0] == 255) dataArray[6] = 1;
                        else dataArray[6] = 0;
                        if (data.get(0, i - 1)[0] == 255) dataArray[7] = 1;
                        else dataArray[7] = 0;
                        if (data_last.get(0, i - 1)[0] == 255) dataArray[8] = 1;
                        else dataArray[8] = 0;
                        int whitePointTotal = 0;
                        for (int k = 1; k < 9; k++) {
                            whitePointTotal = whitePointTotal + dataArray[k];
                        }
                        //条件一
                        if ((whitePointTotal >= 2) && (whitePointTotal <= 6)) {
                            int ap = 0;
                            if ((dataArray[1] == 0) && (dataArray[2] == 1)) ap++;
                            if ((dataArray[2] == 0) && (dataArray[3] == 1)) ap++;
                            if ((dataArray[3] == 0) && (dataArray[4] == 1)) ap++;
                            if ((dataArray[4] == 0) && (dataArray[5] == 1)) ap++;
                            if ((dataArray[5] == 0) && (dataArray[6] == 1)) ap++;
                            if ((dataArray[6] == 0) && (dataArray[7] == 1)) ap++;
                            if ((dataArray[7] == 0) && (dataArray[8] == 1)) ap++;
                            if ((dataArray[8] == 0) && (dataArray[1] == 1)) ap++;
                            //条件二
                            if (ap == 1) {
                                //条件三，条件四
                                if ((dataArray[1] * dataArray[3] * dataArray[5] == 0) &&
                                        (dataArray[1] * dataArray[3] * dataArray[7] == 0)) {
                                    deleteList.add(new Point(i, j));
                                }
                            }
                        }
                    }
                }
            }
            if (deleteList.size() == 0) break;
            for (int i = 0; i < deleteList.size(); i++) {
                Point tem;
                tem = deleteList.get(i);
                srcimage.put((int) tem.y, (int) tem.x, 0);
            }
            deleteList.clear();
        }

        return srcimage;
    }

    public static Mat HoughPrecess(Mat img){
        Mat res = new Mat();
        //霍夫检测直线
        Mat lines = new Mat();

        //canny算子进行一次边缘检测
        Imgproc.Canny(img, res, 80, 160);

        //霍夫直线检测
        Imgproc.HoughLinesP(res, lines, 1, Math.PI/180,
                50, 50, 100);

        for (int i=0; i<lines.rows(); i++){
            double[] vec = lines.get(i, 0);

            double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
            Point start = new Point(x1, y1);
            Point end = new Point(x2, y2);
            Imgproc.line(res, start, end, new Scalar(255, 0 ,0), 1);
        }

        Log.e("TAG", "lines的行数："+lines.rows()+"  列数："+lines.cols());
        Log.e("TAG", "lines的维数："+lines.dims()+" 大小："+lines.size());
        Log.e("TAG", "第一个数："+lines.get(0,0).length+" shuju"+lines.get(0,0));

        return res;
    }
}