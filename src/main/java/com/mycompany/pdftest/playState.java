package com.mycompany.pdftest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author elimo
 */
//This classes job keeps track of the current chunk, checks if a reload is needed for the gui, and keeps track of if the playback is true
public class playState {

    private int currentChunk;

    private boolean isPlaying;

    private int endChunk;
    private int startChunk;

    private int loadedRange = 100;
    private int reloadRange = 30;

    private ArrayList<String> fullBook;
    private ArrayList<Audio> loadWindowAudio;

    private String audioCachDirPath = "/cach";

    public void prefetchAndCleanUP() {
        //TODO: Need to check if some chunks need to be removed

        File directory = new File(audioCachDirPath);

        File[] files = directory.listFiles();

        List<File> filesToDelete = null;

        for (File file : files) {

            // This checks if the file name is large enough to even be an .mp3 file
            //5 is the minimum number of cars in a .mp4 file
            String fullFileName = file.getName();
            if (fullFileName.length() < 5) {
                filesToDelete.add(file);
                continue;
            }

            int chunkNum = Integer.parseInt(fullFileName.substring(0, fullFileName.length() - 4));
           
            // TODO - Need to Add support for choosing the cash size
            
            int cachSize = 5;
            // If the file needs to be removed. 4 is the number of chars in ".mp3"
            if (fullFileName.substring(fullFileName.length() - 4) != ".mp3" || chunkNum <= currentChunk - cachSize || chunkNum >= currentChunk + cachSize) {
                filesToDelete.add(file);
            }

        }

        for (File file : filesToDelete) {
            if (file.delete()) {
                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        }
        
        //TODO: need to add the prefetching logic.
    }

    public playState(int currentChunk, ArrayList<String> fullBook, int loadedRange, int reloadRange) {
        this.loadedRange = loadedRange;
        this.reloadRange = reloadRange;
        this.currentChunk = currentChunk;
        this.fullBook = fullBook;
        startChunk = Math.max(0, (currentChunk - loadedRange));
        endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));
        this.loadWindowAudio = new ArrayList<>();

    }

    public void getAudio() {

    }

    public void setCurrentChunk(int currentChunk) {

        System.out.println("This is the full book size " + fullBook.size());
        this.currentChunk = currentChunk;
        // I need to find some way to reset the the view.

    }

    public int getCurrentChunk() {
        return currentChunk;
    }

    public int getStartChunk() {
        return startChunk;
    }

    public boolean reloadCheck() {
        int startRelaodRange = Math.max(0, (currentChunk - reloadRange));
        int endRelaodRange = Math.min(fullBook.size() - 1, (currentChunk + reloadRange));

        System.out.println("reloadCheck: currentChunk=" + currentChunk + " startChunk=" + startChunk + " endChunk=" + endChunk
                + " reloadRange=(" + startRelaodRange + "," + endRelaodRange + ")");

        if (endRelaodRange > endChunk || startRelaodRange < startChunk) {
            System.out.println("This is the reload Range then the end Chunk" + (currentChunk + reloadRange) + " > " + endChunk);
            System.out.println("This is the reload Range then the start Chunk" + (currentChunk - reloadRange) + " < " + startChunk);

            return true;
        }
        return false;
    }

    // Might want to move to GUI.
    public ArrayList<String> reloadChunks() {

        //System.out.println("This is the full book" + fullBook);
        // Math.min is the easiest way to keep it inbounds
        startChunk = Math.max(0, currentChunk - loadedRange);
        endChunk = Math.min(fullBook.size() - 1, currentChunk + loadedRange);

        //System.out.println("These are the start and end chunks: " + startChunk + ", " + endChunk);
        ArrayList<String> loadWindowText = new ArrayList<>();

        for (int i = startChunk; i <= endChunk; i++) {
            loadWindowText.add(fullBook.get(i));

            //TODO:
            // I need to add the extra info we will get from the settings json file.
            //loadWindowAudio.add(new Audio(fullBook.get(i)));
            //System.out.println("These are the Chunks loaded " + fullBook.get(i));
        }
        return loadWindowText;
    }

    public void setPlayState(boolean playState) {
        this.isPlaying = playState;
    }

    public boolean getPlayState() {
        return isPlaying;
    }
}
