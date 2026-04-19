package com.mycompany.pdftest.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mycompany.pdftest.controller.AppController;
import com.mycompany.pdftest.model.persistence.AudioBookDB;

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

    /**
     * Make the GUI for managing PDFs.
     */
    public JPanel makeGUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        // Top section: Title and Add Button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.DARK_GRAY);
        JLabel titleLabel = new JLabel("Your Audiobooks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JButton addButton = new JButton("+ Add PDF");
        addButton.setFocusPainted(true);
        addButton.setContentAreaFilled(true);
        addButton.setBackground(Color.LIGHT_GRAY);
        addButton.setForeground(Color.BLACK);
        addButton.addActionListener(e -> uploadFile());

        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalStrut(20)); // Spacing
        topPanel.add(addButton);

        // Center section: List of files
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.DARK_GRAY);
        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        refreshFileList();

        return mainPanel;
    }

    /**
     * Opens a file chooser to "upload" a PDF into the system.
     */
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

    /**
     * Refreshes the display whenever a file is added or removed.
     */
    public void refreshFileList() {
        listPanel.removeAll();

        // Get screen dimensions for sizing
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int itemHeight = 80; // Fixed height for each book item

        List<AudioBookDB.AudioBook> files = getFiles();
        if (files.isEmpty()) {
            JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            emptyPanel.setBackground(Color.DARK_GRAY);
            JLabel emptyLabel = new JLabel("No PDFs uploaded yet. Click '+ Add PDF' to start.");
            emptyLabel.setForeground(Color.WHITE);
            emptyPanel.add(emptyLabel);
            listPanel.add(emptyPanel);
        } else {
            for (AudioBookDB.AudioBook book : files) {
                // Skip entries with null file paths, Important if there are no books
                if (book.filePath == null || book.filePath.trim().isEmpty()) {
                    continue;
                }

                JPanel itemPanel = new JPanel(new BorderLayout());
                itemPanel.setBackground(Color.DARK_GRAY);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                itemPanel.setMaximumSize(new Dimension((int) (screenWidth * 0.99), itemHeight));
                itemPanel.setPreferredSize(new Dimension((int) (screenWidth * 0.99), itemHeight));

                File f = new File(book.filePath);
                JLabel nameLabel = new JLabel(f.getName());
                nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                nameLabel.setForeground(Color.WHITE);

                JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                btnPanel.setBackground(Color.DARK_GRAY);
                JButton playBtn = new JButton("Open");
                playBtn.setFocusPainted(true);
                playBtn.setContentAreaFilled(true);
                playBtn.setBackground(Color.RED);
                playBtn.setForeground(Color.WHITE);

                JButton delBtn = new JButton("Delete");
                delBtn.setFocusPainted(true);
                delBtn.setContentAreaFilled(true);
                delBtn.setBackground(Color.LIGHT_GRAY);
                delBtn.setForeground(Color.BLACK);

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

    /**
     * Get the files from the {@link AudioBookDB} to display.
     * @return list of AudioBook files
     */
    public List<AudioBookDB.AudioBook> getFiles() {
        return audioBookDB.getAudioBooks();
    }

    /**
     * Add a new file path to the {@link AudioBookDB}.
     * @param path the file path to add
     */
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

    /**
     * Delete a specific file path from the {@link AudioBookDB}.
     * @param path the file path to delete
     */
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
