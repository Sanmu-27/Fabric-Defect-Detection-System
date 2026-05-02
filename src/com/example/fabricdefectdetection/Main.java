package com.example.fabricdefectdetection;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import com.example.fabricdefectdetection.ui.MainFrame;
import com.example.fabricdefectdetection.util.OpenCVLoader;

/**
 * 布匹瑕疵检测系统的主入口类
 * 
 * 该类负责初始化OpenCV库并启动图形用户界面。
 * 布匹瑕疵检测系统使用OpenCV进行图像处理和瑕疵检测，
 * 通过Java Swing提供用户界面。
 * 
 * @author FabricDefectDetection‘
 * @version 1.0
 */
public class Main {
    
    /**
     * 静态初始化块，用于加载OpenCV本地库
     * 在类被加载时自动执行
     */
    static {
        try {
            // 使用OpenCVLoader加载OpenCV库
            boolean loaded = OpenCVLoader.loadOpenCV();
            if (!loaded) {
                throw new UnsatisfiedLinkError("OpenCVLoader无法加载OpenCV库");
            }
            
            // 获取并显示OpenCV版本
            String version = OpenCVLoader.getOpenCVVersion();
            if (version != null) {
                System.out.println("OpenCV库已成功加载: " + version);
            } else {
                System.out.println("OpenCV库已加载，但无法获取版本信息");
            }
        } catch (UnsatisfiedLinkError e) {
            // 如果OpenCV库加载失败，记录错误并输出详细信息
            System.err.println("无法加载OpenCV本地库: " + e.getMessage());
            System.err.println("请确保OpenCV已正确安装，并且本地库路径已设置。");
            System.err.println("可以通过设置系统属性java.library.path来指定库路径。");
            
            // 加载失败时，程序无法继续运行，因此在此处理
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 应用程序主入口方法
     * 
     * @param args 命令行参数，本应用未使用
     */
    public static void main(String[] args) {
        // 检查OpenCV是否已正确加载
        try {
            // 检查OpenCV版本
            String openCVVersion = OpenCVLoader.getOpenCVVersion();
            if (openCVVersion != null) {
                System.out.println("启动布匹瑕疵检测系统，OpenCV版本: " + openCVVersion);
            } else {
                System.out.println("启动布匹瑕疵检测系统，无法获取OpenCV版本信息");
            }
            
            // 在EDT线程中启动Swing界面
            SwingUtilities.invokeLater(() -> {
                try {
                    // 创建并显示主窗口
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.setVisible(true);
                    System.out.println("系统界面已启动");
                } catch (Exception e) {
                    // 处理UI创建过程中可能出现的异常
                    System.err.println("创建用户界面时发生错误: " + e.getMessage());
                    e.printStackTrace();
                    
                    // 向用户显示错误信息
                    JOptionPane.showMessageDialog(
                        null,
                        "启动应用程序时发生错误:\n" + e.getMessage(),
                        "启动错误",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            });
        } catch (Exception e) {
            // 处理任何其他未预见的异常
            System.err.println("程序启动时发生未知错误: " + e.getMessage());
            e.printStackTrace();
            
            // 向用户显示错误信息
            JOptionPane.showMessageDialog(
                null,
                "程序启动失败:\n" + e.getMessage(),
                "严重错误",
                JOptionPane.ERROR_MESSAGE
            );
            
            // 发生严重错误时退出程序
            System.exit(2);
        }
    }
} 