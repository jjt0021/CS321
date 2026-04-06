package com.mycompany.pdftest;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;

/**
 * Responsible for the GUI that allows users to manage their PDF files. 
 */
public class FileManagerUI {

    private AppController controller;
    private AudioBookDB audioBookDB;
    private JPanel listPanel;

    public FileManagerUI(AppController controller, AudioBookDB audioBookDB) {
        this.controller = controller;
        this.audioBookDB = audioBookDB;
    }

    /** Intent: Make the GUI for managing pdfs. */
    public JPanel makeGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Top section: Title and Add Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Your Audiobooks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JButton addButton = new JButton("+ Add PDF");
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> uploadFile());
        
        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalStrut(20)); // Spacing
        topPanel.add(addButton);
        
        // Center section: List of files
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        refreshFileList();
        
        return mainPanel;
    }

    /** Intent: Opens a file chooser to "upload" a PDF into the system */
    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a PDF Book");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        
        int result = fileChooser.showOpenDialog(null);
        
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            addPath(selectedFile.getAbsolutePath());
        }
    }

    /** Intent: Refreshes the display whenever a file is added or removed */
    public void refreshFileList() {
        listPanel.removeAll();
        
        List<AudioBookDB.AudioBook> files = getFiles();
        if (files.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            emptyPanel.add(new JLabel("No PDFs uploaded yet. Click '+ Add PDF' to start."));
            listPanel.add(emptyPanel);
        } else {
            for (AudioBookDB.AudioBook book : files) {
                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                
                File f = new File(book.filePath);
                JLabel nameLabel = new JLabel(f.getName());
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                
                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton playBtn = new JButton("Open");
                JButton delBtn = new JButton("Delete");
                
                playBtn.addActionListener(e -> {
                    try {
                        // Pass the selected file to the controller's new openBook method
                        controller.openBook(book.filePath);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error opening PDF: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace(); // Prints the exact error to the console just in case!
                    }
                });
                
                delBtn.addActionListener(e -> deletePath(book.filePath));
                
                btnPanel.add(playBtn);
                btnPanel.add(delBtn);
                
                itemPanel.add(nameLabel, BorderLayout.CENTER);
                itemPanel.add(btnPanel, BorderLayout.EAST);
                
                listPanel.add(itemPanel);
            }
        }
        
        listPanel.revalidate();
        listPanel.repaint();
    }

    /** Intent: Get the files from the audioBook DB to display. */
    public List<AudioBookDB.AudioBook> getFiles() {
        return audioBookDB.getAudioBooks();
    }

    /** Intent: Add a new file path to the audioBook DB. */
    public void addPath(String filePath) {
        // Check if it already exists to avoid duplicates
        for (AudioBookDB.AudioBook book : getFiles()) {
            if (book.filePath.equals(filePath)) {
                JOptionPane.showMessageDialog(null, "This PDF is already in your library.");
                return;
            }
        }
        audioBookDB.addAudioBook(filePath);
        audioBookDB.save();
        refreshFileList();
    }

    /** Intent: Delete a specific file path from the audioBook DB. */
    public void deletePath(String filePath) {
        int confirm = JOptionPane.showConfirmDialog(null, 
            "Are you sure you want to remove this book from your library?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            audioBookDB.removeAudioBook(filePath);
            audioBookDB.save();
            refreshFileList();
        }
    }
}