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

import com.mycompany.pdftest.Settings.TtsModel;


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
public class PlayState implements Audio.PlaybackListener {

    private int currentChunk;

    private boolean isPlaying = false;

    private int endChunk;
    private int startChunk;

    private int loadedRange = 100;
    private int reloadRange = 30;
    int cacheSize = 5;
    private TtsModel initialModel;
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

    public PlayState(int currentChunk, ArrayList<String> fullBook, int loadedRange, int reloadRange, int cacheSize, String bookName, TtsModel initialModel, String voice) {
        this.loadedRange = loadedRange;
        this.reloadRange = reloadRange;
        this.currentChunk = currentChunk;
        this.cacheSize = cacheSize;
        this.fullBook = fullBook;
        startChunk = Math.max(0, (currentChunk - loadedRange));
        endChunk = Math.min(fullBook.size() - 1, (currentChunk + loadedRange));
        this.bookName = bookName;
        this.initialModel = initialModel;
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
                    Audio audio = new Audio(fullBook.get(idx), voice, initialModel.URL, initialModel.name, idx, bookName, this);
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
            cachedWindow.put(i, new Audio(fullBook.get(chunkInt), voice, initialModel.URL, initialModel.name, chunkInt, bookName, this));
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
            System.out.println("[PlayState] Play state already " + playState + ", no change needed");
            return;
        }

        this.isPlaying = playState;

        if (isPlaying) {
            System.out.println("[PlayState] PLAY requested for chunk " + currentChunk);
            //TODO need to add pause and play things.
            Audio audio = cachedWindow.get(currentChunk);
            if (audio == null) {
                System.out.println("[PlayState] No cached audio for chunk " + currentChunk + ", requesting generation.");
                // create and request audio in background
                Audio newAudio = new Audio(fullBook.get(currentChunk), voice, initialModel.URL, initialModel.name, currentChunk, bookName, this);
                cachedWindow.put(currentChunk, newAudio);
                System.out.println("[PlayState] Audio instance created for chunk " + currentChunk + ", submitting to executor");
                prefetchExecutor.submit(() -> {
                    try {
                        System.out.println("[PlayState] Executor: Calling requestAudio() for chunk " + currentChunk);
                        newAudio.requestAudio();
                        // attempt to start playback when ready
                        if (newAudio.getFileURL() != null) {
                            System.out.println("[PlayState] Executor: Audio ready at " + newAudio.getFileURL() + ", calling playAudio()");
                            newAudio.playAudio();
                        } else {
                            System.err.println("[PlayState] Executor: ERROR - Audio file URL is null after requestAudio()");
                        }
                    } catch (Exception e) {
                        System.err.println("[PlayState] Executor ERROR for chunk " + currentChunk + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } else {
                System.out.println("[PlayState] Found cached audio for chunk " + currentChunk + ", resuming playback");
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

    /**
     * Called when a chunk finishes playing - auto-advances to next chunk if still playing
     * Implements Audio.PlaybackListener interface
     */
    @Override
    public void onChunkFinished(int chunkNumber) {
        System.out.println("[PlayState.onChunkFinished] Chunk " + chunkNumber + " finished, isPlaying=" + isPlaying);
        
        if (!isPlaying) {
            System.out.println("[PlayState.onChunkFinished] Playback not active, not advancing chunk");
            return;
        }
        
        // Check if we're at the last chunk
        if (chunkNumber >= fullBook.size() - 1) {
            System.out.println("[PlayState.onChunkFinished] Reached end of book");
            isPlaying = false;
            return;
        }
        
        // Advance to next chunk and play it
        int nextChunk = chunkNumber + 1;
        System.out.println("[PlayState.onChunkFinished] Auto-advancing from chunk " + chunkNumber + " to chunk " + nextChunk);
        setCurrentChunk(nextChunk);
        
        // Auto-play next chunk
        Audio nextAudio = cachedWindow.get(nextChunk);
        System.out.println("[PlayState.onChunkFinished] Looking for cached audio for chunk " + nextChunk + ", found: " + (nextAudio != null));
        if (nextAudio != null) {
            if (nextAudio.getFileURL() != null) {
                System.out.println("[PlayState.onChunkFinished] Next chunk " + nextChunk + " has file ready, playing now");
                try {
                    nextAudio.playAudio();
                } catch (Exception e) {
                    System.err.println("[PlayState.onChunkFinished] Failed to play next chunk: " + e.getMessage());
                    onPlaybackError("Failed to play chunk " + nextChunk + ": " + e.getMessage());
                }
            } else {
                // Audio file not yet generated, request and play when ready
                System.out.println("[PlayState.onChunkFinished] Next chunk " + nextChunk + " not generated yet, requesting in background");
                prefetchExecutor.submit(() -> {
                    try {
                        if (nextAudio.currentState == Audio.AudioState.MISSING || 
                            nextAudio.currentState == Audio.AudioState.FAILED) {
                            System.out.println("[PlayState.onChunkFinished] Background: Requesting audio for chunk " + nextChunk);
                            nextAudio.requestAudio();
                        }
                        if (nextAudio.getFileURL() != null) {
                            System.out.println("[PlayState.onChunkFinished] Background: Playing chunk " + nextChunk);
                            nextAudio.playAudio();
                        }
                    } catch (Exception e) {
                        System.err.println("[PlayState.onChunkFinished] Background: Failed for chunk " + nextChunk + ": " + e.getMessage());
                        onPlaybackError("Failed to generate chunk " + nextChunk + ": " + e.getMessage());
                    }
                });
            }
        } else {
            System.out.println("[PlayState.onChunkFinished] ERROR: Next chunk " + nextChunk + " not in cache");
        }
    }

    /**
     * Called when a playback error occurs
     * Implements Audio.PlaybackListener interface
     */
    @Override
    public void onPlaybackError(String errorMessage) {
        System.err.println("[PlayState.onPlaybackError] " + errorMessage);
        // Stop playback on error
        isPlaying = false;
    }

    /**
     * Stop current chunk playback before switching chunks
     */
    public void stopCurrentPlayback() {
        System.out.println("[PlayState] stopCurrentPlayback() called for chunk " + currentChunk);
        Audio audio = cachedWindow.get(currentChunk);
        if (audio != null) {
            audio.stopAudio();
            System.out.println("[PlayState] Successfully stopped chunk " + currentChunk);
        } else {
            System.out.println("[PlayState] No audio found for chunk " + currentChunk + " to stop");
        }
    }
}
