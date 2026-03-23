package com.mycompany.pdftest;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.mycompany.pdftest.Settings.TTSmodel;


/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author elimo
 */


//TODO need to add a the check for it it is fialed, generating or generated.
//This classes job keeps track of the current chunk, checks if a reload is needed for the gui, and keeps track of if the playback is true
public class playState {

    private int currentChunk;

    private boolean isPlaying = false;

    private int endChunk;
    private int startChunk;

    private int loadedRange = 100;
    private int reloadRange = 30;
    int cacheSize = 5;
    private TTSmodel initalModel;
    private ConcurrentMap<Integer, Audio> cachedWindow = new ConcurrentHashMap<>();
    // prefetch/cache fields
    private BlockingQueue<Integer> prefetchQueue = new LinkedBlockingQueue<>();
    private ExecutorService prefetchExecutor;
    private Set<Integer> enqueued = ConcurrentHashMap.newKeySet();
    private Path audioCacheDir = Paths.get("audio_cache");

    private final ArrayList<String> fullBook;

    // audio cache directory is `audioCacheDir` (Path)
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

        // Start of ChatGPT
        // Initialize audio cache directory and start a single-threaded prefetch worker
        try {
            Files.createDirectories(audioCacheDir);
        } catch (IOException ex) {
            System.err.println("Failed to create audio cache dir: " + ex.getMessage());
        }

        prefetchExecutor = Executors.newSingleThreadExecutor();
        prefetchExecutor.submit(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    int idx = prefetchQueue.take(); // blocks until work
                    enqueued.remove(idx);

                    // skip if already cached
                    if (cachedWindow.containsKey(idx)) {
                        continue;
                    }

                    // create Audio instance (uses existing Audio constructor)
                    if (idx < 0 || idx >= fullBook.size()) {
                        continue;
                    }
                    Audio audio = new Audio(fullBook.get(idx), voice, initalModel.URL, initalModel.name, idx, bookName);
                    cachedWindow.put(idx, audio);

                    // perform generation in background using the existing synchronous requestAudio()
                    try {
                        audio.requestAudio(); // requestAudio runs async internals in Audio but may block here
                        // set expected file path in cache (mp3)
                        Path generated = audioCacheDir.resolve(String.format("book_%s_chunk_%d.mp3", bookName, idx));
                        audio.setFileURL(generated.toString());
                    } catch (Exception e) {
                        System.err.println("Audio generation failed for chunk " + idx + ": " + e.getMessage());
                    }

                    // run cleanup periodically
                    prefetchAndCleanUP();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        //END of ChatGPT
    }

    public boolean isIncorectExtention(File file) {
        String fileName = file.getName();
        return (!fileName.endsWith(".mp3"));
    }

    public boolean isIncorrectBook(File file) {
        String fullFileName = file.getName();
        if (!fullFileName.contains("_chunk_") || !fullFileName.startsWith("book_")) {
            return true;
        }

        String fileName = fullFileName.substring(5, fullFileName.length() - 4);
        String[] parts = fileName.split("_chunk_");

        String audioFileBookName = parts[0];

        return (!audioFileBookName.equals(bookName));

    }

    public boolean isOutOfCacheWindow(File file) {
        String fullFileName = file.getName();
        if (!fullFileName.contains("_chunk_") || !fullFileName.startsWith("book_")) {
            return true;
        }

        String fileName = fullFileName.substring(5, fullFileName.length() - 4);
        String[] parts = fileName.split("_chunk_");

        try {
            Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return true;
        }
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
        int from = Math.max(0, currentChunk);
        int to = Math.min(fullBook.size() - 1, currentChunk + cacheSize);
        for (int i = from; i <= to; i++) {
            if (cachedWindow.containsKey(i)) {
                continue;
            }

            int chunkInt = i;
            cachedWindow.put(i, new Audio(fullBook.get(chunkInt), voice, initalModel.URL, initalModel.name, chunkInt, bookName));
        }

    }

    public void prefetchAndCleanUP() {
        // iterate files inside audio cache dir and delete out-of-window files
        ArrayList<File> filesToDelete = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(audioCacheDir)) {
            for (Path p : ds) {
                File file = p.toFile();
                if (isIncorectExtention(file) || isIncorrectBook(file) || isOutOfCacheWindow(file)) {
                    filesToDelete.add(file);
                }
            }
        } catch (IOException e) {
            // directory may not exist or be empty
        }

        if (!filesToDelete.isEmpty()) {
            deleteFiles(filesToDelete);
        }

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
        if (isPlaying == playState) {
            return;
        }

        this.isPlaying = playState;

        if (isPlaying) {
            //TODO need to add pause and play things.
            Audio audio = cachedWindow.get(currentChunk);
            if (audio == null) {
                System.out.println("No cached audio for chunk " + currentChunk + ", requesting generation.");
                // create and request audio in background
                Audio newAudio = new Audio(fullBook.get(currentChunk), voice, initalModel.URL, initalModel.name, currentChunk, bookName);
                cachedWindow.put(currentChunk, newAudio);
                prefetchExecutor.submit(() -> {
                    try {
                        newAudio.requestAudio();
                        // attempt to start playback when ready
                        if (newAudio.getFileURL() != null) {
                            newAudio.playAudio();
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to generate/play audio for chunk " + currentChunk + ": " + e.getMessage());
                    }
                });
            } else {
                audio.resumeAudio();
            }
        } else {
            Audio audio = cachedWindow.get(currentChunk);
            if (audio != null) {
                audio.pauseAudio();
            }

        }
    }

    public boolean getPlayState() {
        return isPlaying;
    }
}
