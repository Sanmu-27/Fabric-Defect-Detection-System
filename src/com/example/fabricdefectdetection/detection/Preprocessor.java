package com.example.fabricdefectdetection.detection;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * 图像预处理器类
 * 
 * 负责对输入图像进行各种预处理操作，以便于后续的瑕疵检测。
 * 包括灰度转换、降噪、对比度增强等操作。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class Preprocessor {
    
    // 预处理参数 - 调整参数以减少误检率
    private int blurSize = 7;  // 增加模糊大小，去除更多噪点
    private boolean equalizeHistEnabled = false;  // 禁用直方图均衡化，减少对小细节的放大
    private boolean bilateralFilterEnabled = true;
    private int bilateralFilterSize = 11;  // 增加双边滤波尺寸
    private double bilateralSigmaColor = 100.0;  // 增加颜色域标准差
    private double bilateralSigmaSpace = 100.0;  // 增加空间域标准差
    
    // 用于断线检测的参数
    private double cannyThreshold1 = 100.0;  // 提高Canny边缘检测阈值，减少细微边缘的检测
    private double cannyThreshold2 = 200.0;
    
    /**
     * 默认构造函数，使用默认参数
     */
    public Preprocessor() {
        // 使用默认参数
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param blurSize 高斯模糊核大小
     * @param equalizeHistEnabled 是否启用直方图均衡化
     * @param bilateralFilterEnabled 是否启用双边滤波
     */
    public Preprocessor(int blurSize, boolean equalizeHistEnabled, boolean bilateralFilterEnabled) {
        this.blurSize = blurSize;
        this.equalizeHistEnabled = equalizeHistEnabled;
        this.bilateralFilterEnabled = bilateralFilterEnabled;
    }
    
    /**
     * 对图像进行预处理
     * 
     * @param image 输入图像
     * @return 预处理后的图像
     */
    public Mat preprocess(Mat image) {
        // 检查输入
        if (image.empty()) {
            throw new IllegalArgumentException("输入图像为空");
        }
        
        // 创建一个副本，避免修改原始图像
        Mat result = image.clone();
        
        // 转换为灰度图
        if (image.channels() > 1) {
            Mat grayImage = new Mat();
            Imgproc.cvtColor(result, grayImage, Imgproc.COLOR_BGR2GRAY);
            result = grayImage;
        }
        
        // 高斯模糊去噪
        if (blurSize > 0) {
            Mat blurredImage = new Mat();
            Imgproc.GaussianBlur(result, blurredImage, new Size(blurSize, blurSize), 0);
            result = blurredImage;
        }
        
        // 直方图均衡化，增强对比度
        if (equalizeHistEnabled) {
            Mat equalizedImage = new Mat();
            Imgproc.equalizeHist(result, equalizedImage);
            result = equalizedImage;
        }
        
        // 双边滤波，保留边缘的同时去除噪声
        if (bilateralFilterEnabled) {
            Mat filteredImage = new Mat();
            Imgproc.bilateralFilter(
                result, 
                filteredImage, 
                bilateralFilterSize, 
                bilateralSigmaColor, 
                bilateralSigmaSpace
            );
            result = filteredImage;
        }
        
        return result;
    }
    
    /**
     * 特定于布匹图像的预处理方法
     * 
     * @param image 输入图像
     * @return 预处理后的图像
     */
    public Mat preprocessFabric(Mat image) {
        // 基本预处理
        Mat preprocessed = preprocess(image);
        
        // 布匹特定的增强处理
        // 可以根据布匹特性添加额外的处理步骤
        
        // 锐化处理，增强边缘特征
        Mat sharpened = new Mat();
        Mat kernel = new Mat(3, 3, CvType.CV_32F);
        // 拉普拉斯算子
        kernel.put(0, 0, 0, -1, 0, -1, 5, -1, 0, -1, 0);
        Imgproc.filter2D(preprocessed, sharpened, -1, kernel);
        
        return sharpened;
    }
    
    /**
     * 专门用于检测断线的预处理方法
     * 
     * @param image 输入图像
     * @return 增强断线特征的图像
     */
    public Mat preprocessForBrokenThreads(Mat image) {
        // 基本预处理
        Mat preprocessed = preprocess(image);
        
        // 边缘检测，突出断线特征 - 使用更高的阈值减少细微边缘检测
        Mat edges = new Mat();
        Imgproc.Canny(preprocessed, edges, cannyThreshold1, cannyThreshold2);
        
        // 形态学处理，连接断开的线段
        Mat morphed = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));  // 增大核大小
        Imgproc.morphologyEx(edges, morphed, Imgproc.MORPH_CLOSE, kernel);
        
        return morphed;
    }
    
    /**
     * 专门用于检测污渍的预处理方法
     * 
     * @param image 输入图像
     * @return 增强污渍特征的图像
     */
    public Mat preprocessForStains(Mat image) {
        // 基本预处理
        Mat preprocessed = preprocess(image);
        
        // 使用自适应阈值而非全局阈值，更好地处理光照不均的布匹
        Mat thresholded = new Mat();
        Imgproc.adaptiveThreshold(
            preprocessed,
            thresholded,
            255,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            25,  // 增大块大小，减少小噪点的影响
            15   // 增大常数值，减少误检率
        );
        
        // 进一步形态学处理消除噪点
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Mat processed = new Mat();
        Imgproc.morphologyEx(thresholded, processed, Imgproc.MORPH_OPEN, kernel);
        
        return processed;
    }
    
    // Getter和Setter方法
    
    public int getBlurSize() {
        return blurSize;
    }
    
    public void setBlurSize(int blurSize) {
        this.blurSize = blurSize;
    }
    
    public boolean isEqualizeHistEnabled() {
        return equalizeHistEnabled;
    }
    
    public void setEqualizeHistEnabled(boolean equalizeHistEnabled) {
        this.equalizeHistEnabled = equalizeHistEnabled;
    }
    
    public boolean isBilateralFilterEnabled() {
        return bilateralFilterEnabled;
    }
    
    public void setBilateralFilterEnabled(boolean bilateralFilterEnabled) {
        this.bilateralFilterEnabled = bilateralFilterEnabled;
    }
    
    public int getBilateralFilterSize() {
        return bilateralFilterSize;
    }
    
    public void setBilateralFilterSize(int bilateralFilterSize) {
        this.bilateralFilterSize = bilateralFilterSize;
    }
    
    public double getBilateralSigmaColor() {
        return bilateralSigmaColor;
    }
    
    public void setBilateralSigmaColor(double bilateralSigmaColor) {
        this.bilateralSigmaColor = bilateralSigmaColor;
    }
    
    public double getBilateralSigmaSpace() {
        return bilateralSigmaSpace;
    }
    
    public void setBilateralSigmaSpace(double bilateralSigmaSpace) {
        this.bilateralSigmaSpace = bilateralSigmaSpace;
    }
    
    public double getCannyThreshold1() {
        return cannyThreshold1;
    }
    
    public void setCannyThreshold1(double cannyThreshold1) {
        this.cannyThreshold1 = cannyThreshold1;
    }
    
    public double getCannyThreshold2() {
        return cannyThreshold2;
    }
    
    public void setCannyThreshold2(double cannyThreshold2) {
        this.cannyThreshold2 = cannyThreshold2;
    }
} 