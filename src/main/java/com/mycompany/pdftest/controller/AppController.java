/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.pdftest.controller;

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mycompany.pdftest.model.persistence.AudioBookDB;
import com.mycompany.pdftest.model.persistence.Settings;
import com.mycompany.pdftest.model.persistence.Settings.SettingsValues;
import com.mycompany.pdftest.model.state.PlayState;
import com.mycompany.pdftest.text.TextProcesser;
import com.mycompany.pdftest.view.BookUI;
import com.mycompany.pdftest.view.FileManagerUI;
import com.mycompany.pdftest.view.SettingsUI;

/**
This class is the controller, it was added to more closely mirror the MVC model.
 */
public class AppController {

    private JFrame frame;
    private Settings settingsModel;
    private AudioBookDB audioBookDBModel;
    private PlayState playStateModel;
    private ArrayList<String> currentBook;
    private String currentBookPath;
    private String currentBookName;
    
    // Views
    private BookUI bookUIView;
    private JLayeredPane bookUIPane;
    private JScrollPane settingsUIPane;
    private SettingsUI settingsUIView;
    private FileManagerUI fileManagerUIView;
    
    // UI Components
    private CardLayout cardLayout;
    private JPanel screens;
    
    // Filemanager 
    private JPanel fileManagerUIPane;

    /**
     * Initializes the controller with application framework
     */
    public AppController(JFrame frame) {
        this.frame = frame;
        this.cardLayout = new CardLayout();
        this.screens = new JPanel(cardLayout);
    }

    /**
     * Initialize models and load application data.
     * @param pdfPath Path to the PDF file to load
     * @throws IOException if PDF loading fails
     * @see Settings
     * @see AudioBookDB
     * @see PlayState
     */
    
    public void initializeModels(String pdfPath) throws IOException {
        // Load settings model
        settingsModel = new Settings();
        SettingsValues initialSettings = settingsModel.getSettingsValues();
        
        // Load audio book database model
        audioBookDBModel = new AudioBookDB();
        
        // Load and parse the PDF
        File pdf = new File(pdfPath);
        currentBook = new ArrayList<>();
        currentBook = TextProcesser.splitText(TextProcesser.getTextFromPdf(pdf));
        currentBookPath = pdf.getAbsolutePath();
        currentBookName = pdf.getName();
        
        // Check database for saved progress using absolute path
        int savedChunk = 0;
        for (AudioBookDB.AudioBook book : audioBookDBModel.getAudioBooks()) {
            if (book.filePath.equals(currentBookPath)) {
                savedChunk = book.currentChunk;
                break;
            }
        }
        
        // Initialize play state model
        playStateModel = new PlayState(
            savedChunk,                                           // Uses saved progress instead of 0
            currentBook,                                          
            initialSettings.loadedRange,                          
            initialSettings.reloadRange,                          
            initialSettings.cacheSize,                            
            currentBookName,
            settingsModel.getModel(initialSettings.TtsModel),    
            initialSettings.voice
        );
    }
    /**
     * Initialize models without loading a specific book yet.
     * @throws IOException if an I/O error occurs
     */
    private void initializeModelsEmpty() throws IOException {
        // Load settings model
        settingsModel = new Settings();
        SettingsValues initialSettings = settingsModel.getSettingsValues();
        
        // Load audio book database model
        audioBookDBModel = new AudioBookDB();
        
        // Initialize empty data structures
        currentBook = new ArrayList<>();
        currentBookPath = null;
        currentBookName = null;
        
        // Initialize play state model with empty book
        playStateModel = new PlayState(
            0,                                                  // chunk 0
            currentBook,                                        // empty list
            initialSettings.loadedRange,                        
            initialSettings.reloadRange,                        
            initialSettings.cacheSize,                          
            "No Book Selected",
            settingsModel.getModel(initialSettings.TtsModel),   
            initialSettings.voice
        );
    }

    /**
     * Initialize views without loading a specific book
     * This is called on startup, user will select a book from file manager
     */
    public void initializeViewsWithoutBook() throws IOException {
        // Initialize models first (empty state)
        initializeModelsEmpty();
        
        // Create file manager view first
        fileManagerUIView = new FileManagerUI(this, audioBookDBModel);
        fileManagerUIPane = fileManagerUIView.makeGUI();
        
        // Create book UI view (with empty models)
        bookUIView = new BookUI(playStateModel, settingsModel);
        bookUIPane = bookUIView.makePane(frame, playStateModel, this);
        
        // Set up listener for chunk changes during auto-advance
        playStateModel.setOnChunkChangedListener(newChunk -> {
            bookUIView.highlightCurrentChunk(newChunk);
        });
        
        // Create settings UI view
        settingsUIView = new SettingsUI(settingsModel, this);
        settingsUIPane = settingsUIView.getSettingsPane();
        
        // Set up card layout for screen switching
        screens.add(fileManagerUIPane, "FileManager");
        screens.add(settingsUIPane, "Settings");
        screens.add(bookUIPane, "audioBook");
        
        frame.setContentPane(screens);
    }

    /**
     * Initialize views with a specific book loaded
     * This is the old method, kept for backward compatibility
     */
    public void initializeViews() throws IOException {
        // Create file manager view first
        FileManagerUI fileManagerUI = new FileManagerUI(this, audioBookDBModel);
        fileManagerUIPane = fileManagerUI.makeGUI();
        
        // Create book UI view
        bookUIView = new BookUI(playStateModel, settingsModel);
        bookUIPane = bookUIView.makePane(frame, playStateModel, this);
        
        // Set up listener for chunk changes during auto-advance
        playStateModel.setOnChunkChangedListener(newChunk -> {
            bookUIView.highlightCurrentChunk(newChunk);
        });
        
        // Create settings UI view
        settingsUIView = new SettingsUI(settingsModel, this);
        settingsUIPane = settingsUIView.getSettingsPane();
        
        // Set up card layout for screen switching
        screens.add(fileManagerUIPane, "FileManager");
        screens.add(settingsUIPane, "Settings");
        screens.add(bookUIPane, "audioBook");
        
        frame.setContentPane(screens);
    }

    /**
     * Show the audiobook view
     */
    public void showAudioBookView() {
        cardLayout.show(screens, "audioBook");
    }

    /**
     * Show the settings view
     */
    public void showSettingsView() {
        cardLayout.show(screens, "Settings");
    }

    /**
     * Show the file manager view
     */
    public void showFileManagerView() {
        // Stop playback when exiting the book
        playStateModel.stopCurrentPlayback();
        
        saveBookProgress(); // Keeps your current progress saving logic
        
        // Refresh the file manager list before showing it
        FileManagerUI fileManagerUI = new FileManagerUI(this, audioBookDBModel);
        screens.add(fileManagerUI.makeGUI(), "FileManager");
        
        cardLayout.show(screens, "FileManager");
        System.out.println("Controller: File Manager view displayed");
    }
    
    /**
     * Safely loads a new book and refreshes the UI
     */
    public void openBook(String filePath) throws IOException {
        // Check if file exists before trying to open it
        File pdfFile = new File(filePath);
        if (!pdfFile.exists()) {
            // File not found - remove from database
            audioBookDBModel.removeAudioBook(filePath);
            audioBookDBModel.save();
            
            // Refresh file manager display
            if (fileManagerUIView != null) {
                fileManagerUIView.refreshFileList();
            }
            
            // Notify user
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "The PDF file could not be found:\n" + filePath + "\n\nIt has been removed from your library.",
                "File Not Found",
                javax.swing.JOptionPane.WARNING_MESSAGE
            );
            
            // Show file manager again
            showFileManagerView();
            return;
        }
        
        // Save progress of the current book before switching
        if (currentBookPath != null) {
            saveBookProgress();
        }

        try {
            // Initialize models with the new PDF data
            initializeModels(filePath);
        } catch (IOException e) {
            // If loading fails, show error and return to file manager
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "Error loading PDF file:\n" + e.getMessage(),
                "Error Loading File",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
            showFileManagerView();
            throw e;
        }

        // Remove the old book pane from the CardLayout to prevent stacking
        if (bookUIPane != null) {
            screens.remove(bookUIPane);
        }

        // Generate the new book UI with the newly loaded text
        bookUIView = new BookUI(playStateModel, settingsModel);
        bookUIPane = bookUIView.makePane(frame, playStateModel, this);
        
        // Set up listener for chunk changes during auto-advance
        playStateModel.setOnChunkChangedListener(newChunk -> {
            bookUIView.highlightCurrentChunk(newChunk);
        });

        // Add the updated pane back to the CardLayout
        screens.add(bookUIPane, "audioBook");
        
        //Route to the Player view FIRST so it is active on screen
        showAudioBookView(); 
        
        // Force a hard visual refresh AFTER it is brought to the front
        screens.revalidate();
        screens.repaint();
    } 
    
    
// ========== Controller Actions ==========

    /**
     * Handle chunk selection from BookUI
     * @param chunkNum The chunk number selected by user
     */
    public void onChunkSelected(int chunkNum) {
        // Check if audio is currently playing
        boolean wasPlaying = playStateModel.getPlayState();
        
        // If playing, fully stop the current audio (close it, don't just pause)
        if (wasPlaying) {
            playStateModel.setPlayState(false);  // Pause first
            playStateModel.stopCurrentPlayback();  // Fully close the clip
            System.out.println("Stopped playback of current chunk before switching");
            
            // Give the audio system time to release the old clip
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Change to the new chunk
        playStateModel.setCurrentChunk(chunkNum);
        System.out.println("Current chunk updated to " + chunkNum);
        
        // Highlight the selected chunk button
        bookUIView.highlightCurrentChunk(chunkNum);
        
        // Check if reload is needed and notify view
        if (playStateModel.reloadCheck()) {
            bookUIView.updateScrollPane(playStateModel.reloadChunks());
        }
        
        // Resume playing if it was playing before
        if (wasPlaying) {
            playStateModel.setPlayState(true);  // Resume with new chunk
            System.out.println("Resumed playback at new chunk " + chunkNum);
        }
    }

    /**
     * Handle play/pause from BookUI
     * @param shouldPlay true to play, false to pause
     */
    public void onPlayStateChanged(boolean shouldPlay) {
        playStateModel.setPlayState(shouldPlay);
        System.out.println("Play state changed to: " + shouldPlay);
        
        // Update play button text to reflect new state
        if (bookUIView != null) {
            bookUIView.updatePlayButtonState(shouldPlay);
            bookUIView.updateChunkStatusIndicators();
        }
        
        // If starting playback, start a refresh thread
        if (shouldPlay) {
            startIndicatorRefreshThread();
        }
    }
    
    /**
     * Start a background thread to refresh status indicators periodically
     * This updates the UI to show real-time audio generation and playback state
     */
    private void startIndicatorRefreshThread() {
        new Thread(() -> {
            while (playStateModel.getPlayState()) {
                try {
                    Thread.sleep(500);  // Refresh every 500ms
                    if (bookUIView != null && playStateModel.getPlayState()) {
                        bookUIView.updateChunkStatusIndicators();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }
    
    /**
     * Refresh status indicators on demand
     * Call this when you need to update the UI immediately
     */
    public void refreshStatusIndicators() {
        if (bookUIView != null) {
            bookUIView.updateChunkStatusIndicators();
        }
    }

    // =========== Handles Saving Settings ================
    public void onSettingsSaved(
            boolean progressBarEnabled,
            int chunkLoadedRange,
            int chunkReloadedRange,
            int cacheSizeInt,
            String voiceSelected,
            String ttsAddr,
            java.util.List<String> voicesList,
            String modelName,
            String apiKeyValue
    ) {
        SettingsValues settings = settingsModel.getSettingsValues();
        Settings.TtsModel newModel = settingsModel.new TtsModel();
        
        newModel.URL = ttsAddr;
        newModel.apiKey = apiKeyValue;
        newModel.name = modelName;
        newModel.voices = voicesList;
        
        settingsModel.updateModelList(newModel);
        
        // Update settings values
        settings.showProgressBar = progressBarEnabled;
        settings.loadedRange = chunkLoadedRange;
        settings.reloadRange = chunkReloadedRange;
        settings.cacheSize = cacheSizeInt;
        settings.TtsModel = modelName;
        settings.voice = voiceSelected;
        
        settingsModel.save();
        
        // Update PlayState with the new voice for future audio requests
        if (playStateModel != null) {
            playStateModel.setVoice(voiceSelected);
        }
        
        // Refresh the model dropdown in SettingsUI to show the newly added model
        if (settingsUIView != null) {
            settingsUIView.refreshModelDropdown(settingsModel);
        }
        
        System.out.println("Settings have been saved");
    }

    /**
     * Update audiobook database with current progress
     * Called when transitioning away from a book
     */
    public void saveBookProgress() {
        audioBookDBModel.upDateCurrentChunk(currentBookPath, playStateModel.getCurrentChunk());
        System.out.println("Controller: Book progress saved");
    }

    // ========== Getters for models  ==========

    /**
     * Get the play state model
     * @return PlayState model
     */
    public PlayState getPlayStateModel() {
        return playStateModel;
    }

    /**
     * Get the settings model
     * @return Settings model
     */
    public Settings getSettingsModel() {
        return settingsModel;
    }

    /**
     * Get the audio book database model
     * @return AudioBookDB model
     */
    public AudioBookDB getAudioBookDBModel() {
        return audioBookDBModel;
    }

    /**
     * Get current book text chunks
     * @return ArrayList of book chunks
     */
    public ArrayList<String> getCurrentBook() {
        return currentBook;
    }

    /**
     * Get current book name
     * @return name of current book
     */
    public String getCurrentBookName() {
        return currentBookName;
    }

    /**
     * Get the main frame
     * @return JFrame
     */
    public JFrame getFrame() {
        return frame;
    }

    /**
     * Handle bookmark creation
     * @param chunkNum The chunk number to bookmark
     * @param note The bookmark note/description
     */
    public void onBookmarkCreated(int chunkNum, String note) {
        audioBookDBModel.updateBookMarks(currentBookPath, chunkNum, note);
        System.out.println("Bookmark created at chunk " + chunkNum + ": " + note);
    }

    /**
     * Get all bookmark chunk numbers for current book
     * @return List of bookmark chunk numbers
     */
    public java.util.List<Integer> getBookmarkChunks() {
        for (AudioBookDB.AudioBook book : audioBookDBModel.getAudioBooks()) {
            if (book.filePath.equals(currentBookPath)) {
                return book.bookMarkID;
            }
        }
        return new ArrayList<>();
    }

    /**
     * Get all bookmark notes for current book
     * @return List of bookmark notes
     */
    public java.util.List<String> getBookmarkNotes() {
        for (AudioBookDB.AudioBook book : audioBookDBModel.getAudioBooks()) {
            if (book.filePath.equals(currentBookPath)) {
                return book.bookMakredText;
            }
        }
        return new ArrayList<>();
    }
}
