package com.example.fabricdefectdetection.ui;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * 图像显示面板
 * 
 * 该类继承自JPanel，专门用于显示和缩放BufferedImage图像。
 * 
 * @author FabricDefectDetection
 * @version 1.0
 */
public class ImagePanel extends JPanel {
    
    private static final long serialVersionUID = 1L;
    
    // 当前显示的图像
    private BufferedImage image;
    
    // 控制图像缩放方式
    private boolean maintainAspectRatio = true;
    
    /**
     * 构造函数
     */
    public ImagePanel() {
        // 设置背景色和边框
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEtchedBorder());
        
        // 设置首选大小
        setPreferredSize(new Dimension(400, 300));
    }
    
    /**
     * 设置要显示的图像
     * 
     * @param image 要显示的BufferedImage，可以为null
     */
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint(); // 重绘面板以显示新图像
    }
    
    /**
     * 获取当前显示的图像
     * 
     * @return 当前显示的BufferedImage
     */
    public BufferedImage getImage() {
        return image;
    }
    
    /**
     * 设置是否保持图像原始宽高比
     * 
     * @param maintainRatio true表示保持宽高比，false表示拉伸填充
     */
    public void setMaintainAspectRatio(boolean maintainRatio) {
        this.maintainAspectRatio = maintainRatio;
        repaint();
    }
    
    /**
     * 重写paintComponent方法来绘制图像
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 如果没有图像，只绘制背景
        if (image == null) {
            return;
        }
        
        // 获取面板大小
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        
        // 图像原始大小
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        
        // 计算绘制位置和大小
        int x = 0;
        int y = 0;
        int width = panelWidth;
        int height = panelHeight;
        
        // 如果需要保持宽高比
        if (maintainAspectRatio) {
            double panelRatio = (double) panelWidth / panelHeight;
            double imgRatio = (double) imgWidth / imgHeight;
            
            if (imgRatio > panelRatio) {
                // 图像比面板更宽，宽度占满，高度按比例计算
                width = panelWidth;
                height = (int) (width / imgRatio);
                y = (panelHeight - height) / 2; // 垂直居中
            } else {
                // 图像比面板更高，高度占满，宽度按比例计算
                height = panelHeight;
                width = (int) (height * imgRatio);
                x = (panelWidth - width) / 2; // 水平居中
            }
        }
        
        // 绘制图像
        g.drawImage(image, x, y, width, height, null);
    }
} 