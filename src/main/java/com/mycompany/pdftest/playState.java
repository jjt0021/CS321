package com.mycompany.pdftest;

import java.util.ArrayList;
import javax.swing.SwingUtilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author elimo
 */
    public class playState {
        private int currentChunk;
        
        private boolean isPlaying;
        
        private int endChunk;
        private int startChunk;
        
        private int loadedRange = 100;
        private int reloadRange = 30;
        
        private ArrayList<String> fullBook;
        private ArrayList<Audio> loadWindowAudio;

        
        public playState(int currentChunk, ArrayList<String> fullBook){
            this.currentChunk = currentChunk;
            this.fullBook = fullBook;
            startChunk = Math.max(0, (currentChunk - loadedRange));
            endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));
            this.loadWindowAudio = new ArrayList<>();

        }
        
        public void getAudio(){
        
        
        
        
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
                loadWindowAudio.add(new Audio(fullBook.get(i)));

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

