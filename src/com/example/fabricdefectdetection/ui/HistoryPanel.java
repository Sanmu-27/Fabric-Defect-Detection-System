package com.example.fabricdefectdetection.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import com.example.fabricdefectdetection.database.DetectionResultDAO;

/**
 * 历史记录面板
 * 
 * 提供查询和显示检测历史记录的功能，支持多维度筛选和排序
 * 
 * @author FabricDefectDetection
 * @version 2.0
 */
public class HistoryPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    private JTextField startDateField;
    private JTextField endDateField;
    private JComboBox<String> qualityLevelComboBox;
    private JCheckBox aiEnhancedCheckBox;
    private JButton queryButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JLabel statusLabel;
    
    private DetectionResultDAO resultDAO;
    
    /**
     * 构造函数，初始化历史记录面板
     */
    public HistoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 初始化DAO
        resultDAO = new DetectionResultDAO();
        
        // 创建查询面板
        createQueryPanel();
        
        // 创建结果表格
        createResultsTable();
        
        // 创建状态面板
        createStatusPanel();
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
        
        // 质量等级筛选
        JLabel qualityLevelLabel = new JLabel("质量等级:");
        qualityLevelComboBox = new JComboBox<>(new String[]{"全部", "优等品", "一等品", "二等品", "不合格品"});
        
        // AI增强筛选
        aiEnhancedCheckBox = new JCheckBox("AI增强");
        aiEnhancedCheckBox.setSelected(false);
        
        // 查询按钮
        queryButton = new JButton("查询");
        queryButton.addActionListener(e -> performQuery());
        
        // 添加组件到查询面板
        queryPanel.add(startDateLabel);
        queryPanel.add(startDateField);
        queryPanel.add(endDateLabel);
        queryPanel.add(endDateField);
        queryPanel.add(qualityLevelLabel);
        queryPanel.add(qualityLevelComboBox);
        queryPanel.add(aiEnhancedCheckBox);
        queryPanel.add(queryButton);
        
        add(queryPanel, BorderLayout.NORTH);
    }
    
    /**
     * 创建结果表格
     */
    private void createResultsTable() {
        String[] columnNames = {"ID", "检测时间", "图片名称", "缺陷数", "质量等级", "AI增强"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class;
                if (columnIndex == 5) return Boolean.class;
                return String.class;
            }
        };
        
        // 创建表格和排序器
        resultsTable = new JTable(tableModel);
        tableSorter = new TableRowSorter<>(tableModel);
        resultsTable.setRowSorter(tableSorter);
        
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(25);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        
        // 设置列宽
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(150);  // 检测时间
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(200);  // 图片名称
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(80);   // 缺陷数
        resultsTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // 质量等级
        resultsTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // AI增强
        
        // 设置居中渲染器
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        
        // 添加双击事件
        resultsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showDetailReport();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建状态面板
     */
    private void createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        JLabel tipLabel = new JLabel("提示: 双击记录查看详细报告，点击表头排序");
        tipLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        tipLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(tipLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 执行查询
     */
    private void performQuery() {
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
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 查询数据
        List<Object[]> results = resultDAO.findResultsByDateRange(startDate, endDate);
        
        // 更新表格
        for (Object[] row : results) {
            tableModel.addRow(new Object[]{
                row[0],  // ID
                row[1],  // 检测时间
                row[2],  // 图片名称
                row[3],  // 缺陷数
                row[4],  // 质量等级
                row[5]   // AI增强
            });
        }
        
        // 应用筛选条件
        applyFilters();
        
        // 更新状态
        statusLabel.setText("查询完成，找到 " + tableSorter.getViewRowCount() + " 条记录");
    }
    
    /**
     * 应用筛选条件
     */
    private void applyFilters() {
        RowFilter<DefaultTableModel, Integer> filter = null;
        
        // 获取选中的质量等级
        String qualityLevel = (String) qualityLevelComboBox.getSelectedItem();
        boolean aiEnhanced = aiEnhancedCheckBox.isSelected();
        
        // 创建组合筛选器
        if (!"全部".equals(qualityLevel) || aiEnhanced) {
            // 构建筛选条件
            if (!"全部".equals(qualityLevel) && aiEnhanced) {
                // 同时筛选质量等级和AI增强
                filter = RowFilter.andFilter(List.of(
                    RowFilter.regexFilter("^" + qualityLevel + "$", 4),  // 质量等级列
                    RowFilter.regexFilter("^true$", 5)                   // AI增强列
                ));
            } else if (!"全部".equals(qualityLevel)) {
                // 只筛选质量等级
                filter = RowFilter.regexFilter("^" + qualityLevel + "$", 4);
            } else {
                // 只筛选AI增强
                filter = RowFilter.regexFilter("^true$", 5);
            }
            
            // 应用筛选器
            tableSorter.setRowFilter(filter);
        } else {
            // 清除筛选器
            tableSorter.setRowFilter(null);
        }
    }
    
    /**
     * 显示详细报告
     */
    private void showDetailReport() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        // 转换为模型索引
        int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
        
        // 获取ID
        int id = (int) tableModel.getValueAt(modelRow, 0);
        
        // 获取报告内容
        String reportContent = resultDAO.getAnalysisReportById(id);
        if (reportContent == null || reportContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "无法获取报告内容", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 获取记录信息
        String time = (String) tableModel.getValueAt(modelRow, 1);
        String imageName = (String) tableModel.getValueAt(modelRow, 2);
        int defectCount = (int) tableModel.getValueAt(modelRow, 3);
        String qualityLevel = (String) tableModel.getValueAt(modelRow, 4);
        boolean isAiEnhanced = (boolean) tableModel.getValueAt(modelRow, 5);
        
        // 显示报告对话框
        ReportViewerDialog dialog = new ReportViewerDialog(
                JOptionPane.getFrameForComponent(this),
                id, time, imageName, defectCount, qualityLevel, isAiEnhanced, reportContent);
        dialog.setVisible(true);
    }
    
    /**
     * 报告查看器对话框
     */
    private class ReportViewerDialog extends javax.swing.JDialog {
        
        private static final long serialVersionUID = 1L;
        
        /**
         * 构造函数
         */
        public ReportViewerDialog(java.awt.Frame parent, int id, String time, String imageName, 
                int defectCount, String qualityLevel, boolean isAiEnhanced, String reportContent) {
            super(parent, "检测报告 #" + id, true);
            initComponents(id, time, imageName, defectCount, qualityLevel, isAiEnhanced, reportContent);
            setSize(800, 600);
            setLocationRelativeTo(parent);
        }
        
        /**
         * 初始化组件
         */
        private void initComponents(int id, String time, String imageName, 
                int defectCount, String qualityLevel, boolean isAiEnhanced, String reportContent) {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
            
            // 创建信息面板
            JPanel infoPanel = new JPanel(new BorderLayout());
            
            StringBuilder infoHtml = new StringBuilder();
            infoHtml.append("<html><body style='font-family:Arial;'>");
            infoHtml.append("<h2 style='margin:0;'>检测报告 #").append(id).append("</h2>");
            infoHtml.append("<p>检测时间: ").append(time).append("<br>");
            infoHtml.append("图片名称: ").append(imageName).append("<br>");
            infoHtml.append("缺陷数量: ").append(defectCount).append("<br>");
            infoHtml.append("质量等级: <b>").append(qualityLevel).append("</b><br>");
            infoHtml.append("AI增强: ").append(isAiEnhanced ? "是" : "否").append("</p>");
            infoHtml.append("</body></html>");
            
            JLabel infoLabel = new JLabel(infoHtml.toString());
            infoPanel.add(infoLabel, BorderLayout.CENTER);
            
            // 创建内容面板
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.setBorder(BorderFactory.createTitledBorder("分析内容"));
            
            javax.swing.JTextArea textArea = new javax.swing.JTextArea(reportContent);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            contentPanel.add(scrollPane, BorderLayout.CENTER);
            
            // 创建按钮面板
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton closeButton = new JButton("关闭");
            closeButton.addActionListener(e -> dispose());
            buttonPanel.add(closeButton);
            
            // 添加到主面板
            panel.add(infoPanel, BorderLayout.NORTH);
            panel.add(contentPanel, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);
            
            setContentPane(panel);
        }
    }
} 