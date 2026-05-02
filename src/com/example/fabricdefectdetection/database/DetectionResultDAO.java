package com.example.fabricdefectdetection.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.example.fabricdefectdetection.analysis.DefectAnalysisReport;

/**
 * 检测结果数据访问对象
 * 
 * 提供对检测结果数据的访问和操作方法
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class DetectionResultDAO {
    
    private static final Logger logger = Logger.getLogger(DetectionResultDAO.class.getName());
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 保存检测结果和分析报告到数据库
     * 
     * @param report 分析报告
     * @param imageName 图片名称
     */
    public void saveResult(DefectAnalysisReport report, String imageName) {
        String sql = "INSERT INTO detection_history " +
                     "(detection_timestamp, image_name, defect_count, quality_level, is_ai_enhanced, analysis_report) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 设置参数
            pstmt.setString(1, LocalDateTime.now().format(DATE_FORMAT));
            pstmt.setString(2, imageName);
            pstmt.setInt(3, report.getDefectCount());
            pstmt.setString(4, report.getQualityLevel());
            pstmt.setInt(5, report.isAiEnhanced() ? 1 : 0);
            pstmt.setString(6, report.getAnalysisContent());
            
            // 执行SQL
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("成功保存检测结果: " + imageName);
            } else {
                logger.warning("保存检测结果失败: " + imageName);
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "保存检测结果到数据库时发生错误", e);
        }
    }
    
    /**
     * 按日期范围查询检测结果
     * 
     * @param startDate 开始日期 (yyyy-MM-dd)
     * @param endDate 结束日期 (yyyy-MM-dd)
     * @return 检测结果数据列表
     */
    public List<Object[]> findResultsByDateRange(String startDate, String endDate) {
        List<Object[]> results = new ArrayList<>();
        
        String sql = "SELECT id, detection_timestamp, image_name, defect_count, quality_level, is_ai_enhanced " +
                     "FROM detection_history " +
                     "WHERE detection_timestamp BETWEEN ? AND ? " +
                     "ORDER BY detection_timestamp DESC";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // 转换日期格式添加时间部分
            pstmt.setString(1, startDate + " 00:00:00");
            pstmt.setString(2, endDate + " 23:59:59");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[6];
                    row[0] = rs.getInt("id");
                    row[1] = rs.getString("detection_timestamp");
                    row[2] = rs.getString("image_name");
                    row[3] = rs.getInt("defect_count");
                    row[4] = rs.getString("quality_level");
                    row[5] = rs.getBoolean("is_ai_enhanced");
                    
                    results.add(row);
                }
            }
            
            logger.info("查询到 " + results.size() + " 条检测结果记录");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "查询检测结果时发生错误", e);
        }
        
        return results;
    }
    
    /**
     * 根据ID获取完整的分析报告
     * 
     * @param id 检测结果ID
     * @return 分析报告文本
     */
    public String getAnalysisReportById(int id) {
        String report = null;
        String sql = "SELECT analysis_report FROM detection_history WHERE id = ?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    report = rs.getString("analysis_report");
                }
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取分析报告时发生错误", e);
        }
        
        return report;
    }
    
    /**
     * 删除检测结果记录
     * 
     * @param id 检测结果ID
     * @return 是否删除成功
     */
    public boolean deleteResult(int id) {
        String sql = "DELETE FROM detection_history WHERE id = ?";
                     
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("成功删除ID为" + id + "的检测结果");
                return true;
            } else {
                logger.warning("未找到ID为" + id + "的检测结果");
            }
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "删除检测结果时发生错误", e);
        }
        
        return false;
    }
} 