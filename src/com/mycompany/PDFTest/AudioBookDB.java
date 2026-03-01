package com.mycompany.pdftest;

import java.util.List;

/**
 * Manages the persistence of user data, including the library of 
 * processed books and their respective bookmarks.
 */
public class AudioBookDB {
    /** Intent: A data structure representing a single book's saved state. */
    public class AudioBook {
        public String filePath;
        public int currentChunk;
        public List<String> bookMakredText;
    }

    /** Intent: Deserialize the AudioBookDB.json file into Java objects. */
    private void load() {}

    /** Intent: Serialize the current list of books into the AudioBookDB.json file. */
    public void save() {}

    /** Intent: Record the user's current reading position in a specific PDF. */
    public void upDateCurrentChunk(String filePath, int currentChunk) {}
}