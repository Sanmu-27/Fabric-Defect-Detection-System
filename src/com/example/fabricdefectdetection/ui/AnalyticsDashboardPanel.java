package com.example.fabricdefectdetection.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.example.fabricdefectdetection.database.DetectionResultDAO;

/**
 * 统计分析面板
 * 
 * 提供数据可视化和统计分析功能
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class AnalyticsDashboardPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private JTextField startDateField;
    private JTextField endDateField;
    private JButton queryButton;
    private PieChartPanel pieChartPanel;
    private BarChartPanel barChartPanel;
    private LineChartPanel lineChartPanel;
    
    private DetectionResultDAO resultDAO;
    
    /**
     * 构造函数，初始化统计分析面板
     */
    public AnalyticsDashboardPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 初始化DAO
        resultDAO = new DetectionResultDAO();
        
        // 创建查询面板
        createQueryPanel();
        
        // 创建图表面板
        createChartPanels();
    }
    
    /**
     * 创建查询面板
     */
    private void createQueryPanel() {
        JPanel queryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        queryPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // 开始日期
        JLabel startDateLabel = new JLabel("开始日期:");
        startDateField = new JTextField(10);
        startDateField.setText(LocalDate.now().minusDays(30).format(DATE_FORMATTER));
        startDateField.setToolTipText("格式: yyyy-MM-dd");
        
        // 结束日期
        JLabel endDateLabel = new JLabel("结束日期:");
        endDateField = new JTextField(10);
        endDateField.setText(LocalDate.now().format(DATE_FORMATTER));
        endDateField.setToolTipText("格式: yyyy-MM-dd");
        
        // 查询按钮
        queryButton = new JButton("生成分析报表");
        queryButton.addActionListener(e -> generateReport());
        
        // 添加组件
        queryPanel.add(startDateLabel);
        queryPanel.add(startDateField);
        queryPanel.add(endDateLabel);
        queryPanel.add(endDateField);
        queryPanel.add(queryButton);
        
        add(queryPanel, BorderLayout.NORTH);
    }
    
    /**
     * 创建图表面板
     */
    private void createChartPanels() {
        // 创建图表容器
        JPanel chartsContainer = new JPanel(new BorderLayout(10, 10));
        
        // 创建上部面板（饼图和柱状图）
        JPanel topChartsPanel = new JPanel(new BorderLayout(10, 0));
        
        // 创建饼图面板
        pieChartPanel = new PieChartPanel();
        pieChartPanel.setBorder(BorderFactory.createTitledBorder("缺陷类型分布"));
        pieChartPanel.setPreferredSize(new Dimension(400, 300));
        
        // 创建柱状图面板
        barChartPanel = new BarChartPanel();
        barChartPanel.setBorder(BorderFactory.createTitledBorder("质量等级分布"));
        barChartPanel.setPreferredSize(new Dimension(400, 300));
        
        // 添加饼图和柱状图到上部面板
        topChartsPanel.add(pieChartPanel, BorderLayout.WEST);
        topChartsPanel.add(barChartPanel, BorderLayout.CENTER);
        
        // 创建折线图面板
        lineChartPanel = new LineChartPanel();
        lineChartPanel.setBorder(BorderFactory.createTitledBorder("每日平均缺陷数趋势"));
        lineChartPanel.setPreferredSize(new Dimension(800, 250));
        
        // 添加上部面板和折线图到图表容器
        chartsContainer.add(topChartsPanel, BorderLayout.CENTER);
        chartsContainer.add(lineChartPanel, BorderLayout.SOUTH);
        
        add(chartsContainer, BorderLayout.CENTER);
    }
    
    /**
     * 生成报表
     */
    private void generateReport() {
        String startDate = startDateField.getText().trim();
        String endDate = endDateField.getText().trim();
        
        // 验证日期格式
        try {
            LocalDate.parse(startDate, DATE_FORMATTER);
            LocalDate.parse(endDate, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "日期格式不正确，请使用yyyy-MM-dd格式", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // 查询数据
            List<Object[]> results = resultDAO.findResultsByDateRange(startDate, endDate);
            
            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "选定日期范围内没有数据", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            // 分析数据
            Map<String, Integer> defectTypeDistribution = analyzeDefectTypeDistribution(results);
            Map<String, Integer> qualityLevelDistribution = analyzeQualityLevelDistribution(results);
            Map<String, Double> dailyDefectTrend = analyzeDailyDefectTrend(results);
            
            // 更新图表
            pieChartPanel.updateData(defectTypeDistribution);
            barChartPanel.updateData(qualityLevelDistribution);
            lineChartPanel.updateData(dailyDefectTrend);
            
            // 重绘面板
            pieChartPanel.repaint();
            barChartPanel.repaint();
            lineChartPanel.repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "生成报表时发生错误: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    /**
     * 分析缺陷类型分布
     */
    private Map<String, Integer> analyzeDefectTypeDistribution(List<Object[]> results) {
        // 示例数据，实际项目中应从数据库查询或解析结果
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("破洞", 25);
        distribution.put("污渍", 35);
        distribution.put("断线", 15);
        distribution.put("色差", 10);
        distribution.put("其他", 15);
        return distribution;
    }
    
    /**
     * 分析质量等级分布
     */
    private Map<String, Integer> analyzeQualityLevelDistribution(List<Object[]> results) {
        Map<String, Integer> distribution = new HashMap<>();
        
        // 统计不同质量等级的数量
        for (Object[] row : results) {
            String qualityLevel = (String) row[4];
            distribution.put(qualityLevel, distribution.getOrDefault(qualityLevel, 0) + 1);
        }
        
        return distribution;
    }
    
    /**
     * 分析每日平均缺陷趋势
     */
    private Map<String, Double> analyzeDailyDefectTrend(List<Object[]> results) {
        // 按日期分组统计
        Map<String, List<Integer>> defectsByDate = new HashMap<>();
        
        for (Object[] row : results) {
            String timestamp = (String) row[1];
            String date = timestamp.split(" ")[0];  // 提取日期部分
            int defectCount = (int) row[3];
            
            defectsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(defectCount);
        }
        
        // 计算每日平均值
        Map<String, Double> dailyAverage = new TreeMap<>();  // TreeMap确保日期排序
        for (Map.Entry<String, List<Integer>> entry : defectsByDate.entrySet()) {
            String date = entry.getKey();
            List<Integer> counts = entry.getValue();
            
            double average = counts.stream().mapToInt(Integer::intValue).average().orElse(0);
            dailyAverage.put(date, average);
        }
        
        return dailyAverage;
    }
    
    /**
     * 饼图面板
     */
    private class PieChartPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Map<String, Integer> data = new HashMap<>();
        private Color[] colors = {
            new Color(66, 133, 244),    // 蓝色
            new Color(219, 68, 55),     // 红色
            new Color(244, 180, 0),     // 黄色
            new Color(15, 157, 88),     // 绿色
            new Color(171, 71, 188)     // 紫色
        };
        
        /**
         * 更新数据
         */
        public void updateData(Map<String, Integer> data) {
            this.data = data;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (data.isEmpty()) {
                drawNoDataMessage(g2d);
                return;
            }
            
            // 计算总和
            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            
            // 绘制饼图
            int width = getWidth() - 100;  // 留出空间显示图例
            int height = getHeight() - 40; // 留出空间显示标题
            int x = 50;
            int y = 30;
            int size = Math.min(width, height);
            
            // 绘制饼图
            double startAngle = 0;
            int colorIndex = 0;
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String label = entry.getKey();
                int value = entry.getValue();
                double angle = 360.0 * value / total;
                
                // 设置颜色
                g2d.setColor(colors[colorIndex % colors.length]);
                
                // 绘制扇形
                Arc2D.Double arc = new Arc2D.Double(x, y, size, size, startAngle, angle, Arc2D.PIE);
                g2d.fill(arc);
                
                // 更新角度和颜色索引
                startAngle += angle;
                colorIndex++;
            }
            
            // 绘制图例
            int legendX = x + size + 10;
            int legendY = y + 20;
            colorIndex = 0;
            
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String label = entry.getKey();
                int value = entry.getValue();
                double percentage = 100.0 * value / total;
                
                // 绘制颜色块
                g2d.setColor(colors[colorIndex % colors.length]);
                g2d.fillRect(legendX, legendY, 15, 15);
                
                // 绘制标签和值
                g2d.setColor(Color.BLACK);
                g2d.drawString(label + ": " + value + " (" + String.format("%.1f", percentage) + "%)", 
                        legendX + 20, legendY + 12);
                
                // 更新位置和颜色索引
                legendY += 20;
                colorIndex++;
            }
        }
        
        /**
         * 绘制无数据信息
         */
        private void drawNoDataMessage(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            String message = "暂无数据";
            int x = (getWidth() - g2d.getFontMetrics().stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
    
    /**
     * 柱状图面板
     */
    private class BarChartPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Map<String, Integer> data = new HashMap<>();
        
        /**
         * 更新数据
         */
        public void updateData(Map<String, Integer> data) {
            this.data = data;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (data.isEmpty()) {
                drawNoDataMessage(g2d);
                return;
            }
            
            // 绘制坐标轴
            int margin = 50;
            int width = getWidth() - 2 * margin;
            int height = getHeight() - 2 * margin;
            int axisX = margin;
            int axisY = getHeight() - margin;
            
            // 绘制X轴
            g2d.setColor(Color.BLACK);
            g2d.drawLine(axisX, axisY, axisX + width, axisY);
            
            // 绘制Y轴
            g2d.drawLine(axisX, axisY, axisX, axisY - height);
            
            // 找出最大值
            int maxValue = data.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            
            // 计算刻度
            int scaleY = calculateScale(maxValue);
            
            // 绘制Y轴刻度
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            for (int i = 0; i <= maxValue; i += scaleY) {
                int y = axisY - (i * height) / (maxValue + scaleY);
                g2d.drawLine(axisX - 5, y, axisX, y);
                g2d.drawString(String.valueOf(i), axisX - 30, y + 5);
            }
            
            // 绘制柱状图
            int barCount = data.size();
            int barWidth = width / (barCount * 2);
            int barSpacing = barWidth / 2;
            int index = 0;
            
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String label = entry.getKey();
                int value = entry.getValue();
                
                // 计算柱子位置
                int x = axisX + barSpacing + index * (barWidth + barSpacing);
                int barHeight = (value * height) / (maxValue + scaleY);
                int y = axisY - barHeight;
                
                // 设置颜色
                if ("优等品".equals(label) || "一等品".equals(label)) {
                    g2d.setColor(new Color(15, 157, 88));  // 绿色
                } else if ("二等品".equals(label)) {
                    g2d.setColor(new Color(244, 180, 0));  // 黄色
                } else {
                    g2d.setColor(new Color(219, 68, 55));  // 红色
                }
                
                // 绘制柱子
                g2d.fillRect(x, y, barWidth, barHeight);
                
                // 绘制标签
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                int labelWidth = g2d.getFontMetrics().stringWidth(label);
                g2d.drawString(label, x + (barWidth - labelWidth) / 2, axisY + 15);
                
                // 绘制数值
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
                String valueStr = String.valueOf(value);
                int valueWidth = g2d.getFontMetrics().stringWidth(valueStr);
                g2d.drawString(valueStr, x + (barWidth - valueWidth) / 2, y - 5);
                
                index++;
            }
        }
        
        /**
         * 计算适当的刻度
         */
        private int calculateScale(int maxValue) {
            if (maxValue <= 5) return 1;
            if (maxValue <= 10) return 2;
            if (maxValue <= 50) return 10;
            if (maxValue <= 100) return 20;
            return maxValue / 5;
        }
        
        /**
         * 绘制无数据信息
         */
        private void drawNoDataMessage(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            String message = "暂无数据";
            int x = (getWidth() - g2d.getFontMetrics().stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
    
    /**
     * 折线图面板
     */
    private class LineChartPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private Map<String, Double> data = new TreeMap<>();  // 使用TreeMap确保按日期排序
        
        /**
         * 更新数据
         */
        public void updateData(Map<String, Double> data) {
            this.data = data;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (data.isEmpty()) {
                drawNoDataMessage(g2d);
                return;
            }
            
            // 绘制坐标轴
            int margin = 50;
            int width = getWidth() - 2 * margin;
            int height = getHeight() - 2 * margin;
            int axisX = margin;
            int axisY = getHeight() - margin;
            
            // 绘制X轴
            g2d.setColor(Color.BLACK);
            g2d.drawLine(axisX, axisY, axisX + width, axisY);
            
            // 绘制Y轴
            g2d.drawLine(axisX, axisY, axisX, axisY - height);
            
            // 找出最大值
            double maxValue = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
            if (maxValue < 1) maxValue = 1;  // 确保最小刻度为1
            
            // 绘制Y轴刻度
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
            int scaleCount = 5;  // 分5份
            for (int i = 0; i <= scaleCount; i++) {
                double value = maxValue * i / scaleCount;
                int y = axisY - (int)(i * height / scaleCount);
                g2d.drawLine(axisX - 5, y, axisX, y);
                g2d.drawString(String.format("%.1f", value), axisX - 35, y + 5);
            }
            
            // 计算X轴数据点间距
            int pointCount = data.size();
            int pointSpacing = width / (pointCount > 1 ? pointCount - 1 : 1);
            
            // 如果数据点过多，可能需要减少显示的日期标签
            int dateSkip = Math.max(1, pointCount / 10);  // 最多显示10个日期标签
            
            // 准备绘制折线
            int[] xPoints = new int[pointCount];
            int[] yPoints = new int[pointCount];
            
            // 绘制数据点和连接线
            List<String> dates = new ArrayList<>(data.keySet());
            for (int i = 0; i < pointCount; i++) {
                String date = dates.get(i);
                double value = data.get(date);
                
                // 计算坐标
                int x = axisX + i * pointSpacing;
                int y = axisY - (int)(value * height / maxValue);
                
                // 存储坐标点
                xPoints[i] = x;
                yPoints[i] = y;
                
                // 绘制数据点
                g2d.setColor(new Color(66, 133, 244));
                g2d.fillOval(x - 3, y - 3, 6, 6);
                
                // 绘制X轴日期标签（每隔dateSkip个显示一个）
                if (i % dateSkip == 0 || i == pointCount - 1) {
                    g2d.setColor(Color.BLACK);
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
                    String shortDate = date.substring(5);  // 仅显示月-日
                    int labelWidth = g2d.getFontMetrics().stringWidth(shortDate);
                    g2d.drawString(shortDate, x - labelWidth / 2, axisY + 15);
                }
            }
            
            // 绘制连接线
            g2d.setColor(new Color(66, 133, 244));
            for (int i = 0; i < pointCount - 1; i++) {
                g2d.drawLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
            }
            
            // 绘制Y轴标题
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            String yTitle = "每日平均缺陷数 (DPU)";
            g2d.rotate(-Math.PI/2, axisX - 35, axisY - height/2);
            g2d.drawString(yTitle, axisX - 35, axisY - height/2);
            g2d.rotate(Math.PI/2, axisX - 35, axisY - height/2);
        }
        
        /**
         * 绘制无数据信息
         */
        private void drawNoDataMessage(Graphics2D g2d) {
            g2d.setColor(Color.GRAY);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            String message = "暂无数据";
            int x = (getWidth() - g2d.getFontMetrics().stringWidth(message)) / 2;
            int y = getHeight() / 2;
            g2d.drawString(message, x, y);
        }
    }
} 