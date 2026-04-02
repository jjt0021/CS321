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
 * AppController - Main application controller following MVC architecture
 * 
 * This controller acts as the mediator between Model and View layers:
 * - Initializes the application and loads data
 * - Handles user interactions from views
 * - Updates models based on user actions
 * - Notifies views of model state changes via observer pattern
 * 
 * @author elimo
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
        
        // Check database for saved progress
        int savedChunk = 0;
        for (AudioBookDB.AudioBook book : audioBookDBModel.getAudioBooks()) {
            if (book.filePath.equals(pdfPath)) {
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
        // Create book UI view
        bookUIView = new BookUI(playStateModel, settingsModel);
        bookUIPane = bookUIView.makePane(frame, playStateModel, this);
        
        // Create settings UI view
        settingsUIPane = SettingsUI.createSettingsGUI(settingsModel, this);
        
        // Set up card layout for screen switching
        screens.add(settingsUIPane, "Settings");
        screens.add(bookUIPane, "audioBook");
        
        frame.setContentPane(screens);
        
        
        // create the file manger view 
        FileManagerUI fileManagerUI = new FileManagerUI(this, audioBookDBModel);
        fileManagerUIPane = fileManagerUI.makeGUI();
        
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

        // Add the updated pane back to the CardLayout
        screens.add(bookUIPane, "audioBook");
        
        //Route to the Player view FIRST so it is active on screen
        showAudioBookView(); 
        
        // Force a hard visual refresh AFTER it is brought to the front
        screens.revalidate();
        screens.repaint();
    } 
    
    
// ========== Controller Actions - Called by Views ==========

    /**
     * Handle chunk selection from BookUI
     * @param chunkNum The chunk number selected by user
     */
    public void onChunkSelected(int chunkNum) {
        playStateModel.setCurrentChunk(chunkNum);
        System.out.println("Controller: Current chunk updated to " + chunkNum);
        
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
        System.out.println("Controller: Play state changed to " + shouldPlay);
    }

    /**
     * Handle settings save from SettingsUI
     * @param progressBarEnabled progress bar setting
     * @param chunkLoadedRange loaded range setting
     * @param chunkReloadedRange reload range setting
     * @param cacheSizeInt cache size setting
     * @param voiceSelected selected voice
     * @param ttsAddr TTS API address
     * @param voicesList list of available voices
     * @param modelName model name
     * @param apiKeyValue API key
     */
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
        
        System.out.println("Controller: Settings saved successfully");
    }

    /**
     * Update audiobook database with current progress
     * Called when transitioning away from a book
     */
    public void saveBookProgress() {
        audioBookDBModel.upDateCurrentChunk(currentBookPath, playStateModel.getCurrentChunk());
        System.out.println("Controller: Book progress saved");
    }

    // ========== Getters for models (Views should use these, not direct access) ==========

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
