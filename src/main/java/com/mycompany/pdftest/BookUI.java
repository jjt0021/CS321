/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import static javax.swing.JLayeredPane.PALETTE_LAYER;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.mycompany.pdftest.Settings.SettingsValues;

/**
 *
 * @author elimo
 */
public class BookUI {

    private PlayState playState;
    private Settings settingsObject;
    private SettingsValues loadedValues;
    private AppController controller;

    //need to make it not staic so each book gets a fresh UI
    private JPanel panel;
    private JScrollPane scrollPane;

    // TODO: load models.
    public BookUI(PlayState playState, Settings settingsObject) {
        this.playState = playState;
        this.settingsObject = settingsObject;
        this.loadedValues = settingsObject.getSettingsValues();
        this.controller = null;

    }
    
    /**
     * Sets the controller reference for MVC communication
     * @param controller the AppController instance
     */
    public void setController(AppController controller) {
        this.controller = controller;
    }

    public JScrollPane makeScrollPane(ArrayList<String> window, PlayState playState) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();

        // Create panel once
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.DARK_GRAY);
        }

        // Reload content
        panel.removeAll();

        // This makes all of the buttons and adds them to the scroll pane.
        for (int i = 0; i < window.size(); i++) {
            JButton button = new JButton("<html><div align='left'>" + window.get(i) + "</div></html>");

            // I had a bug where I forgot to add startChunk to i resulting it not reloading properly.
            // ChatGPT added these line
            int absoluteIndex = playState.getStartChunk() + i;
            button.putClientProperty("chunkNum", absoluteIndex);
            // End of chat

            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension((int) (width * 0.99), 1000));
            button.setForeground(Color.WHITE);

            button.addActionListener(e -> {
                int chunkNum = (int) ((JButton) e.getSource()).getClientProperty("chunkNum");
                
                // Use controller to handle chunk selection (MVC pattern)
                if (controller != null) {
                    controller.onChunkSelected(chunkNum);
                } else {
                    // Fallback for backward compatibility
                    playState.setCurrentChunk(chunkNum);
                    System.out.println("Current chunk updated: " + chunkNum);

                    if (playState.reloadCheck()) {
                        makeScrollPane(playState.reloadChunks(), playState);
                    }
                }
            });

            panel.add(button);
        }

        // Create scrollPane once
        if (scrollPane == null) {
            scrollPane = new JScrollPane(panel);
        } else {
            scrollPane.setViewportView(panel);
        }

        panel.revalidate();
        panel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();

        return scrollPane;
    }

    /**
     * Update the scroll pane with new content (called by Controller)
     * This separates view updates from direct model access
     * @param window the new text chunks to display
     */
    public void updateScrollPane(ArrayList<String> window) {
        makeScrollPane(window, playState);
    }

  

    public JLayeredPane makePane(JFrame frame, PlayState playstate, AppController controller) throws IOException {
        // Set controller reference for MVC communication
        setController(controller);

        // Call the method directly without wrapping it in a new JScrollPane
        JScrollPane localScrollPane = makeScrollPane(playstate.reloadChunks(), playstate);


        // ========== Top Navigation Buttons ==========
        JButton closeButton = new JButton("X");
        closeButton.setFocusPainted(true);
        closeButton.setContentAreaFilled(true);
        closeButton.setBackground(Color.LIGHT_GRAY);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(closeButton.getFont().deriveFont(14f));
        closeButton.setToolTipText("Back to File Manager");
        
        closeButton.addActionListener(e -> {
            if (controller != null) {
                controller.showFileManagerView();
            }
        });

        JButton settingsButton = new JButton("⚙ Settings");
        settingsButton.setFocusPainted(true);
        settingsButton.setContentAreaFilled(true);
        settingsButton.setBackground(Color.LIGHT_GRAY);
        settingsButton.setForeground(Color.BLACK);
        settingsButton.setToolTipText("Open Settings");
        
        settingsButton.addActionListener(e -> {
            if (controller != null) {
                controller.showSettingsView();
            }
        });

        // ============== Bottom navigation Buttons =================
        // This section is for all of the action buttons like play pause etc.
        JButton play = new JButton("PLAY");
        play.setFocusPainted(true);
        play.setContentAreaFilled(true); 
        play.setBackground(Color.red);
        
        // Add play button action listener using controller
        play.addActionListener(e -> {
            if (controller != null) {
                boolean currentPlayState = playstate.getPlayState();
                controller.onPlayStateChanged(!currentPlayState);
            }
        });

        // FIXED: Corrected the variable names for prev and skip buttons
        JButton prevChunk = new JButton("PREV");
        prevChunk.setFocusPainted(true);
        prevChunk.setContentAreaFilled(true); 
        prevChunk.setBackground(Color.red);

        JButton skipChunk = new JButton("SKIP");
        skipChunk.setFocusPainted(true);
        skipChunk.setContentAreaFilled(true); 
        skipChunk.setBackground(Color.red);

        JLayeredPane layerWindow = new JLayeredPane();
        layerWindow.add(localScrollPane, JLayeredPane.DEFAULT_LAYER); // FIXED: Added localScrollPane here
        layerWindow.add(closeButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(settingsButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(play, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(skipChunk, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(prevChunk, JLayeredPane.PALETTE_LAYER);

        // --- FIXED SIZING LOGIC ---
        // Create a reusable block of logic for sizing the components
        Runnable updateLayout = () -> {
            int w = frame.getContentPane().getWidth();
            int h = frame.getContentPane().getHeight();
            
            // Fallback just in case the frame hasn't rendered its dimensions yet
            if (w == 0 || h == 0) { w = 800; h = 600; }

            layerWindow.setBounds(0, 0, w, h);
            localScrollPane.setBounds(0, 0, w, h);

            // Top button dimensions
            int topBtnWidth = (int) (w * 0.08);
            int topBtnHeight = (int) (h * 0.05);
            int topPadding = 10;
            
            closeButton.setBounds(topPadding, topPadding, topBtnWidth, topBtnHeight);
            settingsButton.setBounds(w - topBtnWidth - topPadding, topPadding, topBtnWidth + 20, topBtnHeight);

            // Bottom button dimensions
            int btnWidth = (int) (w * 0.1);
            int btnHeight = (int) (w * 0.1);
            play.setBounds((int) ((w - btnWidth) * .5), h - btnHeight, btnWidth, btnHeight);
            skipChunk.setBounds((int) ((w - btnWidth) * .25), h - btnHeight, btnWidth, btnHeight);
            prevChunk.setBounds((int) ((w - btnWidth) * .75), h - btnHeight, btnWidth, btnHeight);
        };

        // 1. Run it immediately so components aren't 0x0
        updateLayout.run();

        // 2. Add listener to handle both resizing and the moment the screen is shown
        layerWindow.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayout.run();
            }
            
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Triggers the exact moment CardLayout brings this pane to the front
                updateLayout.run();
            }
        });

        return layerWindow;
    }
}
