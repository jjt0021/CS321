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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
        private PlayState playState;
        private static JPanel panel;
    private static JScrollPane scrollPane;
        public gui(PlayState playState){
            this.playState = playState;
        }
    
        
        public static JScrollPane makeScrollPane(ArrayList<String> window, PlayState playState) {
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
                scrollPane.setViewportView(panel); // <-- important!
            }

            panel.revalidate();
            panel.repaint();
            scrollPane.revalidate();
            scrollPane.repaint();

            return scrollPane;
        }
        
    
        public static JLayeredPane make(JFrame frame, PlayState playstate) throws IOException{
        
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
    
    
    public static class PlayState {
        private int currentChunk;
        
        private boolean isPlaying;
        
        private int endChunk;
        private int startChunk;
        
        private int loadedRange = 100;
        private int reloadRange = 30;
        
        private ArrayList<String> fullBook;
        
        public PlayState(int currentChunk, ArrayList<String> fullBook){
            this.currentChunk = currentChunk;
            this.fullBook = fullBook;
            startChunk = Math.max(0, (currentChunk - loadedRange));
            endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));

        }
        
        public void setCurrentChunk(int currentChunk){
            
            System.out.println("This is the full book size " + fullBook.size());
            this.currentChunk = currentChunk;
            // I need to find some way to reset the the view.
            
            if (reloadCheck()) {
                SwingUtilities.invokeLater(() -> {
                    gui.makeScrollPane(reloadChunks(), this);
                });
            }

        
        }
        
        public int getCurrentChunk(){
            return currentChunk;
        }

        public int getStartChunk(){
            return startChunk;
        }

        public boolean reloadCheck(){
            int startRelaodRange = Math.max(0, (currentChunk - reloadRange));
            int endRelaodRange = Math.min(fullBook.size() - 1, (currentChunk + reloadRange));

            System.out.println("reloadCheck: currentChunk=" + currentChunk + " startChunk=" + startChunk + " endChunk=" + endChunk
                    + " reloadRange=(" + startRelaodRange + "," + endRelaodRange + ")");

            if (endRelaodRange > endChunk || startRelaodRange < startChunk){
                System.out.println("This is the reload Range then the end Chunk" + (currentChunk + reloadRange) + " > " + endChunk);
                System.out.println("This is the reload Range then the start Chunk" + (currentChunk - reloadRange) + " < " + startChunk);

                return true;
            }
            return false;
        }
        
        public ArrayList<String> reloadChunks() {
            
            //System.out.println("This is the full book" + fullBook);

            // Math.min is the easiest way to keep it inbounds
            startChunk = Math.max(0, currentChunk - loadedRange);
            endChunk = Math.min(fullBook.size() - 1, currentChunk + loadedRange);
            
            //System.out.println("These are the start and end chunks: " + startChunk + ", " + endChunk);
            ArrayList<String> loadWindowText = new ArrayList<>();

            for(int i = startChunk; i <= endChunk; i++){
                loadWindowText.add(fullBook.get(i));
                //System.out.println("These are the Chunks loaded " + fullBook.get(i));
            }
            return loadWindowText;
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
            

            return new ArrayList<>(combined);
        }
    }
    
    public class Audio {
                
        private Clip clip;
        private long clipPosition;
        private String fileURL;
        
        public void requestAudio(){
        // This will be where we request audio.
        
        
        
        
        
        }

        public void setFileURL(String path){
            this.fileURL = path;
        }
        
        public String getFileURL(){
            return fileURL;
        }
        
        
        public void playAuido() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
            File audioFile = new File(fileURL);
            
            AudioInputStream chunk = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(chunk);
            
            clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
              System.out.println("audio is finished");
              clip.close();
            }
            });
      
        }
        
        public void resumeAudio(){
            clip.setMicrosecondPosition(clipPosition);
            clip.start();
        }
        public void pauseAudio(){
            clipPosition = clip.getMicrosecondPosition();
            clip.stop();
            
        }
        
  }
    
    public static void main(String[] args) throws IOException, InterruptedException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Audio Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);

        String pdfPath = "src/main/java/com/mycompany/pdftest/Challenges and Strategies for African American Women in Higher Ed _ Free Essay Example.pdf";
        File pdf = new File(pdfPath);
        ArrayList<String> book = new ArrayList<>();
        book = TextUtils.splitText(getTextFromPdf(pdf));
        

        // TempVar for testing
        //0 Is just for testing later it will be changed to get from the saved data.
        PlayState test = new PlayState(0, book);
        
        JLayeredPane panel = gui.make(frame, test);
        frame.setContentPane(panel);
        
        
        frame.setVisible(true);

        

        
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
