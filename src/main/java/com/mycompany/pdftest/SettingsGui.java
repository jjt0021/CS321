package com.mycompany.pdftest;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
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

    static JScrollPane createSettingsGUI() {

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
        settingsPanel.add(new JLabel("Set Chunk Loaded Range"), gbc);

        int initialLoadedRange = 100;
        int minLoadedRange = 25;
        int maxLoadedRange = 500;
        int step = 5;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner loadedRange = new JSpinner(new SpinnerNumberModel(initialLoadedRange, minLoadedRange, maxLoadedRange, step));
        gbc.fill = GridBagConstraints.NONE;  // Don't fill space horizontally
        settingsPanel.add(loadedRange, gbc);
        row++;

        // This is for the chunk loader
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Set Chunk Reloaded Range"), gbc);

        int initialReloadedRange = 100;
        int minReloadedRange = 25;
        int maxReloadedRange = 500;
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JSpinner reloadedRange = new JSpinner(new SpinnerNumberModel(initialReloadedRange, minReloadedRange, maxReloadedRange, step));
        settingsPanel.add(reloadedRange, gbc);
        row++;

        // 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // This is for the different TTS voices 
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("TTS Name:"), gbc);

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
        settingsPanel.add(new JLabel("TTS Model Settings"), gbc);
        row++;
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("TTS Model:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JComboBox<String> difficultyBox = new JComboBox<>(
                new String[]{"GPT", "Kokoro", "VoxCPM", "New"}
        );
        settingsPanel.add(difficultyBox, gbc);

        row++;

        settingsPanel.add(new JLabel("TTS Addr:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("TTS Addr:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField TTSURL = new JTextField();
        settingsPanel.add(TTSURL, gbc);

        row++;

        // Voice Names list
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Voices:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField voices = new JTextField();
        settingsPanel.add(voices, gbc);

        row++;

        // Model Name
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Model Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JTextField modelName = new JTextField();
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

        return settingsScrollMenue;
    }

}
