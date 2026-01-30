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

/**
 *
 * @author elimo
 */
    public class gui {
        private playState playState;
        private static JPanel panel;
    private static JScrollPane scrollPane;
        public gui(playState playState){
            this.playState = playState;
        }
    
        
        public static JScrollPane makeScrollPane(ArrayList<String> window, playState playState) {
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
                    playState.setCurrentChunk(chunkNum);
                    System.out.println("Current chunk updated: " + chunkNum);
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
        
    
        public static JLayeredPane makePane(JFrame frame, playState playstate) throws IOException{
        
            JScrollPane scrollPane = new JScrollPane(gui.makeScrollPane(playstate.reloadChunks(), playstate));
    
            // This section is for all of the action buttons like play pause etc.
            JButton play = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JButton prevChunk = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JButton skipChunk = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JLayeredPane layerWindow = new JLayeredPane();
            layerWindow.add(scrollPane, JLayeredPane.DEFAULT_LAYER);
            layerWindow.add(play, PALETTE_LAYER);
            layerWindow.add(skipChunk, PALETTE_LAYER);
            layerWindow.add(prevChunk, PALETTE_LAYER);

            // Resizes components
            // chatgpt - with eddits
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    int w = frame.getContentPane().getWidth();
                    int h = frame.getContentPane().getHeight();

                    layerWindow.setBounds(0, 0, w, h);
                    scrollPane.setBounds(0, 0, w, h);

                    int btnWidth = (int) (w * 0.1);
                    int btnHeight = (int) (w * 0.1);
                    play.setBounds((int) ((w - btnWidth) * .5), h - btnHeight, btnWidth, btnHeight);
                    skipChunk.setBounds((int) ((w - btnWidth) * .25), h - btnHeight, btnWidth, btnHeight);
                    prevChunk.setBounds((int) ((w - btnWidth) * .75), h - btnHeight, btnWidth, btnHeight);

                }
            });
            
            return layerWindow;

        }
        
    }