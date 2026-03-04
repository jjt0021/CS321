package com.mycompany.pdftest;

import java.io.File;
import java.util.ArrayList;
import com.mycompany.pdftest.Audio;
import com.mycompany.pdftest.Settings.TTSmodel;
import java.util.HashMap;
import java.util.Map;


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
    int cacheSize = 5;
    private TTSmodel initalModel;
    private Map<Integer, Audio> cachedWindow = new HashMap<>();

    private final ArrayList<String> fullBook;

    private String audioCachDirPath = "/cach";
    private String bookName;
    private String voice;

    public playState(int currentChunk, ArrayList<String> fullBook, int loadedRange, int reloadRange, int cacheSize, String bookName, TTSmodel initalModel, String voice) {
        this.loadedRange = loadedRange;
        this.reloadRange = reloadRange;
        this.currentChunk = currentChunk;
        this.cacheSize = cacheSize;
        this.fullBook = fullBook;
        startChunk = Math.max(0, (currentChunk - loadedRange));
        endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));
        this.bookName = bookName;
        this.initalModel = initalModel;
        this.voice = voice;
    }

    public boolean isIncorectExtention(File file) {
        int minFileExLength = 5;
        String fileName = file.getName();

        if (fileName.length() < minFileExLength) {
            return true;
        }

        String extention = fileName.substring(fileName.length() - minFileExLength - 1);
        if (!extention.equals(".mp3")) {
            return true;
        }

        return false;
    }

    public boolean isIncorrectBook(File file) {

        String fullFileName = file.getName();
        String fileName = fullFileName.substring(5, fullFileName.length() - 4);

        String[] parts = fileName.split("_chunk_");
        String audioFileBookName = parts[0];              // just the book name

        if (audioFileBookName != bookName) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isOutOfCacheWindow(File file) {
        String fullFileName = file.getName();
        String fileName = fullFileName.substring(5, fullFileName.length() - 4);

        String[] parts = fileName.split("_chunk_");

        int chunkNum = Integer.parseInt(parts[1]);
        if (chunkNum <= currentChunk - cacheSize || chunkNum >= currentChunk + cacheSize) {
            
            // It would be really annoing to add this anyware else
            cachedWindow.remove(chunkNum);
            return true;
        } else {
            return false;
        }

    }

    public void deleteFiles(ArrayList<File> filesToDelete) {

        for (File file : filesToDelete) {
            if (file.delete()) {

                System.out.println("File deleted successfully");
            } else {
                System.out.println("Failed to delete the file");
            }
        }
    }

    public void updateCachWindow() {
        for (int i = currentChunk; i <= currentChunk + cacheSize; i++) {
            if (cachedWindow.containsKey(i)) {
                continue;
            }

            int chunkInt = i;
            cachedWindow.put(i, new Audio(fullBook.get(chunkInt), voice, initalModel.URL, initalModel.name, chunkInt, bookName));
        }

    }

    public void prefetchAndCleanUP() {
        File directory = new File(audioCachDirPath);
        File[] files = directory.listFiles();

        //TODO add error message.
        if (files == null) {
            return;
        }

        ArrayList<File> filesToDelete = new ArrayList<>();

        // Checks if the files need to be deleted
        for (File file : files) {

            if (isIncorectExtention(file)) {
                filesToDelete.add(file);
                continue;
            }

            if (isIncorrectBook(file)) {
                filesToDelete.add(file);
                continue;
            }

            if (isOutOfCacheWindow(file)) {
                filesToDelete.add(file);
                continue;
            }

            if (isOutOfCacheWindow(file)) {
                filesToDelete.add(file);
                continue;
            }

        }
        deleteFiles(filesToDelete);

        updateCachWindow();

    }

    public void setCurrentChunk(int currentChunk) {
        
        //TODO: need to update the current chunk in AudioBookDB
        System.out.println("This is the full book size " + fullBook.size());
        this.currentChunk = currentChunk;
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
