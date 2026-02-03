package com.mycompany.pdftest;

import com.mycompany.pdftest.Settings;
import com.mycompany.pdftest.Settings.SettingsValues;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author elimo
 */
public class SettingsGui {


    static JScrollPane createSettingsGUI(SettingsValues initialSettings) {

        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // This is to enable and disable the progress bar. 
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(new JLabel("Enable Progress Bar:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JCheckBox notificationsCheck = new JCheckBox();

        settingsPanel.add(notificationsCheck, gbc);

        row++;

        // This is for the chunk loader
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("Set Chunk Loaded Range"), gbc);

        int initialLoadedRange = 100;
        int minLoadedRange = 25;
        int maxLoadedRange = 500;
        int step = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner loadedRange = new JSpinner(new SpinnerNumberModel(initialSettings.loadedRange, minLoadedRange, maxLoadedRange, step));
        gbc.fill = GridBagConstraints.NONE;  // Don't fill space horizontally

        settingsPanel.add(loadedRange, gbc);
        row++;

        // This is for the chunk loader
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("Set Chunk Reloaded Range"), gbc);

        int initialReloadedRange = 100;
        int minReloadedRange = 25;
        int maxReloadedRange = 500;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner reloadedRange = new JSpinner(new SpinnerNumberModel(initialSettings.reloadRange, minReloadedRange, maxReloadedRange, step));

        settingsPanel.add(reloadedRange, gbc);
        row++;

        // 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // This is for the different TTS voices 
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("TTS Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JComboBox<String> themeBox = new JComboBox<>(
                new String[]{"alloy", "echo", "sage"}
        );

        settingsPanel.add(themeBox, gbc);

        row++;

        // *************** Start of model area *******************
        // I think a drop down menue of models, with the last option being new model would be nice.
        // All we would need to change is the defulat value and which object it saves to.
        // TTS Addr
        // This is for the different TTS mdoels 
        gbc.gridwidth = 1; // Take up one column
        gbc.gridheight = 1; // Take up one row
        gbc.anchor = GridBagConstraints.CENTER; // Horizontally center the component
        gbc.fill = GridBagConstraints.NONE; // No filling (no stretching)
        gbc.gridy = row;

        settingsPanel.add(
                new JLabel("TTS Model Settings"), gbc);
        row++;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("TTS Model:"), gbc);

        // These are the varibles for the text feilds for URL, modelname, and key. I am delcarling them up here so I can set the defualt value in the drop down menue.
        JTextField TTSURL = new JTextField();
        JTextField voices = new JTextField();
        JTextField modelName = new JTextField();

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JComboBox<String> modelSelector = new JComboBox<>(
                new String[]{"GPT", "Kokoro", "VoxCPM", "New"}
        );

        modelSelector.addActionListener(e
                -> {
            String selectedItem = (String) modelSelector.getSelectedItem();

            //TODO: I need to acually implemenmt this with the settings
            TTSURL.setText(selectedItem);
            voices.setText(selectedItem);
            modelName.setText(initialSettings.TtsModel);

        }
        );
        settingsPanel.add(modelSelector, gbc);
        row++;

        settingsPanel.add(
                new JLabel("TTS Addr:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("TTS Addr:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        settingsPanel.add(TTSURL, gbc);

        row++;

        // Voice Names list
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("Voices:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        settingsPanel.add(voices, gbc);

        row++;

        // Model Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("Model Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        settingsPanel.add(modelName, gbc);

        row++;

        // Spaceing to make sure the content does not gather at the bottom  
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 1.0;

        settingsPanel.add(Box.createVerticalGlue(), gbc);

        JScrollPane settingsScrollMenue = new JScrollPane(settingsPanel);

        settingsScrollMenue.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        // The Save Button
        JButton saveButton = new JButton("Save Settings");

        saveButton.addActionListener(e
                -> {

            // Get the settings from all of the boxes
            boolean progressBarEnabled = notificationsCheck.isSelected();
            int chunkLoadedRange = (Integer) loadedRange.getValue();
            int chunkReloadedRange = (Integer) reloadedRange.getValue();
            String ttsName = (String) themeBox.getSelectedItem();
            String ttsModel = (String) modelSelector.getSelectedItem();
            String ttsAddr = TTSURL.getText();
            String voicesList = voices.getText();
            String model = modelName.getText();

            // Here you can save the data as needed (e.g., save to a file, database, etc.)
            System.out.println("Progress Bar Enabled: " + progressBarEnabled);
            System.out.println("Chunk Loaded Range: " + chunkLoadedRange);
            System.out.println("Chunk Reloaded Range: " + chunkReloadedRange);
            System.out.println("TTS Name: " + ttsName);
            System.out.println("TTS Model: " + ttsModel);
            System.out.println("TTS Addr: " + ttsAddr);
            System.out.println("Voices: " + voicesList);
            System.out.println("Model Name: " + model);
        }
        );

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 0.0;

        settingsPanel.add(saveButton, gbc);

        return settingsScrollMenue;
    }

}
