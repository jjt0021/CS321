/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package githubtest;

import javax.swing.*;
import java.awt.*;

public class GitHubTest extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setColor(Color.BLUE);
        g2d.fillRect(50, 50, 100, 100);
        
        g2d.setColor(Color.BLACK);
        g2d.drawString("Java 21 Setup Verified!", 50, 40);
        
        String version = System.getProperty("java.version");
        g2d.drawString("Running on JDK: " + version, 50, 180);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("CS321 Setup Test");
        frame.add(new GitHubTest()); 
        frame.setSize(300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    } // Added missing brace for main method
} // Added missing brace for class
