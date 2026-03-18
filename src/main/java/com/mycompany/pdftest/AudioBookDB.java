/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

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
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author elimo
 */
/*
You should upload and downlaod a list of objects
The data base will store the path to different books, bookmarks, and currentchunks
**/
public class AudioBookDB {

    private final File db = new File("AudioBookDB.json");// This should not change, but stll makes it easier to change if we decide to have a different dir for steaming and file exporation.
    private final Gson gson = new Gson();
    private Type type = new TypeToken<List<AudioBook>>() {
    }.getType();
    private List<AudioBook> audioBooks;

    public class AudioBook {

        public String filePath;
        public int currentChunk = 0;
        public List<String> bookMakredText = new ArrayList<>();
        public List<Integer> bookMarkID = new ArrayList<>();

        public AudioBook (String filePath){
            this.filePath = filePath;
        }
    }


    public AudioBookDB() {
        load();
    }

    private void load() {

        // In case there is no file
        if (db.exists() && db.length() > 0) {
            try (Reader reader = new FileReader(db)) {

                audioBooks = gson.fromJson(reader, type);

                // In case the file is empty - Can happen if all audioBOoks are deleted
                if (audioBooks.equals(null)) {
                    audioBooks = new ArrayList<>();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load audiobooks", e);
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
            gson.toJson(audioBooks, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audiobooks", e);
        }
    }

    public void upDateCurrentChunk(String filePath, int currentChunk) {
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

    public void updateBookMarks(String filePath, int bookMarkID, String bookMarkText) {
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
        audioBooks.add(new AudioBook(filePath));
    }

    public void removeAudioBook(String filePath) {
        AudioBook bookToRemove = null;
        for (AudioBook book : audioBooks) {
            if (book.filePath.equals(filePath)) {
                bookToRemove = book;
                break;
            }
        }
        
        // We need to do this outside of the loop becuase you can not remove an object form a list while loop through it.
        audioBooks.remove(bookToRemove);
    }
}
