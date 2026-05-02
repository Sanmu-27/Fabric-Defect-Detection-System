package com.example.fabricdefectdetection.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Rect;

import com.example.fabricdefectdetection.detection.DefectType;
import com.example.fabricdefectdetection.detection.DetectionResult;
import com.example.fabricdefectdetection.database.DetectionResultDAO;
import com.example.fabricdefectdetection.util.ConfigurationManager;

/**
 * 布匹缺陷分析助手
 * 
 * 利用大语言模型API对布匹缺陷检测结果进行智能分析，提供专业建议
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DefectAnalysisAssistant {

    private static final Logger logger = Logger.getLogger(DefectAnalysisAssistant.class.getName());
    private static final String PROPERTIES_FILE = "application.properties";
    
    // 硬编码API密钥和配置，不再依赖配置文件
    private static final String HARDCODED_API_KEY = "sk-hYScYPoDDPlPQUzlAbD6B5B502894dD09d41DaB5Ff836c18";
    private static final String HARDCODED_BASE_URL = "https://vip.apiyi.com/v1";
    private static final String HARDCODED_MODEL_NAME = "deepseek-chat";
    private static final String HARDCODED_TIMEOUT = "PT60S";
    
    private final String apiKey;
    private final String baseUrl;
    private final String modelName;
    private final Duration timeout;
    private final HttpClient client;
    private boolean apiAvailable = false;

    // 添加DAO对象
    private final DetectionResultDAO resultDAO;

    /**
     * 构造函数，初始化AI分析助手
     */
    public DefectAnalysisAssistant() {
        Properties props = loadProperties();
        
        // 优先使用硬编码API密钥
        logger.info("使用硬编码API配置...");
        this.apiKey = HARDCODED_API_KEY;
        this.baseUrl = HARDCODED_BASE_URL;
        this.modelName = HARDCODED_MODEL_NAME;
        
        String timeoutStr = HARDCODED_TIMEOUT;
        this.timeout = Duration.parse(timeoutStr);
        
        // 检查API密钥
        logger.info("API密钥: " + (apiKey.length() > 10 ? apiKey.substring(0, 5) + "..." : apiKey));
        if (apiKey == null || apiKey.isEmpty() || "YOUR_DEEPSEEK_API_KEY".equals(apiKey)) {
            logger.warning("提示: DeepSeek API 密钥未配置，AI分析功能将不可用。");
            apiAvailable = false;
        } else {
            apiAvailable = true;
            logger.info("API密钥有效，长度: " + apiKey.length() + "字符");
        }
        
        // 创建HttpClient
        this.client = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
        
        // 初始化DAO
        this.resultDAO = new DetectionResultDAO();
        
        logger.info("布匹缺陷分析助手初始化完成" + (apiAvailable ? "，AI分析功能已启用" : "，AI分析功能未启用"));
    }
    
    /**
     * 分析检测结果，提供专业建议
     * 
     * @param result 检测结果
     * @return 分析报告
     */
    public DefectAnalysisReport analyzeDefects(DetectionResult result) {
        if (!apiAvailable) {
            DefectAnalysisReport basicReport = generateBasicReport(result);
            
            // 保存到数据库
            saveReportToDatabase(basicReport, result);
            
            // 保存到文件
            saveReportToFile(basicReport, result);
            
            return basicReport;
        }
        
        try {
            // 构建检测结果描述
            String defectsDescription = buildDefectsDescription(result);
            logger.info("准备发送检测结果到AI助手分析...");
            
            // 构建系统提示
            String systemPrompt = "你是一个专业的布匹质量控制专家。你需要分析布匹缺陷检测结果，并提供专业的分析和建议。"
                    + "请根据检测到的缺陷类型、数量、位置和大小，判断布匹的总体质量，分析可能的原因，并给出改进建议。"
                    + "分析应该包括：1. 缺陷统计和分布情况 2. 缺陷成因分析 3. 质量等级评估 4. 改进建议。"
                    + "回答要专业、有条理，使用纺织行业的术语。所有回答需要用中文。";
            
            // 构建请求体
            String requestBody = String.format(
                    "{\"model\": \"%s\", \"messages\": ["
                    + "{\"role\": \"system\", \"content\": \"%s\"}, "
                    + "{\"role\": \"user\", \"content\": \"%s\"}"
                    + "]}",
                    modelName, escapeJson(systemPrompt), escapeJson(defectsDescription));
            
            String analysisContent = callApi(requestBody);
            
            // 创建分析报告
            DefectAnalysisReport report = new DefectAnalysisReport(
                result.getDefectCount(),
                analysisContent,
                evaluateQualityLevel(result),
                true
            );
            
            // 保存到数据库
            saveReportToDatabase(report, result);
            
            // 保存到文件
            saveReportToFile(report, result);
            
            return report;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "分析缺陷时发生错误", e);
            DefectAnalysisReport fallbackReport = generateBasicReport(result);
            
            // 保存到数据库
            saveReportToDatabase(fallbackReport, result);
            
            // 保存到文件
            saveReportToFile(fallbackReport, result);
            
            return fallbackReport;
        }
    }
    
    /**
     * 生成基本报告（当AI分析不可用时）
     */
    private DefectAnalysisReport generateBasicReport(DetectionResult result) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("## 布匹缺陷检测基本报告\n\n");
        analysis.append("### 缺陷统计\n");
        analysis.append("- 总缺陷数量: ").append(result.getDefectCount()).append("\n");
        
        if (result.getDefectCount() > 0) {
            analysis.append("- 缺陷类型分布:\n");
            
            // 统计各类型缺陷
            int holes = 0;
            int stains = 0;
            int brokenThreads = 0;
            int others = 0;
            
            List<String> defectTypes = result.getDefectTypes();
            for (String type : defectTypes) {
                if ("破洞".equals(type)) {
                    holes++;
                } else if ("污渍".equals(type)) {
                    stains++;
                } else if ("断线".equals(type)) {
                    brokenThreads++;
                } else {
                    others++;
                }
            }
            
            analysis.append("  - 破洞: ").append(holes).append("\n");
            analysis.append("  - 污渍: ").append(stains).append("\n");
            analysis.append("  - 断线: ").append(brokenThreads).append("\n");
            if (others > 0) {
                analysis.append("  - 其他: ").append(others).append("\n");
            }
        }
        
        analysis.append("\n### 质量评估\n");
        String qualityLevel = evaluateQualityLevel(result);
        analysis.append("- 质量等级: ").append(qualityLevel).append("\n");
        analysis.append("\n注: 此为基本报告。启用AI分析功能可获得更详细的专业分析。");
        
        return new DefectAnalysisReport(
            result.getDefectCount(),
            analysis.toString(),
            qualityLevel,
            false
        );
    }
    
    /**
     * 评估布匹质量等级
     */
    private String evaluateQualityLevel(DetectionResult result) {
        int defectCount = result.getDefectCount();
        if (defectCount == 0) {
            return "优等品";
        } else if (defectCount <= 2) {
            return "一等品";
        } else if (defectCount <= 5) {
            return "二等品";
        } else {
            return "不合格品";
        }
    }
    
    /**
     * 构建缺陷描述文本
     */
    private String buildDefectsDescription(DetectionResult result) {
        StringBuilder description = new StringBuilder();
        description.append("我有一块布料的缺陷检测结果需要分析。\n\n");
        description.append("检测到的总缺陷数量: ").append(result.getDefectCount()).append("\n");
        
        if (result.getDefectCount() > 0) {
            description.append("缺陷详情:\n");
            
            List<String> defectTypes = result.getDefectTypes();
            List<Rect> defectRects = result.getDefectRects();
            
            for (int i = 0; i < defectTypes.size(); i++) {
                Rect rect = defectRects.get(i);
                String type = defectTypes.get(i);
                description.append("- 缺陷 #").append(i+1).append(": 类型=").append(type)
                           .append(", 位置=(").append(rect.x).append(",").append(rect.y).append(")")
                           .append(", 大小=").append(rect.width).append("x").append(rect.height)
                           .append(", 面积=").append(rect.width * rect.height).append("\n");
            }
        }
        
        description.append("\n请根据这些信息分析布料的质量情况，可能的缺陷原因，以及相应的改进建议。");
        return description.toString();
    }
    
    /**
     * 调用API并处理响应
     * 
     * @param requestBody JSON请求体
     * @return API响应中提取的内容
     * @throws Exception 如果API调用失败
     */
    protected String callApi(String requestBody) throws Exception {
        logger.info("发送API请求...");
        
        // 构建HTTP请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(timeout)
                .build();
        
        // 发送请求
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 检查响应状态
        if (response.statusCode() != 200) {
            logger.log(Level.WARNING, "API返回错误状态码: " + response.statusCode());
            return "无法获取AI分析。错误代码: " + response.statusCode();
        }
        
        // 解析响应
        String responseBody = response.body();
        
        // 手动解析JSON响应，提取content字段
        try {
            // 先找到choices数组的第一个元素
            int choicesIdx = responseBody.indexOf("\"choices\"");
            if (choicesIdx != -1) {
                // 找到message对象
                int messageIdx = responseBody.indexOf("\"message\"", choicesIdx);
                if (messageIdx != -1) {
                    // 找到content字段
                    int contentIdx = responseBody.indexOf("\"content\"", messageIdx);
                    if (contentIdx != -1) {
                        // 提取content的值
                        int valueStart = responseBody.indexOf("\"", contentIdx + "\"content\"".length()) + 1;
                        int valueEnd = responseBody.indexOf("\"", valueStart);
                        if (valueStart != -1 && valueEnd != -1) {
                            return responseBody.substring(valueStart, valueEnd)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");
                        }
                    }
                }
            }
            
            // 如果解析失败，返回整个响应体
            logger.warning("无法解析API响应，返回完整响应");
            return responseBody;
        } catch (Exception e) {
            logger.log(Level.WARNING, "解析API响应失败", e);
            return responseBody;
        }
    }
    
    /**
     * 转义JSON字符串
     */
    protected String escapeJson(String input) {
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    /**
     * 判断AI分析功能是否可用
     */
    public boolean isApiAvailable() {
        return apiAvailable;
    }
    
    /**
     * 加载配置文件
     */
    private Properties loadProperties() {
        Properties properties = new Properties();
        
        // 尝试从多个可能的位置加载配置文件
        String[] possibleLocations = {
            PROPERTIES_FILE,
            "application.properties",
            "../" + PROPERTIES_FILE,
            "classes/" + PROPERTIES_FILE,
            "classes/application.properties",
            "classes/com/example/fabricdefectdetection/resources/" + PROPERTIES_FILE
        };
        
        boolean loaded = false;
        for (String location : possibleLocations) {
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(location)) {
                if (input != null) {
                    properties.load(input);
                    logger.info("成功加载配置文件: " + location);
                    // 调试输出所有属性
                    logger.fine("--- 配置内容 ---");
                    for (String key : properties.stringPropertyNames()) {
                        String value = key.contains("api-key") ? 
                                      (properties.getProperty(key).length() > 10 ? 
                                       properties.getProperty(key).substring(0, 5) + "..." : properties.getProperty(key)) 
                                      : properties.getProperty(key);
                        logger.fine(key + " = " + value);
                    }
                    logger.fine("---------------");
                    loaded = true;
                    break;
                }
            } catch (IOException ex) {
                // 继续尝试下一个位置
            }
        }
        
        if (!loaded) {
            // 如果从资源路径加载失败，则尝试从文件系统加载
            for (String location : possibleLocations) {
                try (InputStream input = new java.io.FileInputStream(location)) {
                    properties.load(input);
                    logger.info("从文件系统加载配置文件: " + location);
                    loaded = true;
                    break;
                } catch (IOException ex) {
                    // 继续尝试下一个位置
                }
            }
        }
        
        if (!loaded) {
            logger.warning("警告: 无法加载配置文件 " + PROPERTIES_FILE + "，将使用默认配置");
        }
        
        return properties;
    }

    /**
     * 将分析报告保存到数据库
     * 
     * @param report 要保存的分析报告
     * @param result 检测结果对象，包含图片名称
     */
    private void saveReportToDatabase(DefectAnalysisReport report, DetectionResult result) {
        try {
            // 使用DAO保存到数据库
            resultDAO.saveResult(report, result.getImageName());
            logger.info("分析报告已保存到数据库: " + result.getImageName());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "保存分析报告到数据库失败", e);
        }
    }

    /**
     * 将分析报告保存到文件
     * 
     * @param report 要保存的分析报告
     * @param result 检测结果对象，包含图片名称
     */
    private void saveReportToFile(DefectAnalysisReport report, DetectionResult result) {
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
} 