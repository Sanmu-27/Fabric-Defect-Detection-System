package com.example.fabricdefectdetection.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库管理器
 * 
 * 管理SQLite数据库连接和初始化数据库结构
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public final class DatabaseManager {
    
    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:fabric_detection.db";
    private static DatabaseManager instance;
    
    private Connection connection;
    
    /**
     * 私有构造函数，初始化数据库连接和表结构
     */
    private DatabaseManager() {
        try {
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            logger.info("成功连接到数据库");
            
            // 初始化数据库表结构
            initDatabase();
            
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "无法加载SQLite JDBC驱动", e);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "无法连接到数据库", e);
        }
    }
    
    /**
     * 获取数据库管理器单例实例
     * 
     * @return 数据库管理器实例
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * 初始化数据库表结构
     * 
     * @throws SQLException 如果SQL执行失败
     */
    private void initDatabase() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // 创建检测历史表
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS detection_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "detection_timestamp TEXT NOT NULL, " +
                "image_name TEXT, " +
                "defect_count INTEGER, " +
                "quality_level TEXT, " +
                "is_ai_enhanced INTEGER, " +
                "analysis_report TEXT)";
            
            stmt.execute(createTableSQL);
            logger.info("成功初始化数据库表结构");
            
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "创建数据库表失败", e);
            throw e;
        }
    }
    
    /**
     * 获取数据库连接
     * 
     * @return 数据库连接对象
     * @throws SQLException 如果连接不可用
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("数据库连接已关闭");
            } catch (SQLException e) {
                logger.log(Level.WARNING, "关闭数据库连接失败", e);
            }
        }
    }
} 