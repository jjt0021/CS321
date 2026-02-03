/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author elimo
 */
public class Settings {

    private final File settingsFile = new File("AudioBookDB.json");// This should not change, but stll makes it easier to change if we decide to have a different dir for steaming and file exporation.
    private final Gson gson = new Gson();
    // private Type type = new TypeToken<java.util.List<AudioBookDB.AudioBook>>() {}.getType();
    private SettingsValues settings;

    public Settings() {
        load();
    }

    public class TTSmodel {

        public String URL = "https://api.openai.com/v1/audio/speech";
        public java.util.List<String> voices = new ArrayList<>();
        public String name = "";
        public String apiKey = "";
    }

    public class SettingsValues {

        public boolean showProgressBar = true;
        public String voice = "Alloy";
        public int reloadRange = 30;
        public int loadedRange = 100;
        public java.util.List<TTSmodel> TTSmodelList = new ArrayList<>();
    }

    public TTSmodel loadModel(String modelName) {
        // Finds if the model is in the list and if not it makes a new one
        for (TTSmodel ttsModel : settings.TTSmodelList) {
            if (modelName.equals(ttsModel.name)) {
                return ttsModel;
            }
        }
        return new TTSmodel();
    }

    // This needs save gaurds, so the user can not input bad values.
    public void save() {
        try (Writer writer = new FileWriter(settingsFile)) {
            gson.toJson(settings, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audiobooks", e);
        }
    }

    // Need to change to real settings file getter. Right now it is still like AudioBookDB
    private void load() {

        // In case there is no file
        if (settingsFile.exists() && settingsFile.length() > 0) {
            try (Reader reader = new FileReader(settingsFile)) {

                settings = gson.fromJson(reader, SettingsValues.class);

                // In case the file is empty - Can happen if all audioBOoks are deleted
                if (settings == null) {
                    settings = new SettingsValues();
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load Settings", e);
            }
        } else {
            settings = new SettingsValues();
        }
    }

}
