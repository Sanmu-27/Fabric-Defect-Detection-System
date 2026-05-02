package com.example.fabricdefectdetection.analysis;

/**
 * 布匹缺陷分析报告
 * 
 * 封装了布匹缺陷分析的结果，包括缺陷数量、质量等级和详细分析内容
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DefectAnalysisReport {
    
    private final int defectCount;
    private final String analysisContent;
    private final String qualityLevel;
    private final boolean aiEnhanced;
    
    /**
     * 构造函数
     * 
     * @param defectCount 缺陷数量
     * @param analysisContent 分析内容
     * @param qualityLevel 质量等级
     * @param aiEnhanced 是否使用AI增强分析
     */
    public DefectAnalysisReport(int defectCount, String analysisContent, String qualityLevel, boolean aiEnhanced) {
        this.defectCount = defectCount;
        this.analysisContent = analysisContent;
        this.qualityLevel = qualityLevel;
        this.aiEnhanced = aiEnhanced;
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
     * 获取分析内容
     * 
     * @return 分析内容文本
     */
    public String getAnalysisContent() {
        return analysisContent;
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
     * 判断是否使用AI增强分析
     * 
     * @return 是否使用AI增强分析
     */
    public boolean isAiEnhanced() {
        return aiEnhanced;
    }
    
    /**
     * 获取摘要信息
     * 
     * @return 摘要信息
     */
    public String getSummary() {
        return String.format("检测到%d个缺陷，质量等级：%s", defectCount, qualityLevel);
    }
    
    /**
     * 获取完整的Markdown格式报告
     * 
     * @return 完整的Markdown报告内容
     */
    public String getMarkdownReport() {
        StringBuilder markdown = new StringBuilder();
        
        // 添加标题
        markdown.append("# 布匹缺陷分析报告\n\n");
        
        // 添加元数据
        markdown.append("## 报告元数据\n\n");
        markdown.append("- **生成时间**: ").append(java.time.LocalDateTime.now()).append("\n");
        markdown.append("- **缺陷总数**: ").append(defectCount).append("\n");
        markdown.append("- **质量等级**: ").append(qualityLevel).append("\n");
        markdown.append("- **AI增强**: ").append(aiEnhanced ? "是" : "否").append("\n\n");
        
        // 添加分析内容
        markdown.append("## 分析内容\n\n");
        markdown.append(analysisContent);
        
        return markdown.toString();
    }
    
    /**
     * 转换为字符串
     */
    @Override
    public String toString() {
        return analysisContent;
    }
} 