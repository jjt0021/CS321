package com.mycompany.pdftest.view;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mycompany.pdftest.controller.AppController;
import com.mycompany.pdftest.model.persistence.Settings;
import com.mycompany.pdftest.model.persistence.Settings.SettingsValues;
import com.mycompany.pdftest.model.persistence.Settings.TtsModel;

/**
 * 
 * @author elimo
 */
public class SettingsUI {
    private JScrollPane settingsScrollPane;
    private JComboBox<String> modelSelector;
    private Settings cachedSettings;

    // ==================== Constructor =======================
    /**
     * Creates a new SettingsUI instance with the settings panel.
     * @param settings the {@link Settings} model
     * @param controller the {@link AppController}
     */
    public SettingsUI(Settings settings, AppController controller) {
        this.settingsScrollPane = createSettingsGUIPanel(settings, controller);
    }

    /**
     * Get the settings panel as a scrollable pane.
     * @return {@link JScrollPane} containing the settings UI
     */
    public JScrollPane getSettingsPane() {
        return settingsScrollPane;
    }

    /**
     * Refreshes the model dropdown to reflect newly added models in the Settings model.
     * This method reloads the Settings from disk, updates the dropdown list, and updates the cached reference.
     * @param settings the updated {@link Settings} model
     */
    public void refreshModelDropdown(Settings settings) {
        // Reload settings from disk to get the newly saved model data
        settings.load();
        this.cachedSettings = settings;
        
        if (this.modelSelector != null) {
            // Get the currently selected item before refresh
            String currentSelection = (String) this.modelSelector.getSelectedItem();
            
            // Update the dropdown with new model list
            this.modelSelector.removeAllItems();
            for (String modelName : settings.modelNameList()) {
                this.modelSelector.addItem(modelName);
            }
            
            // If the previously selected model still exists, select it; otherwise select the newly added one (first item)
            if (currentSelection != null && settings.modelNameList().contains(currentSelection)) {
                this.modelSelector.setSelectedItem(currentSelection);
            } else if (!settings.modelNameList().isEmpty()) {
                this.modelSelector.setSelectedIndex(0);
            }
        }
    }

    // ==================== Safety Checks =======================
    private static boolean validateSettings(String ttsAddr, String modelName, String apiKey, String voicesString) {
        // Check if TTS URL is empty
        if (ttsAddr == null || ttsAddr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "TTS API Address cannot be empty.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if TTS URL is valid (contains http:// or https://)
        if (!ttsAddr.startsWith("http://") && !ttsAddr.startsWith("https://")) {
            JOptionPane.showMessageDialog(null, 
                "TTS API Address must start with 'http://' or 'https://'.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if Model name is empty
        if (modelName == null || modelName.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Model Name cannot be empty.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if Model name is "New" (reserved)
        if (modelName.trim().equals("New")) {
            JOptionPane.showMessageDialog(null, 
                "Model Name cannot be 'New' as it is reserved.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if API key is empty
        if (apiKey == null || apiKey.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "API Key cannot be empty.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Check if voices list is empty
        if (voicesString == null || voicesString.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Voices list cannot be empty. Please provide at least one voice (comma-separated).", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    private void addProgressBarBox(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    private void addChunkLoaderRange(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    private void addChunkReloadRang(GridBagConstraints gbc, JPanel settingsPanel, SettingsValues initialSettings) {
    }

    private JScrollPane createSettingsGUIPanel(Settings settings, AppController controller) {

        this.cachedSettings = settings;
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

        settingsPanel.add(new JLabel("Set Audio Cache Size"), gbc);

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
        
        // Set the voiceBox to the saved voice if it exists in the current model's voices
        if (initialSettings.voice != null && !initialSettings.voice.isEmpty()) {
            if (initialModel.voices.contains(initialSettings.voice)) {
                voiceBox.setSelectedItem(initialSettings.voice);
            } else if (!initialModel.voices.isEmpty()) {
                voiceBox.setSelectedIndex(0); // Default to first voice if saved voice not in current model
            }
        } else if (!initialModel.voices.isEmpty()) {
            voiceBox.setSelectedIndex(0); // Default to first voice if no saved voice
        }

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
        this.modelSelector = new JComboBox<>(
                //new String[]{"GPT", "Kokoro", "VoxCPM", "New"}
                // This converts the list to a string of the model names because JCOmboBox does not take strings
                settings.modelNameList().toArray(new String[0])
        );

        System.out.println("This is the Current Model Name that should be deault - " + initialSettings.TtsModel);
        this.modelSelector.setSelectedItem(initialSettings.TtsModel);

        this.modelSelector.addActionListener(e
                -> {
            String modelSelectedString = (String) this.modelSelector.getSelectedItem();

            Settings.TtsModel modelSelectedObject = this.cachedSettings.getModel(modelSelectedString);
            TTSURL.setText(modelSelectedObject.URL);
            apiKey.setText(modelSelectedObject.apiKey);
            voices.setText(String.join(", ", modelSelectedObject.voices));
            modelName.setText(modelSelectedString);
            // Update the voice combo box to reflect the newly selected model's voices
            voiceBox.setModel(new javax.swing.DefaultComboBoxModel<>(modelSelectedObject.voices.toArray(new String[0])));
            
            // Preserve the saved voice selection if it exists in the new model
            String savedVoice = this.cachedSettings.getSettingsValues().voice;
            
            if (savedVoice != null && !savedVoice.isEmpty() && modelSelectedObject.voices.contains(savedVoice)) {
                // Use the saved voice if it exists in this model
                voiceBox.setSelectedItem(savedVoice);
            } else if (!modelSelectedObject.voices.isEmpty()) {
                // Otherwise default to first voice
                voiceBox.setSelectedIndex(0);
            }
        }
        );
        settingsPanel.add(this.modelSelector, gbc);
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

        // Add listener to update voice dropdown when voices text changes
        voices.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateVoiceBoxFromTextField();
            }
            public void removeUpdate(DocumentEvent e) {
                updateVoiceBoxFromTextField();
            }
            public void changedUpdate(DocumentEvent e) {
                updateVoiceBoxFromTextField();
            }
            
            private void updateVoiceBoxFromTextField() {
                String voicesText = voices.getText().trim();
                if (!voicesText.isEmpty()) {
                    List<String> updatedVoices = Arrays.asList(voicesText.split(","));
                    updatedVoices = updatedVoices.stream()
                        .map(String::trim)
                        .filter(v -> !v.isEmpty())
                        .toList();
                    
                    Object currentSelection = voiceBox.getSelectedItem();
                    voiceBox.setModel(new javax.swing.DefaultComboBoxModel<>(updatedVoices.toArray(new String[0])));
                    
                    // Try to keep the previous selection if it's still in the list
                    if (currentSelection != null && updatedVoices.contains(currentSelection)) {
                        voiceBox.setSelectedItem(currentSelection);
                    } else if (!updatedVoices.isEmpty()) {
                        voiceBox.setSelectedIndex(0);
                    }
                }
            }
        });

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
            String ttsModelSelected = (String) this.modelSelector.getSelectedItem();

            // New Model Vars
            String ttsAddr = TTSURL.getText();
            String voicesString = voices.getText();
            String TTSModelName = modelName.getText();
            String apiKeyValue = apiKey.getText();

            // ========== SAFETY VALIDATION ==========
            // Validate all text field inputs before saving
            if (!validateSettings(ttsAddr, TTSModelName, apiKeyValue, voicesString)) {
                System.out.println("Settings validation failed. Save cancelled.");
                return; // Stop save operation if validation fails
            }

            List<String> voicesList = Arrays.asList(voicesString.split(","));
            // Trim whitespace from each voice (strings are immutable, so create new list)
            voicesList = voicesList.stream()
                .map(String::trim)
                .toList();

            // Ensure the selected voice is in the voices list; otherwise use first voice
            if (voiceSelected == null || voiceSelected.trim().isEmpty() || !voicesList.contains(voiceSelected.trim())) {
                if (!voicesList.isEmpty()) {
                    voiceSelected = voicesList.get(0);
                    System.out.println("Voice selection adjusted to: " + voiceSelected);
                }
            } else {
                // Trim the selected voice to remove any whitespace
                voiceSelected = voiceSelected.trim();
            }

            // I need to check if everything is entered correctly.
            // The end user can not name a model new. This is to prevent that
            // This only matter if JComboBox is New
            if (ttsModelSelected.equals("New")) {
                if (this.cachedSettings.modelNameList().contains(initialModel.name)) {

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
