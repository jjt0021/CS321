package com.mycompany.pdftest;

import java.io.File;
import java.util.ArrayList;
import com.mycompany.pdftest.Audio;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.pdftest.Settings;
import com.mycompany.pdftest.Settings.SettingsValues;
import com.mycompany.pdftest.Settings.TTSmodel;

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

    Settings initialSettingObj;
    SettingsValues initialSettings = initialSettingObj.getSettingsValues();
    TTSmodel initialModle;

    private int currentChunk;

    private boolean isPlaying;

    private int endChunk;
    private int startChunk;

    private int loadedRange = 100;
    private int reloadRange = 30;
    Map<Integer, Audio> cachedWindow = new HashMap<>();

    private final ArrayList<String> fullBook;

    private String audioCachDirPath = "/cach";
    private String bookName;

    public void prefetchAndCleanUP() {
        File directory = new File(audioCachDirPath);

        File[] files = directory.listFiles();

        //TODO add error message.
        if (files == null) {
            return;
        }

        ArrayList<File> filesToDelete = new ArrayList<>();
        // TODO - Need to Add support for choosing the cash size
        int cachSize = 5;

        for (File file : files) {

            // This checks if the file name is large enough to even be an .mp3 file
            //5 is the minimum number of cars in a .mp4 file
            String fullFileName = file.getName();
            if (fullFileName.length() < 5) {
                filesToDelete.add(file);
                continue;
            }

            // Check for the correct extention
            String extention = fullFileName.substring(fullFileName.length() - 4);
            if (!extention.equals(".mp3")) {
                filesToDelete.add(file);
                continue;
            }

            // Gets the chunk number and the File name.\
            // I chose to manually handel the cach becuase it works better when reloading
            // The only reason the name is not just the chunk number, is so the if you exit the program and go back to the same bookafter you log back in, it will still have the cached chunks.
            String fileName = fullFileName.substring(5, fullFileName.length() - 4);

            String[] parts = fileName.split("_chunk_");
            String audioFileBookName = parts[0];              // just the book name
            int chunkNum = Integer.parseInt(parts[1]);  // just the chunk number

            // Checks if the chunk is in the cach range.
            if (chunkNum <= currentChunk - cachSize || chunkNum >= currentChunk + cachSize) {
                filesToDelete.add(file);
                cachedWindow.remove(chunkNum); // remove from memory too

                continue;
            }

            // Checks if the chunk is in the correct book. This is mainly for when you first load the book
            if (audioFileBookName != bookName) {
                filesToDelete.add(file);
                continue;
            }

        }

        // Removes all the files that need to be removed.
        for (File file : filesToDelete) {
            if (file.delete()) {

                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        }

        //TODO: need to add the prefetching logic.
        for (int i = currentChunk; i <= currentChunk + cachSize; i++) {
            if (cachedWindow.containsKey(i)) {
                continue;
            }

            //TODO: add audio settings
            //cachedWindow.put(i, new Audio());
        }

    }

    public playState(int currentChunk, ArrayList<String> fullBook, int loadedRange, int reloadRange, String bookName) {
        this.loadedRange = loadedRange;
        this.reloadRange = reloadRange;
        this.currentChunk = currentChunk;
        this.fullBook = fullBook;
        startChunk = Math.max(0, (currentChunk - loadedRange));
        endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));
        this.bookName = bookName;

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
