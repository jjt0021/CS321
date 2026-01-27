/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.pdftest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static javax.swing.JLayeredPane.PALETTE_LAYER;
/**
 *
 * @author elimo
 */
public class PDFTest {

    public static String getTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }//Tell-Tale_Heart.pdf

    public static class gui {
    
        public static JScrollPane makeScrollPane(ArrayList<String> window){
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int width = (int) screenSize.getWidth();
            int height = (int) screenSize.getHeight();
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.DARK_GRAY);

            System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));

            for(int i = 0; i <= window.size() - 1; i++) {
                // Used HTML here becuase it helps with auto text wrapping
                JButton button = new JButton("<html><div align='left'>" + window.get(i) + "</div></html>");            
                button.putClientProperty("chunkNum", i);
                button.setFocusPainted(false);// highlights what you click on
                button.setContentAreaFilled(false); // IMPORTANT- Can change the button color, important of highlighting
                button.setHorizontalAlignment(SwingConstants.LEFT);

                //button.setBorderPainted(false); highlights what you hover over
                //button.setOpaque(false); as far as I can tell does nothing.

                // would like to make them dynamicly resizeable, but very hard.
                button.setMaximumSize(new Dimension((int) Math.round(width * 0.9), 1000));
                button.setForeground(Color.WHITE);
                button.addActionListener(e->{
                    JButton source = (JButton) e.getSource();
                    int chunkNum = (int) source.getClientProperty("chunkNum");
                    System.out.println(chunkNum);
                });
                panel.add(button);

            }

            JScrollPane scrollPane = new JScrollPane(panel);
            return scrollPane;

        }
    
    }
    
    
    public static class PlayState {
        private int currentChunk;
        private boolean isPlaying;
        
        private int endChunk;
        private int startChunk;
        
        private int loadedRange = 100;
        private int reloadRange = 30;
        
        private ArrayList<String> fullBook;
        private ArrayList<String> loadWindowText = new ArrayList<>();
        
        public PlayState(int currentChunk, ArrayList<String> fullBook){
            this.currentChunk = currentChunk;
            this.fullBook = fullBook;
            
        }

        public void reloadCheck(){
            if (currentChunk + reloadRange > endChunk || currentChunk - reloadRange < startChunk){
                reloadChunks(fullBook);
            }
        }
        
        public void reloadChunks(List<String> fullBook) {
            
            // Math.min is the easiest way to keep it inbounds
            startChunk = Math.min(0, currentChunk - loadedRange);
            endChunk = Math.min(fullBook.size() - 1, currentChunk + loadedRange);
            
            for(int i = startChunk; i > endChunk; i++){
                loadWindowText.add(fullBook.get(i));
            }  
        }
        
        public void setPlayState(boolean playState){
            this.isPlaying = playState;
        }
        public boolean getPlayState(){
            return isPlaying;
        }
    }
    
    public class TextUtils {


        public static ArrayList<String> splitText(String inputText) {
            
            String[] chunks = inputText.split("(?<=[.!?])\\s+");
            
            List<String> combined = new ArrayList<>();
            
            int chunkPos = 1;
            int chunkLength = 31;
            int maxChunkLength = 60;
            String combinedText = chunks[0];
            
            
            while (chunkPos < chunks.length) {

                while (combinedText.length() + chunks[chunkPos].length() < chunkLength) {
                    combinedText += chunks[chunkPos];
                    chunkPos++;
                }
                
                // This part is to split the text if it is too long. 
                if (combinedText.length() > maxChunkLength){
                    int stringPos = combinedText.length()/2;
                    while (stringPos < combinedText.length() && combinedText.charAt(stringPos) != ' ') {
                        stringPos++;
                    }
                
                    String firstPart = combinedText.substring(0, stringPos);  
                    combinedText = combinedText.substring(stringPos);  

                    combined.add(firstPart.strip());
                    
                }
                
               
                combined.add(combinedText.strip());
                
                combinedText = chunks[chunkPos];
                chunkPos++;
            }
            

            return new ArrayList<>(Arrays.asList(chunks));
        }
    }
    
    public static void main(String[] args) throws IOException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Audio Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);

        
        //TEST
        String pdfPath = "src/main/java/com/mycompany/pdftest/Tell-Tale_Heart.pdf";
        File pdf = new File(pdfPath);
        ArrayList<String> book = new ArrayList<>();
        book = TextUtils.splitText(getTextFromPdf(pdf));
        
        JScrollPane scrollPane = new JScrollPane(gui.makeScrollPane(book));
        
        
        
        //END TEST
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
        frame.setContentPane(layerWindow);
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

            int btnWidth = (int)(w * 0.1);  
            int btnHeight = (int)(w * 0.1); 
            play.setBounds((int)((w - btnWidth) * .5), h - btnHeight, btnWidth, btnHeight);
            skipChunk.setBounds((int)((w - btnWidth) * .25), h - btnHeight, btnWidth, btnHeight);  
            prevChunk.setBounds((int)((w - btnWidth) * .75), h - btnHeight, btnWidth, btnHeight);  

        }  
        });
        // chatgpt
   
        frame.setVisible(true);

        // TempVar for testing
        PlayState test = new PlayState(0, book);
        
        // The Main LOOP
        boolean playing = true;
        while(playing){
            
        
        }
        
    /*
       ************ IMPORTANT - AUTO SCROLL CODE ******************* 

        // Auto Scroll Test
        JViewport viewport = scrollPane.getViewport();
        Point p = viewport.getViewPosition();

        p.y += 100;   // move down 50 pixels
        viewport.setViewPosition(p);
        
        
        **/
    }


}
