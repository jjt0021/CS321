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
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 *
 * This class's job is to save and retrieve settings.
 * Most of the values given are validated in the {@link FileManagerUI} before it reaches this class.
 */
public class Settings {

    private final File settingsFile = new File("Settings.json");// This should not change, but still makes it easier to change if we decide to have a different dir for streaming and file exploration.
    private final Gson gson = new Gson();
    // private Type type = new TypeToken<java.util.List<AudioBookDB.AudioBook>>() {}.getType();
    private SettingsValues settings;

    public Settings() {
        load();
        if (settings.ttsModelList.size() == 0) {
            settings.ttsModelList.add(new TtsModel());
            System.out.println("New TTSModel added");
            System.out.print(settings.ttsModelList);
        }
        System.out.println("Model found");
        System.out.println(settings.ttsModelList);

    }

    public SettingsValues getSettingsValues() {
        return settings;
    }

    public void setSettingsValues(SettingsValues settings) {
        this.settings = settings;
    }

    /**
     * This class is used to save the values for different text to speech models such as:
     *  - the URL for the API
     *  - the list of voices available to the model
     *  - the model name
     *  - the API key (if needed)
     */
    public class TtsModel {

        public String URL = "https://api.openai.com/v1/audio/speech";
        public java.util.List<String> voices = new ArrayList<>();
        public String name = "New";
        public String apiKey = "";
    }

    /**
     * This is the class that holds the values that will be saved to the json file
     * This class is the one that gets passed to all of the others
     */
    public class SettingsValues {

        public boolean showProgressBar = true;
        public String voice = "";
        public String TtsModel = "New";
        public int reloadRange = 30;
        public int loadedRange = 100;
        public int cacheSize = 8;
        @SerializedName("TTSmodelList")
        public List<TtsModel> ttsModelList = new ArrayList<>();

    }

    /**
     * These next methods mainly deal with finding specific models and values for those models.
     * @return list of model names
     */
    public List<String> modelNameList() {
        List<String> modelNameList = new ArrayList<>();
        for (TtsModel model : settings.ttsModelList) {
            modelNameList.add(model.name);
            System.out.println("Name added");
            System.out.println("This is the model added" + model.name);

        }
        System.out.println("Model List");
        System.out.print(modelNameList);
        return modelNameList;
    }

    public TtsModel getModel(String modelName) {
        for (TtsModel model : settings.ttsModelList) {
            if (model.name.equals(modelName)) {
                return model;

            }

        }
        return new TtsModel();
    }

    /*
    This function is used to update the model list, which includes adding new models, and replacing models that have been altered.
    The different models or identified by their names, so we can not have 2 models with the same name.
    **/
    public void updateModelList(TtsModel inputModel) {
        List<String> modelNamesList = modelNameList();
        if (modelNamesList.contains(inputModel.name)) {
            TtsModel modelToRemove = null;
            // Remove  The same model if it exist. If it is the same model we just add it back after, so it does not matter.
            // WE CAN NOT REMOVE A MODEL WHILE LOOPING OVER EVERY ITEM IN A LIST
            for (TtsModel model : settings.ttsModelList) {
                if (model.name.equals(inputModel.name)) {
                    modelToRemove = model;
                    System.out.println("This is the model that is being removed - " + inputModel.name);
                    break;
                }
            }

            if (modelToRemove != null) {
                settings.ttsModelList.remove(modelToRemove);
            }
        }

        System.out.println("A NEW MODEL IS BEING ADDED");
        System.out.println(inputModel);

        settings.ttsModelList.add(inputModel);

    }

    public TtsModel loadModel(String modelName) {
        // Finds if the model is in the list and if not it makes a new one
        for (TtsModel ttsModel : settings.ttsModelList) {
            if (modelName.equals(ttsModel.name)) {
                return ttsModel;
            }
        }
        return new TtsModel();
    }

    // The safeguards for this are in SettingsUI.java
    public void save() {
        System.out.println("Save attempted");
        try (Writer writer = new FileWriter(settingsFile)) {
            System.out.println("Save successful");
            System.out.print(settings);
            gson.toJson(settings, writer);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save audiobooks", e);
        }
    }

    /**
     * This method handles loading the settings from the JSON file, or creating a default file
     * if it does not exist.
     */
    public void load() {

        // In case there is no file
        if (settingsFile.exists() && settingsFile.length() > 0) {
            try (Reader reader = new FileReader(settingsFile)) {

                settings = gson.fromJson(reader, SettingsValues.class);

                // In case the file is empty - Can happen if all audiobooks are deleted
                if (settings == null) {
                    settings = new SettingsValues();
                }
            } catch (IOException e) {
                System.err.println("Failed to read Settings file: " + e.getMessage());
                settings = new SettingsValues();
            } catch (JsonSyntaxException e) {
                System.err.println("Settings file is corrupted or has invalid format: " + e.getMessage());
                System.err.println("Initializing with default settings. Please check the file format or delete it to reset.");
                settings = new SettingsValues();
            }
        } else {
            settings = new SettingsValues();
        }
        
        // In case there are no models.
        if (settings.ttsModelList == null) {
            settings.ttsModelList = new ArrayList<>();
        }
    }

}
