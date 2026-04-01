package com.mycompany.pdftest;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;

import javax.swing.JFrame;

/**
 *
 * @author elimo
 */
public class PDFTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Audio Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);

        String pdfPath = "src/main/java/com/mycompany/pdftest/Challenges and Strategies for African American Women in Higher Ed _ Free Essay Example.pdf";
        
        // ========== MVC Architecture ==========
        // Initialize the controller (main coordinator between Model and View)
        AppController appController = new AppController(frame);
        
        // Initialize models with data
        appController.initializeModels(pdfPath);
        
        // Initialize views
        appController.initializeViews();
        
        // Show the application
        frame.setVisible(true);
    }

}

/*
Implementation Notes

We should update the current chunk with highlighting
The current chunk will be updated in the highlighted chunk function
We should create the adio objects when we make the 

The file explorer should handle checking if the pdf file is good.

MVC Architecture Overview:
- AppController: Coordinates all interactions between Models and Views
- Models: PlayState, Settings, AudioBookDB, Audio (manage application data)
- Views: BookUI, SettingsUI (display data and handle user input)
- Events: Views communicate with Controller, Controller updates Models and notifies Views

Auto Scroll Implementation (Future):
It would be nice to implement this, but might be way too difficult.
// JViewport viewport = scrollPane.getViewport();
// Point p = viewport.getViewPosition();
// p.y += 100;   // move down 50 pixels
// viewport.setViewPosition(p);

**/
