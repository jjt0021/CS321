/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest.model.persistence;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author elimo
 */
/**
 * This class handles saving and loading the audio book database.
 * The database is saved using JSON and uses the {@link Gson} library to save and retrieve data.
 * 
 */
public class AudioBookDB {

    private final File db = new File("AudioBookDB.json");// This should not change, but still makes it easier to change if we decide to have a different dir for streaming and file exploration.
    private final Gson gson = new Gson();
    private Type type = new TypeToken<List<AudioBook>>() {
    }.getType();
    private List<AudioBook> audioBooks;

    public class AudioBook {

        public String filePath;
        public int currentChunk = 0;
        public List<String> bookMakredText = new ArrayList<>();
        public List<Integer> bookMarkID = new ArrayList<>();

        /**
         * Constructor for Gson deserialization
         * Note: This should not be used directly; use AudioBook(String filePath) instead
         */
        public AudioBook() {
            this.filePath = null;
        }

        public AudioBook (String filePath){
            this.filePath = filePath;
        }
    }


    public AudioBookDB() {
        load();
    }

    /**
     * This checks if the database exists and loads it if it does, or creates it if it does not.
     */
    private void load() {

        // In case there is no file
        if (db.exists() && db.length() > 0) {
            try (Reader reader = new FileReader(db)) {

                audioBooks = gson.fromJson(reader, type);

                // In case the file is empty - Can happen if all audiobooks are deleted
                if (audioBooks == null) {
                    audioBooks = new ArrayList<>();
                } else {
                    // Filter out corrupted entries (those with null or empty filePath)
                    List<AudioBook> validBooks = new ArrayList<>();
                    for (AudioBook book : audioBooks) {
                        if (book.filePath != null && !book.filePath.trim().isEmpty()) {
                            validBooks.add(book);
                        } else {
                            System.err.println("Warning: Removing corrupted AudioBook entry with null filePath from database");
                        }
                    }
                    audioBooks = validBooks;
                }
            } catch (IOException e) {
                System.err.println("Failed to read AudioBookDB file: " + e.getMessage());
                audioBooks = new ArrayList<>();
            } catch (JsonSyntaxException e) {
                System.err.println("AudioBookDB file is corrupted or has invalid format: " + e.getMessage());
                System.err.println("Initializing with empty audiobook list. Please check the file format or delete it to reset.");
                audioBooks = new ArrayList<>();
            }
        } else {
            audioBooks = new ArrayList<>();
        }
    }

    public List<AudioBook> getAudioBooks() {
        return audioBooks;
    }


    public void save() {
        try (Writer writer = new FileWriter(db)) {
            // Filter out corrupted entries (those with null or empty filePath)
            List<AudioBook> validBooks = new ArrayList<>();
            for (AudioBook book : audioBooks) {
                if (book.filePath != null && !book.filePath.trim().isEmpty()) {
                    validBooks.add(book);
                } else {
                    System.err.println("Warning: Skipping corrupted AudioBook entry with null filePath");
                }
            }
            // Save only valid entries
            gson.toJson(validBooks, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audiobooks", e);
        }
    }

    /**
     * This updates only the current chunk in the audio book database.
     * 
     * @param filePath the path to the audio book file
     * @param currentChunk the current chunk number to save
     */
    public void upDateCurrentChunk(String filePath, int currentChunk) {
        // Validate filePath
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Cannot update current chunk with null or empty filePath");
            return;
        }
        
        for (AudioBook book : audioBooks) {
            if (book.filePath.equals(filePath)) {
                book.currentChunk = currentChunk;
                save();
                return;
            }
        }

        addAudioBook(filePath);
        save();
    }

    /**
     * This is used to update a bookmark. The text should either be a note or the chunk being bookmarked.
     * @param filePath the path to the audio book file
     * @param bookMarkID the ID of the bookmark
     * @param bookMarkText the text or note for the bookmark
     */
    public void updateBookMarks(String filePath, int bookMarkID, String bookMarkText) {
        // Validate filePath
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Cannot update bookmarks with null or empty filePath");
            return;
        }
        
        for (AudioBook book : audioBooks) {
            if (book.filePath.equals(filePath)) {
                book.bookMakredText.add(bookMarkText);
                book.bookMarkID.add(bookMarkID);
                save();
                return;
            }
        }

        // This is really only need for the current chunk update, but just to be safe
        addAudioBook(filePath);
        save();

    }

    
    public void addAudioBook(String filePath) {
        // Validate filePath before adding
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Cannot add AudioBook with null or empty filePath");
            return;
        }
        audioBooks.add(new AudioBook(filePath));
    }

    public void removeAudioBook(String filePath) {
        // Validate filePath
        if (filePath == null || filePath.trim().isEmpty()) {
            System.err.println("Error: Cannot remove AudioBook with null or empty filePath");
            return;
        }
        
        // Normalize the path for comparison (handle different slash styles and case sensitivity)
        String normalizedFilePath = new File(filePath).getAbsolutePath();
        
        AudioBook bookToRemove = null;
        for (AudioBook book : audioBooks) {
            if (book.filePath != null) {
                // Normalize the book's path too for comparison
                String normalizedBookPath = new File(book.filePath).getAbsolutePath();
                if (normalizedBookPath.equalsIgnoreCase(normalizedFilePath)) {
                    bookToRemove = book;
                    System.out.println("Found book to remove: " + book.filePath);
                    break;
                }
            }
        }
        
        // We need to do this outside of the loop because you can not remove an object from a list while looping through it.
        if (bookToRemove != null) {
            audioBooks.remove(bookToRemove);
            System.out.println("Book removed from database");
            save();
        } else {
            System.out.println("Book not found in database: " + filePath);
        }
    }
}
