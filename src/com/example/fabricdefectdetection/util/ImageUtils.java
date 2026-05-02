package com.example.fabricdefectdetection.util;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * 图像工具类
 * 
 * 提供图像处理相关的静态辅助方法，例如图像加载、保存、转换等。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public final class ImageUtils {
    
    /**
     * 私有构造函数，防止实例化
     */
    private ImageUtils() {
        throw new AssertionError("工具类不应被实例化");
    }
    
    /**
     * 从文件加载图像到Mat对象
     * 
     * @param path 图像文件路径
     * @return Mat对象
     * @throws IOException 如果图像加载失败
     */
    public static Mat loadImage(String path) throws IOException {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("路径不能为空");
        }
        
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("文件不存在或不是有效文件: " + path);
        }
        
        Mat image = Imgcodecs.imread(path);
        if (image.empty()) {
            throw new IOException("无法读取图像文件: " + path);
        }
        
        return image;
    }
    
    /**
     * 将Mat对象保存为图像文件
     * 
     * @param image Mat对象
     * @param path 保存路径
     * @return 是否保存成功
     */
    public static boolean saveImage(Mat image, String path) {
        if (image == null || image.empty()) {
            return false;
        }
        
        if (path == null || path.isEmpty()) {
            return false;
        }
        
        return Imgcodecs.imwrite(path, image);
    }
    
    /**
     * 将OpenCV的Mat对象转换为Java的BufferedImage
     * 
     * @param mat 待转换的Mat对象
     * @return 转换后的BufferedImage
     */
    public static BufferedImage matToBufferedImage(Mat mat) {
        if (mat == null || mat.empty()) {
            return null;
        }
        
        // 确定图像类型
        int type;
        if (mat.channels() == 1) {
            type = BufferedImage.TYPE_BYTE_GRAY;
        } else {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        
        // 确保Mat是连续的
        Mat convertedMat;
        if (!mat.isContinuous()) {
            convertedMat = mat.clone();
        } else {
            convertedMat = mat;
        }
        
        // 创建BufferedImage
        int width = convertedMat.cols();
        int height = convertedMat.rows();
        BufferedImage image = new BufferedImage(width, height, type);
        
        // 获取图像数据
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        convertedMat.get(0, 0, data);
        
        return image;
    }
    
    /**
     * 将Java的BufferedImage转换为OpenCV的Mat对象
     * 
     * @param image 待转换的BufferedImage
     * @return 转换后的Mat对象
     */
    public static Mat bufferedImageToMat(BufferedImage image) {
        if (image == null) {
            return null;
        }
        
        // 获取图像数据
        byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        
        // 创建Mat对象
        Mat mat;
        if (image.getType() == BufferedImage.TYPE_BYTE_GRAY) {
            mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        } else {
            mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
        }
        
        // 填充数据
        mat.put(0, 0, data);
        
        return mat;
    }
    
    /**
     * 调整图像大小
     * 
     * @param src 源图像
     * @param width 目标宽度
     * @param height 目标高度
     * @return 调整大小后的图像
     */
    public static Mat resizeImage(Mat src, int width, int height) {
        if (src == null || src.empty()) {
            return null;
        }
        
        Mat resized = new Mat();
        Imgproc.resize(src, resized, new org.opencv.core.Size(width, height));
        
        return resized;
    }
    
    /**
     * 裁剪图像
     * 
     * @param src 源图像
     * @param roi 感兴趣区域（裁剪区域）
     * @return 裁剪后的图像
     */
    public static Mat cropImage(Mat src, Rect roi) {
        if (src == null || src.empty()) {
            return null;
        }
        
        // 检查ROI是否在图像范围内
        if (roi.x < 0 || roi.y < 0 || 
            roi.x + roi.width > src.cols() || 
            roi.y + roi.height > src.rows()) {
            throw new IllegalArgumentException("ROI超出图像范围");
        }
        
        return new Mat(src, roi);
    }
    
    /**
     * 在图像上绘制检测结果
     * 
     * @param image 源图像
     * @param defects 瑕疵区域列表
     * @param types 瑕疵类型列表
     * @return 标记了检测结果的图像
     */
    public static Mat drawDetectionResults(Mat image, List<Rect> defects, List<String> types) {
        if (image == null || image.empty() || defects == null || defects.isEmpty()) {
            return image;
        }
        
        // 创建副本，避免修改原始图像
        Mat result = image.clone();
        
        // 绘制每个检测到的瑕疵
        for (int i = 0; i < defects.size(); i++) {
            Rect rect = defects.get(i);
            
            // 获取瑕疵类型和颜色
            String type = (types != null && i < types.size()) ? types.get(i) : "未知";
            Scalar color;
            
            // 根据类型设置不同的颜色
            switch (type) {
                case "破洞":
                    color = new Scalar(0, 0, 255); // 红色
                    break;
                case "污渍":
                    color = new Scalar(0, 255, 255); // 黄色
                    break;
                case "断线":
                    color = new Scalar(255, 0, 0); // 蓝色
                    break;
                default:
                    color = new Scalar(255, 255, 255); // 白色
            }
            
            // 绘制矩形框
            Imgproc.rectangle(result, rect.tl(), rect.br(), color, 2);
            
            // 绘制类型文本
            Imgproc.putText(
                result,
                type,
                new org.opencv.core.Point(rect.x, rect.y - 5),
                Imgproc.FONT_HERSHEY_SIMPLEX,
                0.5,
                color,
                1
            );
        }
        
        return result;
    }
    
    /**
     * 创建缩略图
     * 
     * @param image 源图像
     * @param maxDimension 最大维度（宽或高）
     * @return 缩略图
     */
    public static Mat createThumbnail(Mat image, int maxDimension) {
        if (image == null || image.empty() || maxDimension <= 0) {
            return null;
        }
        
        // 计算缩放比例
        double scale;
        if (image.width() > image.height()) {
            scale = (double) maxDimension / image.width();
        } else {
            scale = (double) maxDimension / image.height();
        }
        
        // 计算新尺寸
        int newWidth = (int) (image.width() * scale);
        int newHeight = (int) (image.height() * scale);
        
        // 调整大小
        return resizeImage(image, newWidth, newHeight);
    }
    
    /**
     * 生成用于可视化的颜色映射
     * 
     * @param defectCount 瑕疵数量，用于生成不同的颜色
     * @return 颜色数组
     */
    public static Scalar[] generateColorMap(int defectCount) {
        if (defectCount <= 0) {
            return new Scalar[0];
        }
        
        Scalar[] colors = new Scalar[defectCount];
        
        // 使用HSV色彩空间，均匀分布色调
        for (int i = 0; i < defectCount; i++) {
            double hue = 180.0 * i / defectCount;
            
            // 创建颜色对象（BGR格式）
            Mat hsvColor = new Mat(1, 1, CvType.CV_8UC3, new Scalar(hue, 255, 255));
            Mat bgrColor = new Mat();
            Imgproc.cvtColor(hsvColor, bgrColor, Imgproc.COLOR_HSV2BGR);
            
            double[] color = bgrColor.get(0, 0);
            colors[i] = new Scalar(color[0], color[1], color[2]);
        }
        
        return colors;
    }
} 