/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.mycompany.pdftest.controller.AppController;
import com.mycompany.pdftest.model.persistence.Settings;
import com.mycompany.pdftest.model.persistence.Settings.SettingsValues;
import com.mycompany.pdftest.model.state.PlayState;

/**
 *
 * @author elimo
 * This class's job is to manage the UI for listening to audio books.
 */
public class BookUI {

    private final PlayState playState;
    private final Settings settingsObject;
    private final SettingsValues loadedValues;
    private AppController controller;

    //need to make it not staic so each book gets a fresh UI
    private JPanel panel;
    private JScrollPane scrollPane;
    private Map<Integer, JButton> chunkButtons = new HashMap<>();
    private JButton playButton;  // Store reference to play button for dynamic updates

    public BookUI(PlayState playState, Settings settingsObject) {
        this.playState = playState;
        this.settingsObject = settingsObject;
        this.loadedValues = settingsObject.getSettingsValues();
        this.controller = null;

    }

    /**
     * This sets the controller reference for communication.
     *
     * @param controller the {@link AppController} instance
     */
    public void setController(AppController controller) {
        this.controller = controller;
    }

    /**
     * Creates the scrollable text panel with clickable chunks.
     * Builds buttons for each text chunk with status indicators and colors.
     * @param window the list of text chunks to display
     * @param playState the current playback state
     * @return scrollable pane containing the chunk buttons
     */
    public JScrollPane makeScrollPane(ArrayList<String> window, PlayState playState) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();

        // Create panel once
        if (panel == null) {
            panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBackground(Color.DARK_GRAY);
        }

        // Reload content
        panel.removeAll();
        chunkButtons.clear();

        // This makes all of the buttons and adds them to the scroll pane.
        for (int i = 0; i < window.size(); i++) {
            // Calculate absolute chunk index using the start of the current window
            int absoluteIndex = playState.getStartChunk() + i;

            // Get audio state for this chunk
            com.mycompany.pdftest.model.audio.Audio.AudioState audioState = playState.getAudioState(absoluteIndex);
            String statusIndicator = getStatusIndicator(audioState);
            String buttonText = "<html><div align='left'>" + statusIndicator + " " + window.get(i) + "</div></html>";

            JButton button = new JButton(buttonText);
            button.putClientProperty("chunkNum", absoluteIndex);
            button.putClientProperty("chunkText", window.get(i));  // Store original text for updates

            button.setFocusPainted(false);
            button.setContentAreaFilled(false);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setMaximumSize(new Dimension((int) (width * 0.99), 1000));

            // Set button color based on audio state
            button.setForeground(getStatusColor(audioState));

            button.addActionListener(e -> {
                int chunkNum = (int) ((JButton) e.getSource()).getClientProperty("chunkNum");

                // Use controller to handle chunk selection (MVC pattern)
                if (controller != null) {
                    controller.onChunkSelected(chunkNum);
                } else {
                    // Fallback for backward compatibility
                    playState.setCurrentChunk(chunkNum);
                    System.out.println("Current chunk updated: " + chunkNum);

                    if (playState.reloadCheck()) {
                        makeScrollPane(playState.reloadChunks(), playState);
                    }
                }
            });

            chunkButtons.put(absoluteIndex, button);
            panel.add(button);
        }

        // Create scrollPane once
        if (scrollPane == null) {
            scrollPane = new JScrollPane(panel);
        } else {
            scrollPane.setViewportView(panel);
        }

        panel.revalidate();
        panel.repaint();
        scrollPane.revalidate();
        scrollPane.repaint();

        return scrollPane;
    }

    /**
     * Update the scroll pane with new content (called by controller). This
     * separates view updates from direct model access.
     *
     * @param window the new text chunks to display
     */
    public void updateScrollPane(ArrayList<String> window) {
        makeScrollPane(window, playState);
        highlightCurrentChunk(playState.getCurrentChunk());
    }

    /**
     * This gets the status indicator, so the user has some idea what is happening
     * with the HTTP request for audio.
     */
    /**
     * Get the unicode symbol for the current audio state.
     * @param state the audio state
     * @return symbol representing the state (✓, ⏳, ✗, ◯, etc.)
     */
    private String getStatusIndicator(com.mycompany.pdftest.model.audio.Audio.AudioState state) {
        switch (state) {
            case READY:
                return "✓";
            case GENERATING:
                return "⏳";
            case FAILED:
                return "✗";
            case MISSING:
                return "◯";
            case PLAYING:
                return "▶";
            case PAUSED:
                return "⏸";
            case STOPPED:
                return "⏹";
            default:
                return "?";
        }
    }

    /**
     * Get the display color for a chunk based on its audio state.
     * Shows user if text is ready, generating, failed, or listened to.
     * @param state the audio state
     * @return color to display for this chunk
     */
    private Color getStatusColor(com.mycompany.pdftest.model.audio.Audio.AudioState state) {
        switch (state) {
            case READY:
                return new Color(0, 200, 0); // Green - ready to play
            case GENERATING:
                return new Color(255, 200, 0); // Yellow - currently generating
            case FAILED:
                return new Color(255, 100, 100); // Red - error
            case MISSING:
                return Color.LIGHT_GRAY; // Gray - not started
            case PLAYING:
                return Color.WHITE; // White - playing
            case PAUSED:
                return Color.CYAN; // Cyan - paused
            case STOPPED:
                return Color.WHITE; // White - stopped
            default:
                return Color.WHITE;
        }
    }

    /**
     * Highlight the current chunk being played with a yellow background.
     * @param currentChunkNum the chunk number to highlight
     */
    public void highlightCurrentChunk(int currentChunkNum) {
        // Unhighlight all buttons
        for (JButton button : chunkButtons.values()) {
            button.setBackground(Color.DARK_GRAY);
            button.setContentAreaFilled(false);
            button.setForeground(Color.WHITE);
        }

        // Highlight the current chunk button
        if (chunkButtons.containsKey(currentChunkNum)) {
            JButton currentButton = chunkButtons.get(currentChunkNum);
            currentButton.setBackground(Color.YELLOW);
            currentButton.setContentAreaFilled(true);
            currentButton.setForeground(Color.BLACK);
        }
    }

    /**
     * Create a bookmark note for the current chunk.
     * Opens a dialog for user to enter a note, then saves it.
     */
    public void bookmarkCurrentChunk() {
        int currentChunk = playState.getCurrentChunk();
        String bookmarkNote = JOptionPane.showInputDialog(
                null,
                "Enter a note for this bookmark:",
                "Bookmark Chunk " + currentChunk,
                JOptionPane.PLAIN_MESSAGE
        );

        if (bookmarkNote != null && !bookmarkNote.trim().isEmpty()) {
            if (controller != null) {
                controller.onBookmarkCreated(currentChunk, bookmarkNote);
            }
            JOptionPane.showMessageDialog(
                    null,
                    "Bookmark created at chunk " + currentChunk,
                    "Bookmark Added",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Display all saved bookmarks in a dialog.
     * Let user click a bookmark to jump to that chunk.
     */
    public void showBookmarks() {
        if (controller == null) {
            JOptionPane.showMessageDialog(null, "Controller not initialized", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get bookmarks from controller
        java.util.List<Integer> bookmarkChunks = controller.getBookmarkChunks();
        java.util.List<String> bookmarkNotes = controller.getBookmarkNotes();

        if (bookmarkChunks == null || bookmarkChunks.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No bookmarks found",
                    "Bookmarks",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Create a dialog to show bookmarks
        JDialog bookmarkDialog = new JDialog((JFrame) null, "Bookmarks", true);
        bookmarkDialog.setSize(400, 300);
        bookmarkDialog.setLocationRelativeTo(null);
        bookmarkDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel bookmarkPanel = new JPanel();
        bookmarkPanel.setLayout(new BoxLayout(bookmarkPanel, BoxLayout.Y_AXIS));
        bookmarkPanel.setBackground(Color.DARK_GRAY);

        // Add each bookmark as a clickable button
        for (int i = 0; i < bookmarkChunks.size(); i++) {
            int chunkNum = bookmarkChunks.get(i);
            String note = (bookmarkNotes != null && i < bookmarkNotes.size()) ? bookmarkNotes.get(i) : "No note";

            JButton bookmarkBtn = new JButton("Chunk " + chunkNum + ": " + note);
            bookmarkBtn.setFocusPainted(false);
            bookmarkBtn.setContentAreaFilled(true);
            bookmarkBtn.setBackground(Color.LIGHT_GRAY);
            bookmarkBtn.setForeground(Color.BLACK);
            bookmarkBtn.setMaximumSize(new Dimension(380, 40));
            bookmarkBtn.setAlignmentX(JButton.LEFT_ALIGNMENT);

            final int finalChunkNum = chunkNum;
            bookmarkBtn.addActionListener(e -> {
                // Set current chunk to bookmark
                playState.setCurrentChunk(finalChunkNum);

                // Check if view needs to be reloaded
                if (playState.reloadCheck()) {
                    updateScrollPane(playState.reloadChunks());
                }

                // Highlight the bookmarked chunk
                highlightCurrentChunk(finalChunkNum);

                // Close the bookmark dialog
                bookmarkDialog.dispose();

                System.out.println("Jumped to bookmarked chunk: " + finalChunkNum);
            });

            bookmarkPanel.add(bookmarkBtn);
            bookmarkPanel.add(javax.swing.Box.createVerticalStrut(5));
        }

        JScrollPane scrollPane = new JScrollPane(bookmarkPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bookmarkDialog.add(scrollPane);
        bookmarkDialog.setVisible(true);
    }

    /**
     * This handles most of the GUI creation.
     *
     * @param frame the main {@link JFrame}
     * @param playstate the {@link PlayState} model
     * @param controller the {@link AppController}
     * @return {@link JLayeredPane} containing the book UI components
     * @throws IOException if an I/O error occurs
     */
    public JLayeredPane makePane(JFrame frame, PlayState playstate, AppController controller) throws IOException {
        // Set controller reference for MVC communication
        setController(controller);

        // Call the method directly without wrapping it in a new JScrollPane
        JScrollPane localScrollPane = makeScrollPane(playstate.reloadChunks(), playstate);

        // Highlight the current chunk on initial load
        highlightCurrentChunk(playstate.getCurrentChunk());

        // ========== Top Navigation Buttons ==========
        JButton closeButton = new JButton("X");
        closeButton.setFocusPainted(true);
        closeButton.setContentAreaFilled(true);
        closeButton.setBackground(Color.LIGHT_GRAY);
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(closeButton.getFont().deriveFont(14f));
        closeButton.setToolTipText("Back to File Manager");

        closeButton.addActionListener(e -> {
            if (controller != null) {
                controller.showFileManagerView();
            }
        });

        JButton settingsButton = new JButton("⚙ Settings");
        settingsButton.setFocusPainted(true);
        settingsButton.setContentAreaFilled(true);
        settingsButton.setBackground(Color.LIGHT_GRAY);
        settingsButton.setForeground(Color.BLACK);
        settingsButton.setToolTipText("Open Settings");

        settingsButton.addActionListener(e -> {
            if (controller != null) {
                controller.showSettingsView();
            }
        });

        JButton bookmarkCurrentButton = new JButton("📌 Bookmark Current Chunk");
        bookmarkCurrentButton.setFocusPainted(true);
        bookmarkCurrentButton.setContentAreaFilled(true);
        bookmarkCurrentButton.setBackground(Color.LIGHT_GRAY);
        bookmarkCurrentButton.setForeground(Color.BLACK);
        bookmarkCurrentButton.setToolTipText("Bookmark Current Chunk");

        bookmarkCurrentButton.addActionListener(e -> {
            bookmarkCurrentChunk();
        });

        JButton showBookmarksButton = new JButton("📖 Show Bookmarks");
        showBookmarksButton.setFocusPainted(true);
        showBookmarksButton.setContentAreaFilled(true);
        showBookmarksButton.setBackground(Color.LIGHT_GRAY);
        showBookmarksButton.setForeground(Color.BLACK);
        showBookmarksButton.setToolTipText("View All Bookmarks");

        showBookmarksButton.addActionListener(e -> {
            showBookmarks();
        });

        /**
         * ============== Bottom navigation Buttons ================= These
         * handel the bottom navigation buttons, which mostly 
         * have to do with the TTS models
         */
        // This section is for all of the action buttons like play pause etc.
        playButton = new JButton(updatePlayButtonText(playstate.getPlayState()));
        playButton.setFocusPainted(true);
        playButton.setContentAreaFilled(true);
        playButton.setBackground(Color.red);

        // Add play button action listener using controller
        playButton.addActionListener(e -> {
            if (controller != null) {
                boolean currentPlayState = playstate.getPlayState();
                controller.onPlayStateChanged(!currentPlayState);
                // Update button text immediately
                playButton.setText(updatePlayButtonText(!currentPlayState));
                // Refresh status indicators
                updateChunkStatusIndicators();
            }
        });

        // FIXED: Corrected the variable names for prev and skip buttons
        JButton prevChunk = new JButton("PREV");
        prevChunk.setFocusPainted(true);
        prevChunk.setContentAreaFilled(true);
        prevChunk.setBackground(Color.red);

        JButton skipChunk = new JButton("SKIP");
        skipChunk.setFocusPainted(true);
        skipChunk.setContentAreaFilled(true);
        skipChunk.setBackground(Color.red);

        JLayeredPane layerWindow = new JLayeredPane();
        layerWindow.add(localScrollPane, JLayeredPane.DEFAULT_LAYER); // FIXED: Added localScrollPane here
        layerWindow.add(closeButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(settingsButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(bookmarkCurrentButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(showBookmarksButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(playButton, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(skipChunk, JLayeredPane.PALETTE_LAYER);
        layerWindow.add(prevChunk, JLayeredPane.PALETTE_LAYER);

        // --- FIXED SIZING LOGIC ---
        // Create a reusable block of logic for sizing the components
        Runnable updateLayout = () -> {
            int w = frame.getContentPane().getWidth();
            int h = frame.getContentPane().getHeight();

            // Fallback just in case the frame hasn't rendered its dimensions yet
            if (w == 0 || h == 0) {
                w = 800;
                h = 600;
            }

            layerWindow.setBounds(0, 0, w, h);
            localScrollPane.setBounds(0, 0, w, h);

            // Top button dimensions
            int topBtnWidth = (int) (w * 0.08);
            int topBtnHeight = (int) (h * 0.05);
            int topPadding = 10;

            closeButton.setBounds(topPadding, topPadding, topBtnWidth, topBtnHeight);

            // Stack buttons on the right side
            int rightBtnWidth = (int) (w * 0.15);
            int rightBtnHeight = (int) (h * 0.04);
            int rightX = w - rightBtnWidth - topPadding;
            int topY = topPadding;

            settingsButton.setBounds(rightX, topY, rightBtnWidth, rightBtnHeight);
            topY += rightBtnHeight + 5;

            bookmarkCurrentButton.setBounds(rightX, topY, rightBtnWidth, rightBtnHeight);
            topY += rightBtnHeight + 5;

            showBookmarksButton.setBounds(rightX, topY, rightBtnWidth, rightBtnHeight);

            // Bottom button dimensions
            int btnWidth = (int) (w * 0.1);
            int btnHeight = (int) (w * 0.1);
            playButton.setBounds((int) ((w - btnWidth) * .5), h - btnHeight, btnWidth, btnHeight);
            skipChunk.setBounds((int) ((w - btnWidth) * .25), h - btnHeight, btnWidth, btnHeight);
            prevChunk.setBounds((int) ((w - btnWidth) * .75), h - btnHeight, btnWidth, btnHeight);
        };

        // 1. Run it immediately so components aren't 0x0
        updateLayout.run();

        // 2. Add listener to handle both resizing and the moment the screen is shown
        layerWindow.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateLayout.run();
            }

            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Triggers the exact moment CardLayout brings this pane to the front
                updateLayout.run();
            }
        });

        return layerWindow;
    }

    /**
     * Update the play button text based on the current play state
     *
     * @param isPlaying true if currently playing
     * @return the button text
     */
    private String updatePlayButtonText(boolean isPlaying) {
        return isPlaying ? "PAUSE" : "PLAY";
    }

    /**
     * Refresh status indicators for all visible chunks Called when audio states
     * change to show real-time updates
     */
    public void updateChunkStatusIndicators() {
        for (Map.Entry<Integer, JButton> entry : chunkButtons.entrySet()) {
            int chunkNum = entry.getKey();
            JButton button = entry.getValue();

            // Get the original text content stored in the button
            String textContent = (String) button.getClientProperty("chunkText");
            if (textContent == null) {
                textContent = "";
            }

            // Get the current audio state for this chunk
            com.mycompany.pdftest.model.audio.Audio.AudioState audioState = playState.getAudioState(chunkNum);
            String statusIndicator = getStatusIndicator(audioState);

            // Create new button text with updated indicator
            String newButtonText = "<html><div align='left'>" + statusIndicator + " " + textContent + "</div></html>";
            button.setText(newButtonText);
            button.setForeground(getStatusColor(audioState));
        }
    }

    /**
     * Update the play/pause button text based on current play state
     *
     * @param isPlaying true if currently playing
     */
    public void updatePlayButtonState(boolean isPlaying) {
        if (playButton != null) {
            playButton.setText(updatePlayButtonText(isPlaying));
        }
    }
}
