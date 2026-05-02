package com.example.fabricdefectdetection.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.example.fabricdefectdetection.analysis.DefectAnalysisAssistant;
import com.example.fabricdefectdetection.analysis.DefectAnalysisReport;
import com.example.fabricdefectdetection.detection.DefectDetector;
import com.example.fabricdefectdetection.detection.DetectionResult;
import com.example.fabricdefectdetection.util.ConfigurationManager;
import com.example.fabricdefectdetection.util.ImageUtils;
import com.example.fabricdefectdetection.analysis.AIImageDetector;

/**
 * 布匹瑕疵检测系统的主窗口类
 * 
 * 该类提供系统的图形用户界面，包括菜单、图像显示区域和控制按钮。
 * 用户可以通过界面加载图像、启动检测过程，并查看检测结果。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class MainFrame extends JFrame {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(MainFrame.class.getName());
    
    // 界面常量
    private static final String TITLE = "布匹瑕疵检测系统";
    private static final int DEFAULT_WIDTH = 1200;
    private static final int DEFAULT_HEIGHT = 800;
    private static final String STATUS_READY = "就绪，请打开一张布匹图像";
    private static final String STATUS_DETECTING = "正在检测瑕疵...";
    private static final String STATUS_ANALYZING = "正在分析结果...";
    
    // UI组件
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenu historyMenu; // 历史菜单
    private JMenuItem openMenuItem;
    private JMenuItem settingsMenuItem; // 设置菜单项
    private JMenuItem exitMenuItem;
    
    private JTabbedPane tabbedPane; // 添加选项卡面板
    private JPanel singleDetectionPanel; // 单张检测面板
    private BatchProcessingPanel batchProcessingPanel; // 批量处理面板
    private HistoryPanel historyPanel; // 历史记录面板
    private AnalyticsDashboardPanel analyticsDashboardPanel; // 统计分析面板
    
    private ImagePanel originalImagePanel;
    private ImagePanel resultImagePanel;
    private AnalysisPanel analysisPanel;  // 分析面板
    
    private JPanel controlPanel;
    private JButton detectButton;
    private JButton analyzeButton;  // 分析按钮
    private JButton aiDetectButton;  // AI检测按钮
    private JLabel statusLabel;
    
    // 状态变量
    private File currentImageFile;
    private Mat originalMat;
    private DetectionResult detectionResult;
    private DefectDetector defectDetector;
    private DefectAnalysisAssistant analysisAssistant;  // 分析助手
    private ConfigurationManager configManager; // 配置管理器
    
    /**
     * 构造函数，初始化界面
     */
    public MainFrame() {
        super(TITLE);
        
        // 初始化配置管理器
        this.configManager = ConfigurationManager.getInstance();
        
        // 初始化检测器和分析助手
        this.defectDetector = new DefectDetector();
        this.analysisAssistant = new DefectAnalysisAssistant();
        
        // 设置窗口属性
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示
        
        // 初始化UI组件
        initMenuBar();
        initMainContent();
        initControlPanel();
        
        // 设置布局
        layoutComponents();
        
        // 更新状态
        updateStatus(STATUS_READY);
        
        logger.info("主窗口初始化完成");
    }
    
    /**
     * 初始化菜单栏
     */
    private void initMenuBar() {
        menuBar = new JMenuBar();
        
        // 文件菜单
        fileMenu = new JMenu("文件");
        
        // 打开图片菜单项
        openMenuItem = new JMenuItem("打开图片");
        openMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openImage();
            }
        });
        
        // 设置菜单项
        settingsMenuItem = new JMenuItem("设置");
        settingsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSettings();
            }
        });
        
        // 退出菜单项
        exitMenuItem = new JMenuItem("退出");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        // 组装菜单
        fileMenu.add(openMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(settingsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        // 历史菜单
        historyMenu = new JMenu("历史");
        JMenuItem viewHistoryMenuItem = new JMenuItem("查看历史记录");
        viewHistoryMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedComponent(historyPanel);
            }
        });
        historyMenu.add(viewHistoryMenuItem);
        
        // 分析菜单
        JMenu analysisMenu = new JMenu("分析");
        JMenuItem viewAnalyticsMenuItem = new JMenuItem("查看统计分析");
        viewAnalyticsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedComponent(analyticsDashboardPanel);
            }
        });
        analysisMenu.add(viewAnalyticsMenuItem);
        
        menuBar.add(fileMenu);
        menuBar.add(historyMenu);
        menuBar.add(analysisMenu);
        setJMenuBar(menuBar);
    }
    
    /**
     * 打开设置对话框
     */
    private void openSettings() {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.setVisible(true);
    }
    
    /**
     * 初始化主内容区域（图像面板和分析面板）
     */
    private void initMainContent() {
        // 创建选项卡面板
        tabbedPane = new JTabbedPane();
        
        // 创建单张检测面板
        singleDetectionPanel = new JPanel(new BorderLayout());
        
        // 创建两个图像面板
        originalImagePanel = new ImagePanel();
        originalImagePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "原始图像", 
                TitledBorder.CENTER, 
                TitledBorder.TOP));
        
        resultImagePanel = new ImagePanel();
        resultImagePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "检测结果", 
                TitledBorder.CENTER, 
                TitledBorder.TOP));
        
        // 创建分析面板
        analysisPanel = new AnalysisPanel();
        analysisPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), 
                "分析结果", 
                TitledBorder.CENTER, 
                TitledBorder.TOP));
        
        // 布局单张检测面板
        JPanel imageContainerPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        imageContainerPanel.add(originalImagePanel);
        imageContainerPanel.add(resultImagePanel);
        
        singleDetectionPanel.add(imageContainerPanel, BorderLayout.CENTER);
        singleDetectionPanel.add(analysisPanel, BorderLayout.EAST);
        
        // 创建批量处理面板
        batchProcessingPanel = new BatchProcessingPanel();
        
        // 创建历史记录面板
        historyPanel = new HistoryPanel();
        
        // 创建统计分析面板
        analyticsDashboardPanel = new AnalyticsDashboardPanel();
        
        // 添加选项卡
        tabbedPane.addTab("单张检测", new ImageIcon(), singleDetectionPanel, "检测单张布匹图像");
        tabbedPane.addTab("批量处理", new ImageIcon(), batchProcessingPanel, "批量处理多张布匹图像");
        tabbedPane.addTab("历史记录", new ImageIcon(), historyPanel, "查看历史检测记录");
        tabbedPane.addTab("统计分析", new ImageIcon(), analyticsDashboardPanel, "查看统计分析报表");
        
        // 添加选项卡切换监听器
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                // 单张检测标签页
                updateStatus(STATUS_READY);
            } else if (selectedIndex == 1) {
                // 批量处理标签页
                updateStatus("批量处理模式");
            } else if (selectedIndex == 2) {
                // 历史记录标签页
                updateStatus("历史记录查询模式");
            } else if (selectedIndex == 3) {
                // 统计分析标签页
                updateStatus("统计分析模式");
            }
        });
    }
    
    /**
     * 初始化控制面板（底部按钮和状态标签）
     */
    private void initControlPanel() {
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        // 创建开始检测按钮
        detectButton = new JButton("开始检测");
        detectButton.setEnabled(false); // 初始时禁用，直到加载图片
        detectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startDetection();
            }
        });
        
        // 创建分析按钮
        analyzeButton = new JButton("分析结果");
        analyzeButton.setEnabled(false); // 初始时禁用，直到有检测结果
        analyzeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAnalysis();
            }
        });
        
        // 创建AI检测按钮
        aiDetectButton = new JButton("AI检测");
        aiDetectButton.setEnabled(false); // 初始时禁用，直到加载图片
        aiDetectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAIDetection();
            }
        });
        
        // 添加按钮到按钮面板
        buttonPanel.add(detectButton);
        buttonPanel.add(aiDetectButton);
        buttonPanel.add(analyzeButton);
        
        // 创建状态标签
        statusLabel = new JLabel(STATUS_READY);
        statusLabel.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        // 添加到控制面板
        controlPanel.add(buttonPanel, BorderLayout.WEST);
        controlPanel.add(statusLabel, BorderLayout.CENTER);
    }
    
    /**
     * 设置组件布局
     */
    private void layoutComponents() {
        // 创建内容面板
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // 添加选项卡面板到内容区域
        contentPane.add(tabbedPane, BorderLayout.CENTER);
        
        // 添加控制面板到底部
        contentPane.add(controlPanel, BorderLayout.SOUTH);
        
        // 设置内容面板
        setContentPane(contentPane);
    }
    
    /**
     * 打开图像文件
     */
    private void openImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择布匹图像");
        
        // 设置文件过滤器，只显示图像文件
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "图像文件", "jpg", "jpeg", "png", "bmp", "tif", "tiff");
        fileChooser.setFileFilter(filter);
        
        // 显示文件选择对话框
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // 获取选择的文件
                currentImageFile = fileChooser.getSelectedFile();
                
                // 更新状态
                updateStatus("已加载图像: " + currentImageFile.getName());
                
                // 使用OpenCV加载图像
                originalMat = Imgcodecs.imread(currentImageFile.getAbsolutePath());
                
                if (originalMat.empty()) {
                    throw new Exception("无法读取图像文件");
                }
                
                // 将Mat转换为BufferedImage并显示
                BufferedImage originalImage = ImageUtils.matToBufferedImage(originalMat);
                originalImagePanel.setImage(originalImage);
                
                // 清除之前的结果
                resultImagePanel.setImage(null);
                detectionResult = null;
                
                // 启用检测按钮
                detectButton.setEnabled(true);
                aiDetectButton.setEnabled(true);
                analyzeButton.setEnabled(false);
                
                // 设置图片名称到检测结果中
                if (detectionResult != null) {
                    detectionResult.setImageName(currentImageFile.getName());
                }
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        this,
                        "打开图像时发生错误:\n" + e.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
                logger.log(Level.SEVERE, "打开图像时发生错误", e);
                
                // 重置状态
                currentImageFile = null;
                originalMat = null;
                detectButton.setEnabled(false);
                aiDetectButton.setEnabled(false);
                updateStatus(STATUS_READY);
            }
        }
    }
    
    /**
     * 开始瑕疵检测
     */
    private void startDetection() {
        if (originalMat == null || originalMat.empty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "请先打开一张有效的图像",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 更新UI状态
        detectButton.setEnabled(false);
        aiDetectButton.setEnabled(false);
        openMenuItem.setEnabled(false);
        updateStatus(STATUS_DETECTING);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // 使用SwingWorker在后台线程执行检测，避免阻塞UI
        SwingWorker<DetectionResult, Void> worker = new SwingWorker<DetectionResult, Void>() {
            @Override
            protected DetectionResult doInBackground() throws Exception {
                // 调用检测器执行检测
                long startTime = System.currentTimeMillis();
                DetectionResult result = defectDetector.detectDefects(originalMat.clone());
                long endTime = System.currentTimeMillis();
                
                // 记录检测耗时
                logger.info("检测耗时: " + (endTime - startTime) + "ms");
                
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    // 获取检测结果
                    detectionResult = get();
                    
                    // 设置图片名称
                    if (currentImageFile != null) {
                        detectionResult.setImageName(currentImageFile.getName());
                    }
                    
                    // 将结果显示在右侧面板
                    BufferedImage resultImage = ImageUtils.matToBufferedImage(detectionResult.getResultImageMat());
                    resultImagePanel.setImage(resultImage);
                    
                    // 更新状态显示检测结果信息
                    updateStatus("检测完成: " + detectionResult.getMessage());
                    
                    // 启用分析按钮
                    analyzeButton.setEnabled(true);
                    
                    // 检测完成后自动进行分析
                    boolean autoAnalyze = configManager.getBooleanProperty("system.auto-analyze", false);
                    
                    // 如果启用了自动分析，则自动执行分析
                    if (autoAnalyze) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                startAnalysis();
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "检测过程中发生错误:\n" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "检测过程中发生错误", e);
                    updateStatus("检测失败");
                } finally {
                    // 恢复UI状态
                    detectButton.setEnabled(true);
                    aiDetectButton.setEnabled(true);
                    openMenuItem.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        // 启动后台任务
        worker.execute();
    }
    
    /**
     * 开始分析检测结果
     */
    private void startAnalysis() {
        if (detectionResult == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "请先完成检测",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 更新UI状态
        analyzeButton.setEnabled(false);
        updateStatus(STATUS_ANALYZING);
        
        // 使用SwingWorker在后台线程执行分析，避免阻塞UI
        SwingWorker<DefectAnalysisReport, Void> worker = new SwingWorker<DefectAnalysisReport, Void>() {
            @Override
            protected DefectAnalysisReport doInBackground() throws Exception {
                // 调用分析助手执行分析
                return analysisAssistant.analyzeDefects(detectionResult);
            }
            
            @Override
            protected void done() {
                try {
                    // 获取分析结果
                    DefectAnalysisReport report = get();
                    
                    // 在分析面板中显示分析报告
                    analysisPanel.setAnalysisReport(report);
                    
                    // 更新状态
                    updateStatus("分析完成: 检测到" + report.getDefectCount() + "个缺陷，质量等级：" + report.getQualityLevel());
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "分析过程中发生错误:\n" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "分析过程中发生错误", e);
                    updateStatus("分析失败");
                } finally {
                    // 恢复UI状态
                    analyzeButton.setEnabled(true);
                }
            }
        };
        
        // 启动后台任务
        worker.execute();
    }
    
    /**
     * 开始AI检测（直接使用AI服务进行检测）
     */
    private void startAIDetection() {
        if (originalMat == null || originalMat.empty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "请先打开一张有效的图像",
                    "提示",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // 更新UI状态
        detectButton.setEnabled(false);
        aiDetectButton.setEnabled(false);
        openMenuItem.setEnabled(false);
        updateStatus("正在进行AI检测...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // 使用SwingWorker在后台线程执行AI检测
        SwingWorker<DetectionResult, Void> worker = new SwingWorker<DetectionResult, Void>() {
            @Override
            protected DetectionResult doInBackground() throws Exception {
                // 创建AI检测器
                AIImageDetector aiDetector = new AIImageDetector();
                
                // 执行检测
                return aiDetector.detectDefects(originalMat);
            }
            
            @Override
            protected void done() {
                try {
                    // 获取检测结果
                    detectionResult = get();
                    
                    // 设置图片名称
                    if (currentImageFile != null) {
                        detectionResult.setImageName(currentImageFile.getName());
                    }
                    
                    // 显示检测结果图像
                    BufferedImage resultImage = detectionResult.getResultBufferedImage();
                    resultImagePanel.setImage(resultImage);
                    
                    // 更新状态显示检测结果信息
                    updateStatus("AI检测完成: " + detectionResult.getMessage());
                    
                    // 启用分析按钮
                    analyzeButton.setEnabled(true);
                    
                    // 检测完成后自动进行分析
                    boolean autoAnalyze = configManager.getBooleanProperty("system.auto-analyze", false);
                    
                    // 如果启用了自动分析，则自动执行分析
                    if (autoAnalyze) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                startAnalysis();
                            }
                        });
                    }
                    
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            MainFrame.this,
                            "AI检测过程中发生错误:\n" + e.getMessage(),
                            "错误",
                            JOptionPane.ERROR_MESSAGE);
                    logger.log(Level.SEVERE, "AI检测过程中发生错误", e);
                    updateStatus("AI检测失败");
                } finally {
                    // 恢复UI状态
                    detectButton.setEnabled(true);
                    aiDetectButton.setEnabled(true);
                    openMenuItem.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        // 启动后台任务
        worker.execute();
    }
    
    /**
     * 更新状态标签
     * 
     * @param status 状态信息
     */
    private void updateStatus(String status) {
        statusLabel.setText(status);
    }
    
    /**
     * 窗口关闭前释放资源
     */
    @Override
    public void dispose() {
        // 关闭批量处理面板资源
        if (batchProcessingPanel != null) {
            batchProcessingPanel.close();
        }
        
        // 释放OpenCV资源
        if (originalMat != null && !originalMat.empty()) {
            originalMat.release();
        }
        
        super.dispose();
    }
} 