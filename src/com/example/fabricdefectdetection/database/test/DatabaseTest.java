package com.example.fabricdefectdetection.database.test;

import com.example.fabricdefectdetection.analysis.DefectAnalysisReport;
import com.example.fabricdefectdetection.database.DatabaseManager;
import com.example.fabricdefectdetection.database.DetectionResultDAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据库功能测试类
 * 
 * 用于测试数据库连接、建表和数据访问功能是否正常工作
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DatabaseTest {
    
    /**
     * 测试主方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        System.out.println("开始数据库测试...");
        
        try {
            // 测试数据库连接
            testDatabaseConnection();
            
            // 测试保存结果
            testSaveResult();
            
            // 测试查询结果
            testQueryResults();
            
            System.out.println("所有测试通过!");
            
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 测试数据库连接
     * 
     * @throws SQLException 如果连接失败
     */
    private static void testDatabaseConnection() throws SQLException {
        System.out.println("测试数据库连接...");
        
        Connection conn = DatabaseManager.getInstance().getConnection();
        if (conn != null && !conn.isClosed()) {
            System.out.println("数据库连接成功!");
            
            // 检查表是否存在
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                        "SELECT name FROM sqlite_master WHERE type='table' AND name='detection_history'");
                
                if (rs.next()) {
                    System.out.println("detection_history表已存在");
                } else {
                    System.err.println("警告: detection_history表不存在");
                }
            }
        } else {
            throw new SQLException("无法获取有效的数据库连接");
        }
    }
    
    /**
     * 测试保存结果
     */
    private static void testSaveResult() {
        System.out.println("测试保存检测结果...");
        
        // 创建一个模拟的分析报告
        DefectAnalysisReport report = new DefectAnalysisReport(
                2,                      // 缺陷数量
                "这是一个测试报告内容",  // 分析内容
                "一等品",               // 质量等级
                false                   // 是否AI增强
        );
        
        // 创建DAO并保存
        DetectionResultDAO dao = new DetectionResultDAO();
        dao.saveResult(report, "test_image.jpg");
        
        System.out.println("测试保存完成!");
    }
    
    /**
     * 测试查询结果
     */
    private static void testQueryResults() {
        System.out.println("测试查询检测结果...");
        
        // 获取当前日期
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(formatter);
        
        // 查询今天的结果
        DetectionResultDAO dao = new DetectionResultDAO();
        List<Object[]> results = dao.findResultsByDateRange(todayStr, todayStr);
        
        // 输出结果
        System.out.println("查询到 " + results.size() + " 条记录");
        
        for (Object[] row : results) {
            System.out.println("ID: " + row[0] + 
                    ", 时间: " + row[1] + 
                    ", 图片: " + row[2] + 
                    ", 缺陷数: " + row[3] +
                    ", 质量等级: " + row[4]);
        }
        
        // 如果有记录，测试获取分析报告内容
        if (!results.isEmpty()) {
            int id = (Integer)results.get(0)[0];
            String report = dao.getAnalysisReportById(id);
            System.out.println("分析报告内容: " + (report != null ? report.substring(0, Math.min(report.length(), 50)) + "..." : "null"));
        }
        
        System.out.println("查询测试完成!");
    }
} 