package com.example.fabricdefectdetection.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 配置管理器
 * 
 * 负责系统配置的加载、保存和访问
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class ConfigurationManager {
    
    private static final Logger logger = Logger.getLogger(ConfigurationManager.class.getName());
    private static final String CONFIG_FILE = "config.properties";
    private static ConfigurationManager instance;
    
    private final Properties properties;
    private final File configFile;
    
    /**
     * 私有构造函数，加载或创建配置文件
     */
    private ConfigurationManager() {
        properties = new Properties();
        configFile = new File(CONFIG_FILE);
        
        // 加载配置文件
        if (configFile.exists()) {
            loadConfiguration();
        } else {
            // 创建默认配置
            createDefaultConfiguration();
        }
    }
    
    /**
     * 获取配置管理器单例实例
     * 
     * @return 配置管理器实例
     */
    public static synchronized ConfigurationManager getInstance() {
        if (instance == null) {
            instance = new ConfigurationManager();
        }
        return instance;
    }
    
    /**
     * 加载配置文件
     */
    private void loadConfiguration() {
        try (InputStream input = new FileInputStream(configFile)) {
            properties.load(input);
            logger.info("成功从 " + configFile.getAbsolutePath() + " 加载配置");
        } catch (IOException e) {
            logger.log(Level.WARNING, "加载配置文件失败，将创建默认配置", e);
            createDefaultConfiguration();
        }
    }
    
    /**
     * 创建默认配置
     */
    private void createDefaultConfiguration() {
        // API配置
        properties.setProperty("api.key", "sk-hYScYPoDDPlPQUzlAbD6B5B502894dD09d41DaB5Ff836c18");
        properties.setProperty("api.base_url", "https://vip.apiyi.com/v1");
        properties.setProperty("api.model", "deepseek-chat");
        properties.setProperty("api.timeout", "PT60S");
        
        // 系统配置
        properties.setProperty("system.auto-analyze", "true");
        properties.setProperty("system.thread-count", "4");
        
        // 检测参数配置
        properties.setProperty("detection.min-defect-area", "100");
        properties.setProperty("detection.max-defect-area", "10000");
        
        // 保存默认配置
        saveConfiguration();
        logger.info("已创建默认配置文件");
    }
    
    /**
     * 保存当前配置到文件
     * 
     * @return 是否保存成功
     */
    public boolean saveConfiguration() {
        try (OutputStream output = new FileOutputStream(configFile)) {
            properties.store(output, "Fabric Defect Detection System Configuration");
            logger.info("配置已保存到 " + configFile.getAbsolutePath());
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "保存配置文件失败", e);
            return false;
        }
    }
    
    /**
     * 获取配置属性
     * 
     * @param key 属性键
     * @return 属性值，如果不存在则返回null
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * 获取配置属性，如果不存在则返回默认值
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 属性值，如果不存在则返回默认值
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * 获取整数配置属性
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 整数属性值，如果不存在或无法转换则返回默认值
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warning("无法将属性 " + key + " 的值 '" + value + "' 转换为整数，使用默认值 " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 获取布尔配置属性
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @return 布尔属性值，如果不存在则返回默认值
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * 设置配置属性
     * 
     * @param key 属性键
     * @param value 属性值
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * 删除配置属性
     * 
     * @param key 要删除的属性键
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    /**
     * 刷新配置（重新加载配置文件）
     */
    public void refreshConfiguration() {
        loadConfiguration();
    }
} 