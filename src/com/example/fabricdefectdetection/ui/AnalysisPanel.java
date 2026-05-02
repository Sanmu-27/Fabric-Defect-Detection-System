package com.example.fabricdefectdetection.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.example.fabricdefectdetection.analysis.DefectAnalysisReport;

/**
 * 布匹缺陷分析结果显示面板
 * 
 * 用于展示布匹缺陷分析报告，包括质量等级、缺陷统计和详细分析等信息
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class AnalysisPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private JEditorPane contentPane;
    private DefectAnalysisReport currentReport;
    
    /**
     * 构造函数，初始化分析面板
     */
    public AnalysisPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建富文本编辑器来显示格式化内容
        contentPane = new JEditorPane();
        contentPane.setEditable(false);
        contentPane.setContentType("text/html");
        
        // 设置HTML样式
        HTMLEditorKit kit = new HTMLEditorKit();
        contentPane.setEditorKit(kit);
        
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: Arial, sans-serif; margin: 10px; }");
        styleSheet.addRule("h1 { font-size: 18pt; color: #2c3e50; }");
        styleSheet.addRule("h2 { font-size: 14pt; color: #3498db; }");
        styleSheet.addRule("h3 { font-size: 12pt; color: #e67e22; }");
        styleSheet.addRule("p { font-size: 11pt; }");
        styleSheet.addRule(".summary { font-size: 12pt; background-color: #f8f9fa; padding: 10px; border-left: 4px solid #3498db; }");
        styleSheet.addRule(".quality-high { color: #27ae60; font-weight: bold; }");
        styleSheet.addRule(".quality-medium { color: #f39c12; font-weight: bold; }");
        styleSheet.addRule(".quality-low { color: #e74c3c; font-weight: bold; }");
        styleSheet.addRule(".ai-badge { background-color: #9b59b6; color: white; padding: 2px 6px; border-radius: 3px; font-size: 9pt; }");
        
        // 添加到滚动面板
        JScrollPane scrollPane = new JScrollPane(contentPane);
        add(scrollPane, BorderLayout.CENTER);
        
        // 显示默认内容
        showDefaultContent();
    }
    
    /**
     * 显示默认内容
     */
    private void showDefaultContent() {
        String defaultContent = 
                "<html><body>" +
                "<h1>布匹缺陷分析</h1>" +
                "<p>请先加载布匹图像并运行缺陷检测。完成检测后，分析结果将显示在此处。</p>" +
                "<p>分析结果将包括：</p>" +
                "<ul>" +
                "<li>缺陷统计信息</li>" +
                "<li>布匹质量等级评估</li>" +
                "<li>专业分析意见</li>" +
                "<li>改进建议</li>" +
                "</ul>" +
                "</body></html>";
        
        contentPane.setText(defaultContent);
    }
    
    /**
     * 显示分析报告
     * 
     * @param report 分析报告
     */
    public void displayReport(DefectAnalysisReport report) {
        if (report == null) {
            showDefaultContent();
            return;
        }
        
        this.currentReport = report;
        
        // 确定质量等级样式
        String qualityClass;
        if ("优等品".equals(report.getQualityLevel()) || "一等品".equals(report.getQualityLevel())) {
            qualityClass = "quality-high";
        } else if ("二等品".equals(report.getQualityLevel())) {
            qualityClass = "quality-medium";
        } else {
            qualityClass = "quality-low";
        }
        
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><body>");
        
        // 标题和摘要
        htmlContent.append("<h1>布匹缺陷分析报告");
        if (report.isAiEnhanced()) {
            htmlContent.append(" <span class='ai-badge'>AI增强</span>");
        }
        htmlContent.append("</h1>");
        
        htmlContent.append("<div class='summary'>");
        htmlContent.append("检测到 <b>").append(report.getDefectCount()).append("</b> 个缺陷，");
        htmlContent.append("质量等级: <span class='").append(qualityClass).append("'>");
        htmlContent.append(report.getQualityLevel()).append("</span>");
        htmlContent.append("</div><br/>");
        
        // 转换Markdown风格的内容为HTML
        String content = report.getAnalysisContent();
        content = convertMarkdownHeadingsToHtml(content);
        content = content.replace("\n\n", "<br/><br/>")
                         .replace("\n", "<br/>")
                         .replace("- ", "• ");
        
        htmlContent.append(content);
        htmlContent.append("</body></html>");
        
        contentPane.setText(htmlContent.toString());
        contentPane.setCaretPosition(0);  // 滚动到顶部
    }

    /**
     * 设置并显示分析报告 (MainFrame中使用此方法)
     * 
     * @param report 分析报告
     */
    public void setAnalysisReport(DefectAnalysisReport report) {
        displayReport(report);
    }
    
    /**
     * 转换Markdown风格的标题为HTML标题
     */
    private String convertMarkdownHeadingsToHtml(String text) {
        // 将 ### 标题转换为 <h3>
        text = text.replaceAll("(?m)^### (.+)$", "<h3>$1</h3>");
        
        // 将 ## 标题转换为 <h2>
        text = text.replaceAll("(?m)^## (.+)$", "<h2>$1</h2>");
        
        // 将 # 标题转换为 <h1>
        text = text.replaceAll("(?m)^# (.+)$", "<h1>$1</h1>");
        
        return text;
    }
    
    /**
     * 获取当前报告
     * 
     * @return 当前显示的分析报告
     */
    public DefectAnalysisReport getCurrentReport() {
        return currentReport;
    }
    
    /**
     * 清除当前报告
     */
    public void clearReport() {
        this.currentReport = null;
        showDefaultContent();
    }
} 