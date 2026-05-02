package com.example.fabricdefectdetection.detection;

import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import com.example.fabricdefectdetection.util.ImageUtils;

/**
 * 瑕疵检测结果类
 * 
 * 该类封装了瑕疵检测的结果信息，包括处理后的图像、检测到的瑕疵信息等。
 * 设计为不可变类，确保结果数据的一致性。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DetectionResult {
    
    // 处理后的图像（带有瑕疵标记）
    private final Mat resultImageMat;
    
    // 检测到的瑕疵边界框列表
    private final List<Rect> defectBoundingBoxes;
    
    // 瑕疵类型列表，与边界框列表一一对应
    private final List<String> defectTypes;
    
    // 检测结果描述信息
    private final String message;
    
    // 检测完成的时间戳
    private final Instant timestamp;
    
    // 检测处理耗时（毫秒）
    private final long processingTimeMs;
    
    // 缓存的BufferedImage，用于UI显示
    private BufferedImage resultBufferedImage;
    
    // 图片名称，用于数据库存储
    private String imageName = "未命名图片";
    
    /**
     * 构造函数
     * 
     * @param resultImageMat 处理后的图像
     * @param defectBoundingBoxes 检测到的瑕疵边界框列表
     * @param defectTypes 瑕疵类型列表
     * @param message 结果描述信息
     * @param processingTimeMs 处理耗时
     */
    public DetectionResult(
            Mat resultImageMat,
            List<Rect> defectBoundingBoxes,
            List<String> defectTypes,
            String message,
            long processingTimeMs) {
        
        this.resultImageMat = resultImageMat;
        this.defectBoundingBoxes = Collections.unmodifiableList(defectBoundingBoxes);
        this.defectTypes = Collections.unmodifiableList(defectTypes);
        this.message = message;
        this.timestamp = Instant.now();
        this.processingTimeMs = processingTimeMs;
    }
    
    /**
     * 带图片名称的构造函数
     * 
     * @param resultImageMat 处理后的图像
     * @param defectBoundingBoxes 检测到的瑕疵边界框列表
     * @param defectTypes 瑕疵类型列表
     * @param message 结果描述信息
     * @param processingTimeMs 处理耗时
     * @param imageName 图片名称
     */
    public DetectionResult(
            Mat resultImageMat,
            List<Rect> defectBoundingBoxes,
            List<String> defectTypes,
            String message,
            long processingTimeMs,
            String imageName) {
        
        this(resultImageMat, defectBoundingBoxes, defectTypes, message, processingTimeMs);
        this.imageName = imageName;
    }
    
    /**
     * 简化的构造函数，用于仅有图像和简单信息的情况
     * 
     * @param resultImageMat 处理后的图像
     * @param message 结果描述信息
     */
    public DetectionResult(Mat resultImageMat, String message) {
        this(resultImageMat, 
             Collections.emptyList(), 
             Collections.emptyList(), 
             message,
             0);
    }
    
    /**
     * 获取处理后的图像（Mat格式）
     * 
     * @return Mat对象
     */
    public Mat getResultImageMat() {
        return resultImageMat;
    }
    
    /**
     * 获取处理后的图像（BufferedImage格式，用于UI显示）
     * 
     * @return BufferedImage对象
     */
    public BufferedImage getResultBufferedImage() {
        if (resultBufferedImage == null && resultImageMat != null) {
            // 懒加载，首次调用时才转换
            resultBufferedImage = ImageUtils.matToBufferedImage(resultImageMat);
        }
        return resultBufferedImage;
    }
    
    /**
     * 获取检测到的瑕疵边界框列表
     * 
     * @return 不可变的Rect列表
     */
    public List<Rect> getDefectBoundingBoxes() {
        return defectBoundingBoxes;
    }
    
    /**
     * 获取检测到的瑕疵边界框列表（别名方法，与getDefectBoundingBoxes功能相同）
     * 
     * @return 不可变的Rect列表
     */
    public List<Rect> getDefectRects() {
        return defectBoundingBoxes;
    }
    
    /**
     * 获取瑕疵类型列表
     * 
     * @return 不可变的类型字符串列表
     */
    public List<String> getDefectTypes() {
        return defectTypes;
    }
    
    /**
     * 获取检测结果描述信息
     * 
     * @return 描述信息
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * 获取检测完成的时间戳
     * 
     * @return 时间戳
     */
    public Instant getTimestamp() {
        return timestamp;
    }
    
    /**
     * 获取检测处理耗时
     * 
     * @return 处理耗时（毫秒）
     */
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    /**
     * 获取检测到的瑕疵总数
     * 
     * @return 瑕疵总数
     */
    public int getDefectCount() {
        return defectBoundingBoxes.size();
    }
    
    /**
     * 获取特定类型瑕疵的数量
     * 
     * @param type 瑕疵类型
     * @return 该类型的瑕疵数量
     */
    public int getDefectCountByType(String type) {
        int count = 0;
        for (String defectType : defectTypes) {
            if (defectType.equals(type)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * 获取所有类型瑕疵的数量统计
     * 
     * @return 类型-数量映射
     */
    public Map<String, Integer> getDefectTypeCounts() {
        Map<String, Integer> counts = new HashMap<>();
        
        for (String type : defectTypes) {
            counts.put(type, counts.getOrDefault(type, 0) + 1);
        }
        
        return counts;
    }
    
    /**
     * 获取图片名称
     * 
     * @return 图片名称
     */
    public String getImageName() {
        return imageName;
    }
    
    /**
     * 设置图片名称
     * 
     * @param imageName 图片名称
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
    
    /**
     * 返回检测结果的文本摘要
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("检测结果 [时间: ").append(timestamp).append("]\n");
        sb.append("图片: ").append(imageName).append("\n");
        sb.append("共检测到 ").append(getDefectCount()).append(" 个瑕疵\n");
        
        Map<String, Integer> typeCounts = getDefectTypeCounts();
        for (Map.Entry<String, Integer> entry : typeCounts.entrySet()) {
            sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append(" 个\n");
        }
        
        sb.append("处理耗时: ").append(processingTimeMs).append("ms\n");
        sb.append("描述: ").append(message);
        
        return sb.toString();
    }
} 