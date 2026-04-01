package com.mycompany.pdftest;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.List;

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

import com.mycompany.pdftest.Settings.SettingsValues;
import com.mycompany.pdftest.Settings.TtsModel;

/**
 *
 * @author elimo
 */
public class SettingsUI {

    private void addProgressBarBox(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    private void addChunkLoaderRange(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    private void addChunkReloadRang(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    static JScrollPane createSettingsGUI(Settings settings, AppController controller) {

        SettingsValues initialSettings = settings.getSettingsValues();
        TtsModel initialModel = settings.getModel(initialSettings.TtsModel);

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

        int minLoadedRange = 25;
        int maxLoadedRange = 500;
        int step = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner loadedRange = new JSpinner(new SpinnerNumberModel(initialSettings.loadedRange, minLoadedRange, maxLoadedRange, step));
        gbc.fill = GridBagConstraints.NONE;  // Don't fill space horizontally

        settingsPanel.add(loadedRange, gbc);
        row++;

        // This is for Reload range of the GUI
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(new JLabel("Set Chunk Reloaded Range"), gbc);

        int minReloadedRange = 25;
        int maxReloadedRange = 500;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner reloadedRange = new JSpinner(new SpinnerNumberModel(initialSettings.reloadRange, minReloadedRange, maxReloadedRange, step));

        settingsPanel.add(reloadedRange, gbc);
        row++;

        // This is for Reload range of the GUI
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(new JLabel("Set Chunk Reloaded Range"), gbc);

        int minCacheSize = 1;
        int maxCacheSize = 10;
        int cacheStep = 1;

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner cacheSize = new JSpinner(new SpinnerNumberModel(initialSettings.cacheSize, minCacheSize, maxCacheSize, cacheStep));

        settingsPanel.add(cacheSize, gbc);
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
        // Use real voice names from the selected model
        String[] ttsNames = initialModel.voices.toArray(new String[0]);
        JComboBox<String> voiceBox = new JComboBox<>(ttsNames);

        settingsPanel.add(voiceBox, gbc);

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
        JTextField apiKey = new JTextField();

        // In case the model is deleted set it to New
        if (!settings.modelNameList().contains(initialSettings.TtsModel)) {
            initialSettings.TtsModel = "New";
        }

        // Set the Inital Values
        TTSURL.setText(initialModel.URL);
        voices.setText(String.join(", ", initialModel.voices));
        modelName.setText(initialModel.name);
        apiKey.setText(initialModel.apiKey);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JComboBox<String> modelSelector = new JComboBox<>(
                //new String[]{"GPT", "Kokoro", "VoxCPM", "New"}
                // This converts the list to a string of the model names because JCOmboBox does not take strings
                settings.modelNameList().toArray(new String[0])
        );

        System.out.println("This is the Current Model Name that should be deault - " + initialSettings.TtsModel);
        modelSelector.setSelectedItem(initialSettings.TtsModel);

        modelSelector.addActionListener(e
                -> {
            String modelSelectedString = (String) modelSelector.getSelectedItem();

            Settings.TtsModel modelSelectedObject = settings.getModel(modelSelectedString);
            TTSURL.setText(modelSelectedObject.URL);
            apiKey.setText(modelSelectedObject.apiKey);
            voices.setText(String.join(", ", modelSelectedObject.voices));
            modelName.setText(modelSelectedString);
                        // Update the voice combo box to reflect the newly selected model's voices
                        voiceBox.setModel(new javax.swing.DefaultComboBoxModel<>(modelSelectedObject.voices.toArray(new String[0])));
                        if (!modelSelectedObject.voices.isEmpty()) {
                                voiceBox.setSelectedIndex(0);
                        }

        }
        );
        settingsPanel.add(modelSelector, gbc);
        row++;

        // Model Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(new JLabel("Model Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        settingsPanel.add(modelName, gbc);

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

        // Api Key
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        settingsPanel.add(
                new JLabel("API Key:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;

        settingsPanel.add(apiKey, gbc);

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

            System.out.println("A save has been started.");
            // Get the settings from all of the boxes
            boolean progressBarEnabled = notificationsCheck.isSelected();
            int chunkLoadedRange = (Integer) loadedRange.getValue();
            int chunkReloadedRange = (Integer) reloadedRange.getValue();
            int cacheSizeInt = (Integer) cacheSize.getValue();

            String voiceSelected = (String) voiceBox.getSelectedItem();
            String ttsModelSelected = (String) modelSelector.getSelectedItem();

            // New Model Vars
            String ttsAddr = TTSURL.getText();
            String voicesString = voices.getText();
            String TTSModelName = modelName.getText();
            String apiKeyValue = apiKey.getText();

            List<String> voicesList = Arrays.asList(voicesString.split(","));
            for (String voice : voicesList) {
                voice.strip();
            }

            // I need to check if everything is entered correctly.
            // The end user can not name a model new. This is to prevent that
            // This only matter if JComboBox is New
            if (ttsModelSelected.equals("New")) {
                if (settings.modelNameList().contains(initialModel.name)) {

                    // TODO: give the user an error.
                    //Cut out of the saving
                    //Also combine 
                }
            }

            // Use controller to handle settings save (MVC pattern)
            if (controller != null) {
                controller.onSettingsSaved(
                    progressBarEnabled,
                    chunkLoadedRange,
                    chunkReloadedRange,
                    cacheSizeInt,
                    voiceSelected,
                    ttsAddr,
                    voicesList,
                    TTSModelName,
                    apiKeyValue
                );
                // Navigate back to audiobook view after saving
                controller.showAudioBookView();
            } else {
                // Fallback for backward compatibility
                Settings.TtsModel newModel = settings.new TtsModel();
                newModel.URL = ttsAddr;
                newModel.apiKey = apiKeyValue;
                newModel.name = TTSModelName;
                newModel.voices = voicesList;
                settings.updateModelList(newModel);

                // Here you can save the data as needed (e.g., save to a file, database, etc.)
                System.out.println("Progress Bar Enabled: " + progressBarEnabled);
                initialSettings.showProgressBar = progressBarEnabled;

                System.out.println("Chunk Loaded Range: " + chunkLoadedRange);
                initialSettings.loadedRange = chunkLoadedRange;

                System.out.println("Chunk Reloaded Range: " + chunkReloadedRange);
                initialSettings.reloadRange = chunkReloadedRange;

                System.out.println("cacheSize: " + cacheSizeInt);
                initialSettings.cacheSize = cacheSizeInt;

                // All of these requer more work because of the model list.
                System.out.println("TTS Name: " + voiceSelected);
                //System.out.println("TTS Model: " + ttsModel);

                initialSettings.TtsModel = TTSModelName;

                settings.save();
            }
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
