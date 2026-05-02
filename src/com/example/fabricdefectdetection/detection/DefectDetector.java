package com.example.fabricdefectdetection.detection;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * 布匹瑕疵检测器类
 * 
 * 实现布匹瑕疵检测的核心算法，包括图像预处理、瑕疵识别与分类等功能。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DefectDetector {
    
    // 预处理器
    private Preprocessor preprocessor;
    
    // 检测参数
    private int adaptiveThresholdBlockSize = 35;
    private double adaptiveThresholdConstant = 10.0;
    private int morphologyKernelSize = 3;
    private double minDefectArea = 100.0;
    private double maxDefectArea = 10000.0;
    
    // 调整检测阈值参数，减小误检率
    private static final double THRESHOLD_VALUE = 40.0;  // 从默认值提高
    private static final double MAX_THRESHOLD_VALUE = 255.0;
    private static final int THRESHOLD_TYPE = Imgproc.THRESH_BINARY;
    
    // 增加最小缺陷尺寸，过滤小噪点
    private static final int MIN_CONTOUR_AREA = 150;  // 从默认值提高
    private static final int MIN_DEFECT_SIZE = 20;   // 最小缺陷尺寸
    private static final int MAX_DEFECT_COUNT = 30;  // 最大缺陷数量限制
    
    /**
     * 默认构造函数
     */
    public DefectDetector() {
        this.preprocessor = new Preprocessor();
    }
    
    /**
     * 带参数的构造函数
     * 
     * @param preprocessor 自定义预处理器
     */
    public DefectDetector(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }
    
    /**
     * 检测布匹图像中的瑕疵
     * 
     * @param originalImage 原始图像
     * @return 检测结果
     */
    public DetectionResult detectDefects(Mat originalImage) {
        if (originalImage == null || originalImage.empty()) {
            throw new IllegalArgumentException("输入图像为空");
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 图像预处理
            Mat preprocessedImage = preprocessor.preprocess(originalImage.clone());
            
            // 2. 瑕疵检测
            List<Rect> defectRects = new ArrayList<>();
            List<String> defectTypes = new ArrayList<>();
            
            // 检测不同类型的瑕疵
            detectHolesAndStains(preprocessedImage, defectRects, defectTypes);
            detectBrokenThreads(preprocessedImage, defectRects, defectTypes);
            
            // 3. 在原图上标记瑕疵
            Mat resultImage = originalImage.clone();
            
            for (int i = 0; i < defectRects.size(); i++) {
                Rect rect = defectRects.get(i);
                String type = defectTypes.get(i);
                
                // 根据类型选择颜色
                Scalar color;
                switch (type) {
                    case "污渍":
                        color = new Scalar(0, 0, 255); // 红色
                        break;
                    case "破洞":
                        color = new Scalar(0, 255, 255); // 黄色
                        break;
                    case "断线":
                        color = new Scalar(255, 0, 0); // 蓝色
                        break;
                    default:
                        color = new Scalar(255, 255, 255); // 白色
                }
                
                // 绘制矩形框
                Imgproc.rectangle(resultImage, rect.tl(), rect.br(), color, 2);
                
                // 绘制类型文本
                Imgproc.putText(
                    resultImage,
                    type,
                    new Point(rect.x, rect.y - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.5,
                    color,
                    1
                );
            }
            
            // 4. 创建检测结果
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            String message = String.format("检测到 %d 个瑕疵（%d个破洞，%d个污渍，%d个断线）",
                defectRects.size(),
                countDefectsByType(defectTypes, "破洞"),
                countDefectsByType(defectTypes, "污渍"),
                countDefectsByType(defectTypes, "断线")
            );
            
            return new DetectionResult(
                resultImage,
                defectRects,
                defectTypes,
                message,
                processingTime
            );
            
        } catch (Exception e) {
            // 发生错误时返回原图和错误信息
            long endTime = System.currentTimeMillis();
            return new DetectionResult(
                originalImage.clone(),
                "检测过程中发生错误: " + e.getMessage()
            );
        }
    }
    
    /**
     * 检测破洞和污渍
     * 
     * @param image 预处理后的图像
     * @param defectRects 用于存储检测到的瑕疵区域
     * @param defectTypes 用于存储检测到的瑕疵类型
     */
    private void detectHolesAndStains(Mat image, List<Rect> defectRects, List<String> defectTypes) {
        // 1. 转为灰度图（如果不是）
        Mat grayImage = image.clone();
        if (image.channels() > 1) {
            grayImage = new Mat();
            Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        }
        
        // 2. 自适应阈值处理，突出瑕疵
        Mat binaryImage = new Mat();
        Imgproc.adaptiveThreshold(
            grayImage,
            binaryImage,
            255,
            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            Imgproc.THRESH_BINARY_INV,
            adaptiveThresholdBlockSize,
            adaptiveThresholdConstant
        );
        
        // 3. 形态学操作，去除噪点
        Mat kernel = Imgproc.getStructuringElement(
            Imgproc.MORPH_RECT,
            new Size(morphologyKernelSize, morphologyKernelSize)
        );
        
        Mat morphedImage = new Mat();
        Imgproc.morphologyEx(binaryImage, morphedImage, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(morphedImage, morphedImage, Imgproc.MORPH_CLOSE, kernel);
        
        // 4. 寻找轮廓
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(
            morphedImage,
            contours,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_SIMPLE
        );
        
        // 5. 分析轮廓，识别瑕疵
        for (MatOfPoint contour : contours) {
            double area = Imgproc.contourArea(contour);
            
            // 根据面积筛选，排除过小或过大的区域
            if (area >= minDefectArea && area <= maxDefectArea) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                
                // 计算长宽比
                double aspectRatio = (double) boundingRect.width / boundingRect.height;
                
                // 根据特征判断瑕疵类型
                if (area > 1000 && Math.abs(aspectRatio - 1.0) < 0.5) {
                    // 面积较大且形状接近圆形，现在判断为污渍（原来是破洞）
                    defectRects.add(boundingRect);
                    defectTypes.add("污渍");
                } else if (area < 500) {
                    // 面积较小，现在判断为破洞（原来是污渍）
                    defectRects.add(boundingRect);
                    defectTypes.add("破洞");
                } else {
                    // 其他情况，现在标记为破洞（原来是污渍）
                    defectRects.add(boundingRect);
                    defectTypes.add("破洞");
                }
            }
        }
    }
    
    /**
     * 检测断线
     * 
     * @param image 预处理后的图像
     * @param defectRects 用于存储检测到的瑕疵区域
     * @param defectTypes 用于存储检测到的瑕疵类型
     */
    private void detectBrokenThreads(Mat image, List<Rect> defectRects, List<String> defectTypes) {
        // 使用专门的预处理方法增强断线特征
        Mat processedImage = preprocessor.preprocessForBrokenThreads(image.clone());
        
        // 寻找线条
        Mat lines = new Mat();
        Imgproc.HoughLinesP(
            processedImage,
            lines,
            1,
            Math.PI / 180,
            50,
            50,
            10
        );
        
        // 分析检测到的线条
        if (!lines.empty()) {
            for (int i = 0; i < lines.rows(); i++) {
                double[] line = lines.get(i, 0);
                
                // 线条端点
                Point pt1 = new Point(line[0], line[1]);
                Point pt2 = new Point(line[2], line[3]);
                
                // 计算线段长度
                double length = Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
                
                // 只考虑长度适中的线段（可能是断线）
                if (length > 30 && length < 200) {
                    // 创建线段周围的矩形区域（稍微扩大以包含线段）
                    int padding = 5;
                    int x = (int) Math.min(pt1.x, pt2.x) - padding;
                    int y = (int) Math.min(pt1.y, pt2.y) - padding;
                    int width = (int) Math.abs(pt2.x - pt1.x) + 2 * padding;
                    int height = (int) Math.abs(pt2.y - pt1.y) + 2 * padding;
                    
                    // 确保矩形在图像范围内
                    x = Math.max(0, x);
                    y = Math.max(0, y);
                    width = Math.min(width, image.cols() - x);
                    height = Math.min(height, image.rows() - y);
                    
                    if (width > 0 && height > 0) {
                        Rect lineRect = new Rect(x, y, width, height);
                        
                        // 避免重复添加（与已有瑕疵区域重叠）
                        boolean overlapped = false;
                        for (Rect rect : defectRects) {
                            if (isOverlapping(rect, lineRect)) {
                                overlapped = true;
                                break;
                            }
                        }
                        
                        if (!overlapped) {
                            defectRects.add(lineRect);
                            defectTypes.add("断线");
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 检查两个矩形是否重叠
     * 
     * @param rect1 第一个矩形
     * @param rect2 第二个矩形
     * @return 是否重叠
     */
    private boolean isOverlapping(Rect rect1, Rect rect2) {
        return (rect1.x < rect2.x + rect2.width &&
                rect1.x + rect1.width > rect2.x &&
                rect1.y < rect2.y + rect2.height &&
                rect1.y + rect1.height > rect2.y);
    }
    
    /**
     * 统计特定类型瑕疵的数量
     * 
     * @param types 瑕疵类型列表
     * @param targetType 目标类型
     * @return 数量
     */
    private int countDefectsByType(List<String> types, String targetType) {
        int count = 0;
        for (String type : types) {
            if (type.equals(targetType)) {
                count++;
            }
        }
        return count;
    }
    
    // Getter和Setter方法
    
    public Preprocessor getPreprocessor() {
        return preprocessor;
    }
    
    public void setPreprocessor(Preprocessor preprocessor) {
        this.preprocessor = preprocessor;
    }
    
    public int getAdaptiveThresholdBlockSize() {
        return adaptiveThresholdBlockSize;
    }
    
    public void setAdaptiveThresholdBlockSize(int adaptiveThresholdBlockSize) {
        this.adaptiveThresholdBlockSize = adaptiveThresholdBlockSize;
    }
    
    public double getAdaptiveThresholdConstant() {
        return adaptiveThresholdConstant;
    }
    
    public void setAdaptiveThresholdConstant(double adaptiveThresholdConstant) {
        this.adaptiveThresholdConstant = adaptiveThresholdConstant;
    }
    
    public int getMorphologyKernelSize() {
        return morphologyKernelSize;
    }
    
    public void setMorphologyKernelSize(int morphologyKernelSize) {
        this.morphologyKernelSize = morphologyKernelSize;
    }
    
    public double getMinDefectArea() {
        return minDefectArea;
    }
    
    public void setMinDefectArea(double minDefectArea) {
        this.minDefectArea = minDefectArea;
    }
    
    public double getMaxDefectArea() {
        return maxDefectArea;
    }
    
    public void setMaxDefectArea(double maxDefectArea) {
        this.maxDefectArea = maxDefectArea;
    }
} 