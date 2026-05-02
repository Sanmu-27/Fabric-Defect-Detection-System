package com.example.fabricdefectdetection.util;

import java.io.File;
import javax.swing.JOptionPane;
import java.util.Arrays;

/**
 * OpenCV库加载工具类
 * 
 * 用于在应用启动时正确加载OpenCV库。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class OpenCVLoader {
    
    // 可能的OpenCV库路径
    private static final String[] POSSIBLE_OPENCV_PATHS = {
        "opencv_java455", // OpenCV 4.5.5
        "opencv_java454", // OpenCV 4.5.4
        "opencv_java453", // OpenCV 4.5.3
        "opencv_java452", // OpenCV 4.5.2
        "opencv_java451", // OpenCV 4.5.1
        "opencv_java450", // OpenCV 4.5.0
        "opencv_java440", // OpenCV 4.4.0
        "opencv_java430", // OpenCV 4.3.0
        "opencv_java420", // OpenCV 4.2.0
        "opencv_java410", // OpenCV 4.1.0
        "opencv_java400"  // OpenCV 4.0.0
    };
    
    /**
     * 加载OpenCV库
     * 
     * @return 是否成功加载
     */
    public static boolean loadOpenCV() {
        boolean loaded = false;
        
        // 首先尝试系统属性指定的库名
        String opencvLibName = System.getProperty("opencv.libname");
        if (opencvLibName != null && !opencvLibName.isEmpty()) {
            loaded = tryLoadLibrary(opencvLibName);
        }
        
        // 如果没有指定或加载失败，尝试可能的库名
        if (!loaded) {
            for (String libName : POSSIBLE_OPENCV_PATHS) {
                if (tryLoadLibrary(libName)) {
                    loaded = true;
                    break;
                }
            }
        }
        
        // 如果仍然失败，显示错误消息
        if (!loaded) {
            String message = "无法加载OpenCV库。请确保已正确安装OpenCV，并将其库路径添加到系统路径或java.library.path中。\n\n"
                    + "当前java.library.path: " + System.getProperty("java.library.path") + "\n\n"
                    + "尝试的库名: " + Arrays.toString(POSSIBLE_OPENCV_PATHS) + "\n\n"
                    + "可以尝试以下步骤:\n"
                    + "1. 安装OpenCV 4.5.x\n"
                    + "2. 将OpenCV的bin目录添加到系统PATH环境变量\n"
                    + "3. 或使用-Djava.library.path=<OpenCV库路径> 启动程序\n"
                    + "4. 或使用-Dopencv.libname=<OpenCV库名> 指定具体的库名";
            
            System.err.println(message);
            JOptionPane.showMessageDialog(null, message, "OpenCV加载错误", JOptionPane.ERROR_MESSAGE);
        } else {
            System.out.println("OpenCV库加载成功");
        }
        
        return loaded;
    }
    
    /**
     * 尝试加载指定名称的库
     * 
     * @param libName 库名
     * @return 是否成功加载
     */
    private static boolean tryLoadLibrary(String libName) {
        try {
            System.loadLibrary(libName);
            System.out.println("成功加载OpenCV库: " + libName);
            return true;
        } catch (UnsatisfiedLinkError e) {
            System.out.println("尝试加载 " + libName + " 失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查OpenCV库的版本和可用性
     * 
     * @return OpenCV版本信息，如果无法获取则返回null
     */
    public static String getOpenCVVersion() {
        try {
            // 通过反射获取Core.VERSION
            Class<?> coreClass = Class.forName("org.opencv.core.Core");
            Object versionField = coreClass.getDeclaredField("VERSION").get(null);
            if (versionField != null) {
                return versionField.toString();
            }
        } catch (Exception e) {
            System.err.println("无法获取OpenCV版本: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 检查lib目录下是否有OpenCV的jar文件
     * 
     * @return 是否找到jar文件
     */
    public static boolean checkOpenCVJar() {
        File libDir = new File("lib");
        if (libDir.exists() && libDir.isDirectory()) {
            File[] files = libDir.listFiles((dir, name) -> name.toLowerCase().contains("opencv") && name.endsWith(".jar"));
            return files != null && files.length > 0;
        }
        return false;
    }
} 