package com.example.fabricdefectdetection.detection;

/**
 * 批量处理任务结果类
 * 
 * 用于存储批量处理任务的状态和结果信息
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class BatchTaskResult {
    
    public enum Status {
        WAITING("等待中"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        FAILED("失败");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private final String fileName;
    private Status status;
    private int defectCount;
    private String qualityLevel;
    private String errorMessage;
    
    /**
     * 创建一个新的批处理任务结果
     * 
     * @param fileName 文件名
     */
    public BatchTaskResult(String fileName) {
        this.fileName = fileName;
        this.status = Status.WAITING;
        this.defectCount = 0;
        this.qualityLevel = "-";
        this.errorMessage = null;
    }
    
    /**
     * 获取文件名
     * 
     * @return 文件名
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * 获取任务状态
     * 
     * @return 任务状态
     */
    public Status getStatus() {
        return status;
    }
    
    /**
     * 获取状态显示名称
     * 
     * @return 状态显示名称
     */
    public String getStatusDisplayName() {
        return status.getDisplayName();
    }
    
    /**
     * 设置任务状态
     * 
     * @param status 任务状态
     */
    public void setStatus(Status status) {
        this.status = status;
    }
    
    /**
     * 获取缺陷数量
     * 
     * @return 缺陷数量
     */
    public int getDefectCount() {
        return defectCount;
    }
    
    /**
     * 设置缺陷数量
     * 
     * @param defectCount 缺陷数量
     */
    public void setDefectCount(int defectCount) {
        this.defectCount = defectCount;
    }
    
    /**
     * 获取质量等级
     * 
     * @return 质量等级
     */
    public String getQualityLevel() {
        return qualityLevel;
    }
    
    /**
     * 设置质量等级
     * 
     * @param qualityLevel 质量等级
     */
    public void setQualityLevel(String qualityLevel) {
        this.qualityLevel = qualityLevel;
    }
    
    /**
     * 获取错误信息
     * 
     * @return 错误信息
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * 设置错误信息
     * 
     * @param errorMessage 错误信息
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    /**
     * 任务是否成功完成
     * 
     * @return 是否成功完成
     */
    public boolean isCompleted() {
        return status == Status.COMPLETED;
    }
    
    /**
     * 任务是否失败
     * 
     * @return 是否失败
     */
    public boolean isFailed() {
        return status == Status.FAILED;
    }
} 