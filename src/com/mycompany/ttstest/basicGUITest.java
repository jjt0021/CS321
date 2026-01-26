package com.mycompany.guitest;


import javax.swing.*;
import java.awt.*;

public class GUITest {

    public static void main(String[] args) {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Audio Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize((width), height);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        
        for (int i = 1; i <= 30; i++) {
            // Used HTML here becuase it helps with auto text wrapping
            JButton button = new JButton("<html><div align='left'>" + "This is" + "jjjjjjjjjjjjjjjjjjjjjjjjjj a test" + i + "</div></html>");            
            button.putClientProperty("chunkNum", i);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setOpaque(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            // would like to make them dynamicly resizeable, but very hard.
            button.setMaximumSize(new Dimension((int) Math.round(width * 0.8), 1000));
            
            button.addActionListener(e->{
                JButton source = (JButton) e.getSource();
                int chunkNum = (int) source.getClientProperty("chunkNum");
                System.out.println(chunkNum);
            });
            panel.add(button);
       
        }

        JScrollPane scrollPane = new JScrollPane(panel);

        frame.add(scrollPane);
        frame.setVisible(true);

       
    }
}