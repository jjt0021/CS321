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
import javax.swing.JComboBox;
import javax.swing.JPopupMenu;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static javax.swing.JLayeredPane.PALETTE_LAYER;

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
    
    
    
    
    public static JLayeredPane settingGUI(){
    
                    String[] options = { "Red", "Green", "Blue" };
        JComboBox<String> comboBox = new JComboBox<>(options);

                    String[] dialogOptions = {
                    "Message Dialog",
                    "Confirm Dialog",
                    "Input Dialog",
                    "Warning Dialog"
            };

        comboBox.setMaximumSize(new Dimension(250, 25));
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

            // This section is for all of the action buttons like play pause etc.
            JButton play = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JButton prevChunk = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JButton skipChunk = new JButton("PLAY");
            play.setFocusPainted(true);// highlights what you click on
            play.setContentAreaFilled(true); // IMPORTANT- Can change the button color, important of highlighting
            play.setBackground(Color.red);

            JLayeredPane layerWindow = new JLayeredPane();
            layerWindow.setOpaque(true);

           // layerWindow.add(comboBox, JLayeredPane.DEFAULT_LAYER);
            layerWindow.add(play, PALETTE_LAYER);
            layerWindow.add(skipChunk, PALETTE_LAYER);
            layerWindow.add(prevChunk, PALETTE_LAYER);
            layerWindow.setBackground(Color.DARK_GRAY);


          
            
            return layerWindow;
    }

    
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

**/-l\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\