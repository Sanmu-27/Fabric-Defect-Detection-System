package com.example.fabricdefectdetection.detection;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.example.fabricdefectdetection.analysis.DefectAnalysisAssistant;
import com.example.fabricdefectdetection.analysis.DefectAnalysisReport;
import com.example.fabricdefectdetection.database.DetectionResultDAO;
import com.example.fabricdefectdetection.detection.BatchTaskResult.Status;

/**
 * 批量处理器
 * 
 * 用于批量处理图片文件，进行缺陷检测和分析
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class BatchProcessor {
    
    private static final Logger logger = Logger.getLogger(BatchProcessor.class.getName());
    private static final String[] SUPPORTED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".bmp", ".tif", ".tiff"};
    
    private final ExecutorService executorService;
    private final DefectDetector defectDetector;
    private final DefectAnalysisAssistant analysisAssistant;
    private final DetectionResultDAO resultDAO;
    
    private boolean isProcessing = false;
    private List<BatchTaskResult> taskResults = new ArrayList<>();
    
    /**
     * 构造函数，初始化批处理器
     * 
     * @param threadCount 线程数量
     */
    public BatchProcessor(int threadCount) {
        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.defectDetector = new DefectDetector();
        this.analysisAssistant = new DefectAnalysisAssistant();
        this.resultDAO = new DetectionResultDAO();
        
        logger.info("批处理器初始化完成，线程池大小: " + threadCount);
    }
    
    /**
     * 构造函数，使用默认线程数
     */
    public BatchProcessor() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * 开始批量处理
     * 
     * @param directory 包含图片的目录
     * @param onProgressUpdate 进度更新回调
     * @return 任务结果列表
     */
    public List<BatchTaskResult> startProcessing(File directory, Consumer<BatchTaskResult> onProgressUpdate) {
        if (isProcessing) {
            logger.warning("已有批处理任务正在运行，请等待完成后再试");
            return null;
        }
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            logger.warning("指定的目录不存在或不是一个目录: " + directory);
            return null;
        }
        
        isProcessing = true;
        taskResults.clear();
        
        // 获取所有支持的图片文件
        File[] imageFiles = directory.listFiles(file -> {
            if (file.isFile()) {
                String fileName = file.getName().toLowerCase();
                for (String ext : SUPPORTED_EXTENSIONS) {
                    if (fileName.endsWith(ext)) {
                        return true;
                    }
                }
            }
            return false;
        });
        
        if (imageFiles == null || imageFiles.length == 0) {
            logger.warning("指定的目录中没有找到支持的图片文件: " + directory);
            isProcessing = false;
            return taskResults;
        }
        
        logger.info("开始批量处理 " + imageFiles.length + " 个图片文件...");
        
        // 创建任务结果并提交任务
        Arrays.stream(imageFiles).forEach(file -> {
            BatchTaskResult result = new BatchTaskResult(file.getName());
            taskResults.add(result);
            
            // 通知UI更新
            if (onProgressUpdate != null) {
                SwingUtilities.invokeLater(() -> onProgressUpdate.accept(result));
            }
            
            // 提交任务到线程池
            executorService.submit(() -> processImageFile(file, result, onProgressUpdate));
        });
        
        return taskResults;
    }
    
    /**
     * 处理单个图片文件
     * 
     * @param file 图片文件
     * @param result 任务结果
     * @param onProgressUpdate 进度更新回调
     */
    private void processImageFile(File file, BatchTaskResult result, Consumer<BatchTaskResult> onProgressUpdate) {
        try {
            // 更新状态为处理中
            result.setStatus(Status.PROCESSING);
            updateUI(result, onProgressUpdate);
            
            // 读取图片
            logger.info("处理图片: " + file.getName());
            Mat image = Imgcodecs.imread(file.getAbsolutePath());
            if (image.empty()) {
                throw new IOException("无法读取图片: " + file.getName());
            }
            
            // 预处理和检测
            Preprocessor preprocessor = new Preprocessor();
            Mat preprocessedImage = preprocessor.preprocess(image);
            DetectionResult detectionResult = defectDetector.detectDefects(preprocessedImage);
            detectionResult.setImageName(file.getName());
            
            // 分析结果
            DefectAnalysisReport report = analysisAssistant.analyzeDefects(detectionResult);
            
            // 保存到数据库
            resultDAO.saveResult(report, file.getName());
            
            // 保存到文件
            saveReportToFile(report);
            
            // 更新结果
            result.setDefectCount(detectionResult.getDefectCount());
            result.setQualityLevel(report.getQualityLevel());
            result.setStatus(Status.COMPLETED);
            
            // 释放OpenCV资源
            image.release();
            preprocessedImage.release();
            
            logger.info("完成处理: " + file.getName() + ", 缺陷数: " + detectionResult.getDefectCount());
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "处理图片时发生错误: " + file.getName(), e);
            result.setStatus(Status.FAILED);
            result.setErrorMessage(e.getMessage());
        } finally {
            updateUI(result, onProgressUpdate);
            
            // 检查是否所有任务都已完成
            checkAllTasksCompleted();
        }
    }
    
    /**
     * 将分析报告保存到文件
     * 
     * @param report 要保存的分析报告
     */
    private void saveReportToFile(DefectAnalysisReport report) {
        try {
            // 确保reports目录存在
            File reportsDir = new File("reports");
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
            }
            
            // 创建文件名，格式：report_日期时间_质量等级.md
            String timestamp = java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = String.format("reports/report_%s_%s.md", timestamp, report.getQualityLevel());
            
            // 写入报告内容
            try (java.io.FileWriter writer = new java.io.FileWriter(fileName)) {
                writer.write(report.getMarkdownReport());
            }
            
            logger.info("分析报告已保存到文件: " + fileName);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "保存分析报告到文件失败", e);
        }
    }
    
    /**
     * 更新UI
     * 
     * @param result 任务结果
     * @param onProgressUpdate 进度更新回调
     */
    private void updateUI(BatchTaskResult result, Consumer<BatchTaskResult> onProgressUpdate) {
        if (onProgressUpdate != null) {
            SwingUtilities.invokeLater(() -> onProgressUpdate.accept(result));
        }
    }
    
    /**
     * 检查是否所有任务都已完成
     */
    private synchronized void checkAllTasksCompleted() {
        boolean allCompleted = taskResults.stream()
                .allMatch(result -> result.isCompleted() || result.isFailed());
        
        if (allCompleted) {
            isProcessing = false;
            logger.info("所有批处理任务已完成");
        }
    }
    
    /**
     * 关闭批处理器
     */
    public void shutdown() {
        logger.info("关闭批处理器...");
        executorService.shutdown();
    }
    
    /**
     * 获取所有任务结果
     * 
     * @return 任务结果列表
     */
    public List<BatchTaskResult> getTaskResults() {
        return taskResults;
    }
    
    /**
     * 是否正在处理
     * 
     * @return 是否正在处理
     */
    public boolean isProcessing() {
        return isProcessing;
    }
} 