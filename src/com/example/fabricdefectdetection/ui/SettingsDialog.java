package com.example.fabricdefectdetection.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.example.fabricdefectdetection.util.ConfigurationManager;

/**
 * 系统设置对话框
 * 
 * 提供用户界面用于修改和保存应用配置
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    
    // 配置管理器实例
    private final ConfigurationManager configManager;
    
    // API设置相关字段
    private JTextField apiBaseUrlField;
    private JPasswordField apiKeyField;
    private JTextField apiModelField;
    private JTextField apiTimeoutField;
    
    // 系统设置相关字段
    private JCheckBox autoAnalyzeCheckbox;
    private JSpinner threadCountSpinner;
    
    // 检测参数设置相关字段
    private JSpinner minDefectAreaSpinner;
    private JSpinner maxDefectAreaSpinner;
    
    // 按钮
    private JButton saveButton;
    private JButton cancelButton;
    
    /**
     * 构造函数
     * 
     * @param parent 父窗口
     */
    public SettingsDialog(JFrame parent) {
        super(parent, "系统设置", true);
        
        // 获取配置管理器实例
        configManager = ConfigurationManager.getInstance();
        
        // 初始化界面组件
        initComponents();
        
        // 从配置加载初始值
        loadSettings();
        
        // 设置窗口属性
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    /**
     * 初始化界面组件
     */
    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 创建API设置面板
        tabbedPane.addTab("API设置", createApiPanel());
        
        // 创建系统设置面板
        tabbedPane.addTab("系统设置", createSystemPanel());
        
        // 创建检测参数设置面板
        tabbedPane.addTab("检测参数", createDetectionPanel());
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("保存");
        cancelButton = new JButton("取消");
        
        // 添加保存按钮事件
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
            }
        });
        
        // 添加取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // 设置布局
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建API设置面板
     * 
     * @return API设置面板
     */
    private JPanel createApiPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // API Base URL
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("API 基础URL:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        apiBaseUrlField = new JTextField(20);
        panel.add(apiBaseUrlField, gbc);
        
        // API Key
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("API Key:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        apiKeyField = new JPasswordField(20);
        panel.add(apiKeyField, gbc);
        
        // API Model
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("模型名称:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        apiModelField = new JTextField(20);
        panel.add(apiModelField, gbc);
        
        // API Timeout
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        panel.add(new JLabel("超时时间:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        apiTimeoutField = new JTextField(20);
        panel.add(apiTimeoutField, gbc);
        
        // 添加填充
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * 创建系统设置面板
     * 
     * @return 系统设置面板
     */
    private JPanel createSystemPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 自动分析选项
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        autoAnalyzeCheckbox = new JCheckBox("检测完成后自动进行分析");
        panel.add(autoAnalyzeCheckbox, gbc);
        
        // 线程数设置
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("处理线程数:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        threadCountSpinner = new JSpinner(new SpinnerNumberModel(4, 1, 16, 1));
        panel.add(threadCountSpinner, gbc);
        
        // 添加填充
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * 创建检测参数设置面板
     * 
     * @return 检测参数设置面板
     */
    private JPanel createDetectionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 最小缺陷面积
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("最小缺陷面积:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        minDefectAreaSpinner = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        panel.add(minDefectAreaSpinner, gbc);
        
        // 最大缺陷面积
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("最大缺陷面积:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        maxDefectAreaSpinner = new JSpinner(new SpinnerNumberModel(10000, 1000, 100000, 100));
        panel.add(maxDefectAreaSpinner, gbc);
        
        // 添加填充
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc);
        
        return panel;
    }
    
    /**
     * 从配置加载设置到UI
     */
    private void loadSettings() {
        // API设置
        apiBaseUrlField.setText(configManager.getProperty("api.base_url"));
        apiKeyField.setText(configManager.getProperty("api.key"));
        apiModelField.setText(configManager.getProperty("api.model"));
        apiTimeoutField.setText(configManager.getProperty("api.timeout"));
        
        // 系统设置
        autoAnalyzeCheckbox.setSelected(configManager.getBooleanProperty("system.auto-analyze", true));
        threadCountSpinner.setValue(configManager.getIntProperty("system.thread-count", 4));
        
        // 检测参数设置
        minDefectAreaSpinner.setValue(configManager.getIntProperty("detection.min-defect-area", 100));
        maxDefectAreaSpinner.setValue(configManager.getIntProperty("detection.max-defect-area", 10000));
    }
    
    /**
     * 保存UI设置到配置
     */
    private void saveSettings() {
        // API设置
        configManager.setProperty("api.base_url", apiBaseUrlField.getText());
        configManager.setProperty("api.key", new String(apiKeyField.getPassword()));
        configManager.setProperty("api.model", apiModelField.getText());
        configManager.setProperty("api.timeout", apiTimeoutField.getText());
        
        // 系统设置
        configManager.setProperty("system.auto-analyze", String.valueOf(autoAnalyzeCheckbox.isSelected()));
        configManager.setProperty("system.thread-count", String.valueOf(threadCountSpinner.getValue()));
        
        // 检测参数设置
        configManager.setProperty("detection.min-defect-area", String.valueOf(minDefectAreaSpinner.getValue()));
        configManager.setProperty("detection.max-defect-area", String.valueOf(maxDefectAreaSpinner.getValue()));
        
        // 保存到配置文件
        boolean saved = configManager.saveConfiguration();
        
        if (saved) {
            JOptionPane.showMessageDialog(this, "设置已保存", "保存成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "无法保存设置", "保存失败", JOptionPane.ERROR_MESSAGE);
        }
    }
} 