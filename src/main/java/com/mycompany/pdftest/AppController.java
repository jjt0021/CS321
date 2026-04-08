/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.pdftest;

import java.awt.CardLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mycompany.pdftest.Settings.SettingsValues;

/**
This class is the controller, it was added to more closly mirror the mvc model.
That is why this class was added.
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
     * Initialize models and load application data
     * @param pdfPath Path to the PDF file to load
     * @throws IOException if PDF loading fails
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
        currentBook = TextUtils.splitText(TextUtils.getTextFromPdf(pdf));
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
     * Initialize views and set up the UI
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
        settingsUIPane = SettingsUI.createSettingsGUI(settingsModel, this);
        
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
        // Save progress of the current book before switching
        if (currentBookPath != null) {
            saveBookProgress();
        }

        // Initialize models with the new PDF data
        initializeModels(filePath);

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
        // Stop current playback before switching chunks
        playStateModel.stopCurrentPlayback();
        
        playStateModel.setCurrentChunk(chunkNum);
        System.out.println("Current chunk updated to " + chunkNum);
        
        // Highlight the selected chunk button
        bookUIView.highlightCurrentChunk(chunkNum);
        
        // Check if reload is needed and notify view
        if (playStateModel.reloadCheck()) {
            bookUIView.updateScrollPane(playStateModel.reloadChunks());
        }
    }

    /**
     * Handle play/pause from BookUI
     * @param shouldPlay true to play, false to pause
     */
    public void onPlayStateChanged(boolean shouldPlay) {
        playStateModel.setPlayState(shouldPlay);
        System.out.println("Play state changed");
    }

    // =========== Handels Saving Settings ================
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
}
