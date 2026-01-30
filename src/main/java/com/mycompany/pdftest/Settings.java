/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author elimo
 */
public class Settings {
    
    public boolean showProgressBar = true;
    public String voice = "Alloy";
    public int reloadRange = 30;
    public int loadedRange = 100;
    
    private static final Gson settings = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public void save(String filePath) throws IOException {
        try (FileWriter SettingsWriter = new FileWriter(filePath)) {
            settings.toJson(this, SettingsWriter);
        }
    }

    public static Settings load(String filePath) throws IOException {
        try (FileReader settingsReader = new FileReader(filePath)) {
            return settings.fromJson(settingsReader, Settings.class);
        }
    } 
    
    
    
    
    public void settingGUI(){}
    
}

/**************** THIS IS THE CODE TO LOAD THE SETTINGS *********************


        Settings settings;
        try {
            settings = Settings.load("settings.json");
        } catch (IOException e) {
            settings = new Settings(); // defaults
            settings.save("settings.json");
        }


 ******************** To save settings *****************************
 * Settings settings = new Settings(); // you need to create a settings object before modifications
 * settings.save("settings.json");

**/