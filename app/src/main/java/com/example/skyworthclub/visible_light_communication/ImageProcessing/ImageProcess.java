package com.example.skyworthclub.visible_light_communication.ImageProcessing;

import android.util.Log;

import com.example.skyworthclub.visible_light_communication.utils.Coordinate;
import com.example.skyworthclub.visible_light_communication.utils.LedLine;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dn on 2018/5/13.
 */

public class ImageProcess {

    /**
     * 图像预处理
     **/
    public static void preProcess(Mat resMat, Mat disMat) {
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
    }

    /**
     * 分割 Led
     **/
    public static void divideLed(Mat mat, List<Coordinate> X, List<Coordinate> Y, List<Integer> S) {
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

    /**
     * 计算 led 条纹数
     * @param imgs
     * @return
     */
    public static ArrayList<Integer> getLedLineCount(List<Mat> imgs) {
        ArrayList<Integer> ledLineCountList = new ArrayList<Integer>();
        int threhold = 2;
        int lastColor = 0;
        int curColor = 0;

        for (Mat img:imgs) {
            boolean isAWhiteLine = false;
            //取最中间一列
            int middleIndex = img.rows() / 2;
            int sameCount = 0;
            int count = 0;
            for (int i = 0; i < img.cols(); i++) {
                curColor = (int) img.get(i, middleIndex)[0];
                Log.d("htout", "color:" + curColor);
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
}
