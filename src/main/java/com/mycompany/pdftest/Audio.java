/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author elimo
 */
    public class Audio {
        
        private String text;
        boolean isGenerating;
        
        private Clip clip;
        private long clipPosition;
        private String fileURL;
        
        public Audio(String text){
            this.text = text;
            this.isGenerating = true;
        }
        
        public boolean getIsGenerating(){
            return isGenerating;
        }
        
        
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
    
