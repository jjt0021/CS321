package com.mycompany.pdftest;

/**
 * Handles the application's configuration, such as API keys and playback speed.
 */
public class Settings {
    /** Intent: Inner data structure for JSON serialization of user preferences. */
    public class SettingsValues {
        public String ttsModelName;
        public boolean showProgressBar;
    }

    /** Intent: Load Settings.json or initialize defaults if missing. */
    private void load() {}

    /** Intent: Persist current user preferences to a JSON file. */
    public void save() {}
}