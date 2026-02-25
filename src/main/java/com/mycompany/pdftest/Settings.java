/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/*
It would have been easier to map the model name to the model object. If some one 
wants they can implement it that way.

**/
package com.mycompany.pdftest;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elimo
 */
// This classes job is to save load and update a settings file.
public class Settings {

    private final File settingsFile = new File("Settings.json");// This should not change, but stll makes it easier to change if we decide to have a different dir for steaming and file exporation.
    private final Gson gson = new Gson();
    // private Type type = new TypeToken<java.util.List<AudioBookDB.AudioBook>>() {}.getType();
    private SettingsValues settings;

    public Settings() {
        load();
        if (settings.TTSmodelList.size() == 0) {
            //TTSmodel temp = 
            settings.TTSmodelList.add(new TTSmodel());
            System.out.println("New TTSModel added");
            System.out.print(settings.TTSmodelList);
        }
        System.out.println("Model found");
        System.out.println(settings.TTSmodelList);

    }

    public SettingsValues getSettingsValues() {
        return settings;
    }

    public void setSettingsValues(SettingsValues settings) {
        this.settings = settings;
    }

    public class TTSmodel {

        public String URL = "https://api.openai.com/v1/audio/speech";
        public java.util.List<String> voices = new ArrayList<>();
        public String name = "New";
        public String apiKey = "";
    }

    public class SettingsValues {

        public boolean showProgressBar = true;
        public String voice = "";
        public String TtsModel = "New";
        public int reloadRange = 30;
        public int loadedRange = 100;
        public List<TTSmodel> TTSmodelList = new ArrayList<>();

    }

    public List<String> modelNameList() {
        List<String> modelNameList = new ArrayList<>();
        for (TTSmodel model : settings.TTSmodelList) {
            modelNameList.add(model.name);
            System.out.println("Name added");
            System.out.println("This is ithe model added" + model.name);

        }
        System.out.println("Modle List");
        System.out.print(modelNameList);
        return modelNameList;
    }

    public TTSmodel getModel(String modelName) {
        for (TTSmodel model : settings.TTSmodelList) {
            if (model.name.equals(modelName)) {
                return model;

            }

        }
        return new TTSmodel();
    }

    public void updateModelList(TTSmodel inputModel) {
        List<String> modelNamesList = modelNameList();
        if (modelNamesList.contains(inputModel.name)) {
            TTSmodel modelToRemove = null;
            // Remove  The same model if it exist. If it is the same model we just add it back after, so it does not matter.
            // WE CAN NOT REMOVE A MODEL WHILE LOOPING OVER EVERY ITEM IN A LIST
            for (TTSmodel model : settings.TTSmodelList) {
                if (model.name.equals(inputModel.name)) {
                    modelToRemove = model;
                    System.out.println("This is the model that is being removed - " + inputModel.name);
                    break;
                }
            }

            if (modelToRemove != null) {
                settings.TTSmodelList.remove(modelToRemove);
            }
        }

        System.out.println("A NEW MODEL IS BEING ADDED");
        System.out.println(inputModel);

        settings.TTSmodelList.add(inputModel);

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
    // Those safe gaurds will be in Settings GUI.
    public void save() {
        System.out.println("Save atemped");
        try (Writer writer = new FileWriter(settingsFile)) {
            System.out.println("Save successfull");
            System.out.print(settings);
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
