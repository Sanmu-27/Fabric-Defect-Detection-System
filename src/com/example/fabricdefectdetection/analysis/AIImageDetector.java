package com.example.fabricdefectdetection.analysis;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.example.fabricdefectdetection.detection.DetectionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI图像检测器
 * 
 * 使用图像和语言AI模型直接从图像中检测布匹缺陷
 */
public class AIImageDetector {
    
    // 硬编码API配置
    private static final String API_KEY = "sk-hYScYPoDDPlPQUzlAbD6B5B502894dD09d41DaB5Ff836c18";
    private static final String BASE_URL = "https://vip.apiyi.com/v1";
    private static final String MODEL_NAME = "deepseek-chat";
    private static final Duration TIMEOUT = Duration.parse("PT180S");
    
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数
     */
    public AIImageDetector() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
        
        System.out.println("AI图像检测器初始化完成");
    }
    
    /**
     * 使用AI检测图像中的缺陷
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
            // 1. 将图像转换为Base64
            String base64Image = matToBase64(originalImage);
            if (base64Image == null) {
                throw new Exception("图像转换失败");
            }
            
            // 2. 构建提示词
            String prompt = buildPrompt(base64Image);
            
            // 3. 调用AI API
            System.out.println("正在调用AI分析图像...");
            String response = callApi(prompt);
            System.out.println("AI响应: " + response);
            
            // 4. 解析AI返回的缺陷描述
            List<DefectInfo> defects = parseAIResponse(response);
            System.out.println("解析出 " + defects.size() + " 个缺陷");
            
            // 5. 绘制检测结果
            Mat resultImage = drawDefects(originalImage, defects);
            
            // 6. 提取检测结果信息
            List<Rect> defectRects = new ArrayList<>();
            List<String> defectTypes = new ArrayList<>();
            
            for (DefectInfo defect : defects) {
                defectRects.add(defect.rect);
                defectTypes.add(defect.type);
            }
            
            // 7. 创建检测结果
            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            
            String message = String.format("AI检测到 %d 个缺陷", defects.size());
            
            return new DetectionResult(
                resultImage,
                defectRects,
                defectTypes,
                message,
                processingTime
            );
            
        } catch (Exception e) {
            System.err.println("AI检测过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            
            // 出错时返回原图和错误信息
            long endTime = System.currentTimeMillis();
            return new DetectionResult(
                originalImage.clone(),
                "AI检测过程中发生错误: " + e.getMessage()
            );
        }
    }
    
    /**
     * 将Mat图像转换为Base64编码字符串
     */
    private String matToBase64(Mat mat) {
        try {
            // 将Mat转换为字节数组
            MatOfByte matOfByte = new MatOfByte();
            Imgcodecs.imencode(".jpg", mat, matOfByte);
            byte[] imageBytes = matOfByte.toArray();
            
            // 进行Base64编码
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            System.err.println("图像转换失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 构建API请求提示词
     */
    private String buildPrompt(String base64Image) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("我有一张布匹图像需要检测缺陷。请分析图像中是否存在以下类型的缺陷：破洞、污渍、断线等。\n");
        sb.append("如果发现缺陷，请给出每个缺陷的类型、位置和大小。格式如下：\n");
        sb.append("缺陷1：类型=污渍，位置=(x,y)，大小=宽x高\n");
        sb.append("缺陷2：类型=破洞，位置=(x,y)，大小=宽x高\n");
        sb.append("...\n\n");
        sb.append("请只输出缺陷信息，不要其他解释。如果没有检测到缺陷，请输出\"未检测到缺陷\"。\n\n");
        
        // 添加图像数据
        sb.append("图像数据 (base64): ");
        sb.append(base64Image);
        
        return sb.toString();
    }
    
    /**
     * 调用AI API
     */
    private String callApi(String prompt) throws Exception {
        String requestBody = String.format(
                "{\"model\": \"%s\", \"messages\": ["
                + "{\"role\": \"system\", \"content\": \"你是一个专业的布匹缺陷检测专家，擅长从图像中识别各类布匹缺陷。你能精确地识别出图像中的破洞、污渍、断线等缺陷，并给出它们的类型、位置和大小。\"}, "
                + "{\"role\": \"user\", \"content\": \"%s\"}"
                + "], \"max_tokens\": 2000}",
                MODEL_NAME, escapeJson(prompt));
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(TIMEOUT)
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new Exception("API调用失败，状态码: " + response.statusCode() + ", 响应: " + response.body());
        }
        
        // 解析JSON响应
        JsonNode rootNode = objectMapper.readTree(response.body());
        JsonNode choices = rootNode.get("choices");
        
        if (choices != null && choices.isArray() && choices.size() > 0) {
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message != null) {
                JsonNode content = message.get("content");
                if (content != null && content.isTextual()) {
                    return content.asText();
                }
            }
        }
        
        throw new Exception("无法从API响应中提取内容");
    }
    
    /**
     * 解析AI返回的响应，提取缺陷信息
     */
    private List<DefectInfo> parseAIResponse(String response) {
        List<DefectInfo> defects = new ArrayList<>();
        
        // 如果响应表明没有缺陷，则返回空列表
        if (response.contains("未检测到缺陷")) {
            return defects;
        }
        
        // 匹配缺陷描述的正则表达式
        Pattern pattern = Pattern.compile("缺陷\\d+：类型=([^，]+)，位置=\\((\\d+),(\\d+)\\)，大小=(\\d+)x(\\d+)");
        Matcher matcher = pattern.matcher(response);
        
        // 查找所有匹配项
        while (matcher.find()) {
            String type = matcher.group(1);
            int x = Integer.parseInt(matcher.group(2));
            int y = Integer.parseInt(matcher.group(3));
            int width = Integer.parseInt(matcher.group(4));
            int height = Integer.parseInt(matcher.group(5));
            
            // 创建矩形区域
            Rect rect = new Rect(x, y, width, height);
            
            // 添加到缺陷列表
            defects.add(new DefectInfo(type, rect));
        }
        
        return defects;
    }
    
    /**
     * 在图像上绘制缺陷标记
     */
    private Mat drawDefects(Mat originalImage, List<DefectInfo> defects) {
        Mat resultImage = originalImage.clone();
        
        // 为每个缺陷绘制矩形框和标签
        for (int i = 0; i < defects.size(); i++) {
            DefectInfo defect = defects.get(i);
            Rect rect = defect.rect;
            String type = defect.type;
            
            // 根据缺陷类型选择颜色
            Scalar color;
            if ("破洞".equals(type)) {
                color = new Scalar(255, 0, 0); // 蓝色（原来是红色）
            } else if ("污渍".equals(type)) {
                color = new Scalar(0, 0, 255); // 红色（原来是蓝色）
            } else if ("断线".equals(type)) {
                color = new Scalar(0, 255, 255); // 黄色
            } else {
                color = new Scalar(0, 255, 0); // 绿色
            }
            
            // 绘制矩形框
            Imgproc.rectangle(resultImage, rect.tl(), rect.br(), color, 2);
            
            // 绘制标签
            String label = (i + 1) + ": " + type;
            Point labelPos = new Point(rect.x, rect.y - 5);
            Imgproc.putText(resultImage, label, labelPos, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, color, 1);
        }
        
        return resultImage;
    }
    
    /**
     * 对JSON特殊字符进行转义
     */
    protected String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
    
    /**
     * 缺陷信息类，用于存储缺陷的类型和位置
     */
    private static class DefectInfo {
        final String type;
        final Rect rect;
        
        DefectInfo(String type, Rect rect) {
            this.type = type;
            this.rect = rect;
        }
    }
} 