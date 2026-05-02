package com.example.fabricdefectdetection.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.example.fabricdefectdetection.detection.BatchProcessor;
import com.example.fabricdefectdetection.detection.BatchTaskResult;

/**
 * 批量处理面板
 * 
 * 提供批量处理图片文件的用户界面
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class BatchProcessingPanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    private JButton selectFolderButton;
    private JButton startProcessingButton;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    
    private File selectedDirectory;
    private BatchProcessor batchProcessor;
    
    /**
     * 构造函数，初始化批量处理面板
     */
    public BatchProcessingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 初始化批处理器
        batchProcessor = new BatchProcessor();
        
        // 创建顶部控制面板
        createControlPanel();
        
        // 创建结果表格
        createResultsTable();
        
        // 创建状态面板
        createStatusPanel();
    }
    
    /**
     * 创建控制面板
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        selectFolderButton = new JButton("选择文件夹");
        selectFolderButton.addActionListener(e -> selectFolder());
        
        startProcessingButton = new JButton("开始处理");
        startProcessingButton.setEnabled(false);
        startProcessingButton.addActionListener(e -> startProcessing());
        
        controlPanel.add(selectFolderButton);
        controlPanel.add(startProcessingButton);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * 创建结果表格
     */
    private void createResultsTable() {
        String[] columnNames = {"文件名", "状态", "缺陷数", "质量等级"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            private static final long serialVersionUID = 1L;
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        resultsTable = new JTable(tableModel);
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setRowHeight(25);
        resultsTable.getTableHeader().setReorderingAllowed(false);
        
        // 设置列宽
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(250);  // 文件名
        resultsTable.getColumnModel().getColumn(1).setPreferredWidth(100);  // 状态
        resultsTable.getColumnModel().getColumn(2).setPreferredWidth(80);   // 缺陷数
        resultsTable.getColumnModel().getColumn(3).setPreferredWidth(100);  // 质量等级
        
        // 设置居中渲染器
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        resultsTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        resultsTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建状态面板
     */
    private void createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout(10, 0));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        statusLabel = new JLabel("就绪");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setValue(0);
        progressBar.setPreferredSize(new Dimension(150, 20));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 选择文件夹
     */
    private void selectFolder() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择包含图片的文件夹");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        if (selectedDirectory != null) {
            fileChooser.setCurrentDirectory(selectedDirectory);
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedDirectory = fileChooser.getSelectedFile();
            startProcessingButton.setEnabled(true);
            statusLabel.setText("已选择文件夹: " + selectedDirectory.getName());
            
            // 清空表格
            tableModel.setRowCount(0);
        }
    }
    
    /**
     * 开始处理
     */
    private void startProcessing() {
        if (selectedDirectory == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个文件夹", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (batchProcessor.isProcessing()) {
            JOptionPane.showMessageDialog(this, "已有批处理任务正在运行，请等待完成", "警告", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 清空表格
        tableModel.setRowCount(0);
        
        // 禁用按钮
        selectFolderButton.setEnabled(false);
        startProcessingButton.setEnabled(false);
        
        // 更新状态
        statusLabel.setText("正在处理...");
        progressBar.setValue(0);
        
        // 开始处理
        List<BatchTaskResult> results = batchProcessor.startProcessing(selectedDirectory, this::updateTaskResult);
        
        if (results == null || results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "在所选文件夹中没有找到支持的图片文件", "警告", JOptionPane.WARNING_MESSAGE);
            resetUI();
            return;
        }
        
        // 初始化表格数据
        for (BatchTaskResult result : results) {
            Vector<Object> rowData = new Vector<>();
            rowData.add(result.getFileName());
            rowData.add(result.getStatusDisplayName());
            rowData.add(result.getDefectCount());
            rowData.add(result.getQualityLevel());
            tableModel.addRow(rowData);
        }
    }
    
    /**
     * 更新任务结果
     * 
     * @param result 任务结果
     */
    private void updateTaskResult(BatchTaskResult result) {
        // 查找对应的行
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(result.getFileName())) {
                // 更新行数据
                tableModel.setValueAt(result.getStatusDisplayName(), i, 1);
                tableModel.setValueAt(result.getDefectCount(), i, 2);
                tableModel.setValueAt(result.getQualityLevel(), i, 3);
                break;
            }
        }
        
        // 更新进度
        updateProgress();
    }
    
    /**
     * 更新进度
     */
    private void updateProgress() {
        List<BatchTaskResult> results = batchProcessor.getTaskResults();
        if (results.isEmpty()) return;
        
        int total = results.size();
        int completed = 0;
        
        for (BatchTaskResult result : results) {
            if (result.isCompleted() || result.isFailed()) {
                completed++;
            }
        }
        
        int percentage = (completed * 100) / total;
        progressBar.setValue(percentage);
        progressBar.setString(percentage + "%");
        
        if (completed == total) {
            statusLabel.setText("处理完成. 总计: " + total + " 个文件");
            resetUI();
        } else {
            statusLabel.setText("正在处理... " + completed + "/" + total);
        }
    }
    
    /**
     * 重置UI状态
     */
    private void resetUI() {
        selectFolderButton.setEnabled(true);
        startProcessingButton.setEnabled(selectedDirectory != null);
    }
    
    /**
     * 关闭资源
     */
    public void close() {
        if (batchProcessor != null) {
            batchProcessor.shutdown();
        }
    }
} 