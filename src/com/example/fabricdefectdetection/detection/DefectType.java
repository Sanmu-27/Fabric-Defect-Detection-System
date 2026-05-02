package com.example.fabricdefectdetection.detection;

/**
 * 布匹缺陷类型枚举类
 * 
 * 定义了系统可以检测的各种布匹缺陷类型
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public enum DefectType {
    HOLE("破洞", "严重缺陷，直接导致布匹废弃"),
    STAIN("污渍", "视污渍大小和颜色决定严重程度"),
    BROKEN_THREAD("断线", "影响布匹结构强度的缺陷"),
    COLOR_SHADE("色差", "影响布匹美观度的缺陷"),
    MISSING_THREAD("缺线", "布匹编织中缺少纱线"),
    FOLD("折痕", "布匹表面不平整"),
    FABRIC_EDGE("布边问题", "布匹边缘不规则或有毛边"),
    UNKNOWN("未知缺陷", "无法确定类型的缺陷");
    
    private final String displayName;
    private final String description;
    
    /**
     * 构造函数
     * 
     * @param displayName 显示名称
     * @param description 描述信息
     */
    DefectType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 获取缺陷类型的显示名称
     * 
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 获取缺陷类型的描述信息
     * 
     * @return 描述信息
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据名称获取缺陷类型
     * 
     * @param name 缺陷类型名称
     * @return 缺陷类型枚举值，如果不存在则返回UNKNOWN
     */
    public static DefectType fromString(String name) {
        if (name == null || name.isEmpty()) {
            return UNKNOWN;
        }
        
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 尝试匹配显示名称
            for (DefectType type : values()) {
                if (type.displayName.equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
} 