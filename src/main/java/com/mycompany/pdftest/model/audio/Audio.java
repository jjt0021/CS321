/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest.model.audio;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 *
 * @author elimo
 */
public class Audio {

    /**
     * This is the interface that lets the controller know when a chunk is finished playing.
     * It is useful in {@link PlayState} so when a chunk is finished it can move to the next.
     */
    public interface PlaybackListener {
        void onChunkFinished(int chunkNumber);
        void onPlaybackError(String errorMessage);
    }
    
    private PlaybackListener playbackListener;

    /**
     * This enum is used to keep track of the state of the HTTP request for the audio.
     * It lets the other sections know if it is ready to play, still generating, or has failed.
     */
    public enum AudioState {
        MISSING,
        GENERATING,
        READY,
        PLAYING,
        PAUSED,
        STOPPED,
        FAILED
    }

    AudioState currentState = AudioState.MISSING;
    private String text;
    private String voice;
    private String url;
    private String model;
    private String bookName;
    int chunk;

    private Clip clip;
    private long clipPosition;
    private String fileURL;

    public Audio(String text, String voice, String url, String modelName, int chunk, String bookName) {
        this(text, voice, url, modelName, chunk, bookName, null);
    }

    /**
     * Constructor with {@link PlaybackListener} for continuous playback.
     */
    public Audio(String text, String voice, String url, String modelName, int chunk, String bookName, PlaybackListener listener) {
        this.text = text;
        this.voice = voice;
        this.chunk = chunk;
        this.bookName = bookName;
        this.url = url;
        this.model = modelName;
        this.playbackListener = listener;
    }

    /**
     * Check if audio is currently being generated from the TTS API.
     * @return true if generating, false otherwise
     */
    public boolean getIsGenerating() {
        return (currentState == AudioState.GENERATING);
    }

    /**
     * Get the current audio state.
     * @return the current {@link AudioState}
     */
    public AudioState getCurrentState() {
        return currentState;
    }

    /**
     * Make an HTTP request to the TTS API to generate audio for this chunk.
     * Saves the resulting audio file to the cache and updates state.
     */
    public void requestAudio() {
        // This will be where we request audio.
        System.out.println("[Audio] *** requestAudio() START for chunk " + chunk + " ***");
        System.out.flush();
        System.out.println("[Audio] requestAudio() called for chunk " + chunk + " with model=" + model + " voice=" + voice);
        System.out.println("[Audio] TTS URL: " + url);
        System.out.println("[Audio] Text length: " + text.length() + " characters");
        System.out.flush();
        currentState = AudioState.GENERATING;
        System.out.println("[Audio] State changed to GENERATING");
        System.out.flush();
        HttpClient client = HttpClient.newHttpClient();

        // Build JSON using GSON to properly escape special characters in the text
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", model);
        jsonObject.addProperty("input", text);
        jsonObject.addProperty("voice", voice);
        jsonObject.addProperty("response_format", "wav");
        
        Gson gson = new Gson();
        String jsonBody = gson.toJson(jsonObject);
        
        System.out.println("[Audio] JSON Body: " + jsonBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("[Audio] HTTP request built, sending to " + url);
        System.out.flush();
        try {
            System.out.println("[Audio] *** SENDING HTTP REQUEST to " + url + " ***");
            System.out.flush();
            // Use sendAsync() instead of send() - this works with the TTS server
            client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                    .thenAccept(response -> {
                        try {
                            System.out.println("[Audio] *** HTTP RESPONSE RECEIVED: status=" + response.statusCode() + " ***");
                            System.out.flush();

                            if (response.statusCode() != 200) {
                                System.err.println("[Audio] HTTP error for chunk " + chunk + ": " + response.statusCode());
                                currentState = AudioState.FAILED;
                                return;
                            }

                            System.out.println("[Audio] Response successful, received " + response.body().length + " bytes");
                            Path outputPath = Paths.get("audio_cache", String.format("book_%s_chunk_%d.wav", bookName, chunk));
                            Files.write(outputPath, response.body());
                            setFileURL(outputPath.toString());
                            currentState = AudioState.READY;
                            System.out.println("[Audio] Chunk " + chunk + " successfully saved to " + outputPath.toAbsolutePath());
                            System.out.flush();
                        } catch (IOException e) {
                            System.err.println("[Audio] IOException in async response handler for chunk " + chunk + ": " + e.getMessage());
                            e.printStackTrace();
                            currentState = AudioState.FAILED;
                        }
                    })
                    .join(); // Wait for the async operation to complete
        } catch (Exception e) {
            System.err.println("[Audio] Exception during requestAudio for chunk " + chunk + ": " + e.getMessage());
            e.printStackTrace();
            currentState = AudioState.FAILED;
        }

    }

    public void setFileURL(String path) {
        this.fileURL = path;
    }

    public String getFileURL() {
        return fileURL;
    }

    /**
     * Check if the audio clip is valid and ready to play.
     * @return true if clip exists and is open, false otherwise
     */
    public boolean isClipValid() {
        return clip != null && clip.isOpen();
    }

    /**
     * This stops audio playback and close the clip
     */
    public void stopAudio() {
        System.out.println("[Audio] stopAudio() called for chunk " + chunk);
        if (clip != null) {
            clip.stop();
            clip.close();
            currentState = AudioState.STOPPED;
            System.out.println("[Audio] Stopped and closed chunk " + chunk);
        } else {
            System.out.println("[Audio] stopAudio() called but clip is null for chunk " + chunk);
        }
    }

    /**
     * This set the playback listener which is used to let others know the audio state
     */
    public void setPlaybackListener(PlaybackListener listener) {
        this.playbackListener = listener;
    }

    /**
     * Get the playback listener
     */
    public PlaybackListener getPlaybackListener() {
        return playbackListener;
    }

    /**
     * Load and start playing the audio file.
     * Sets up the Java Sound Clip and begins playback.
     * Calls listener callback when chunk finishes playing.
     * @throws UnsupportedAudioFileException if audio format isn't supported
     * @throws IOException if audio file can't be read
     * @throws LineUnavailableException if audio system is unavailable
     */
    public void playAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        System.out.println("[Audio] playAudio() called for chunk " + chunk);
        if (fileURL == null) {
            throw new IOException("No audio file set for playback");
        }

        File audioFile = new File(fileURL);
        if (!audioFile.exists()) {
            throw new IOException("Audio file not found: " + fileURL);
        }

        System.out.println("[Audio] Audio file verified: " + audioFile.getAbsolutePath());
        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        clip = AudioSystem.getClip();
        clip.open(audioStream);
        System.out.println("[Audio] Clip opened, duration: " + clip.getMicrosecondLength() + " microseconds");

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                System.out.println("[Audio] Chunk " + chunk + " playback finished");
                clip.close();
                // Notify listener that chunk finished playing
                if (playbackListener != null) {
                    System.out.println("[Audio] Calling listener.onChunkFinished(" + chunk + ")");
                    playbackListener.onChunkFinished(chunk);
                } else {
                    System.out.println("[Audio] WARNING: No playback listener set for chunk " + chunk);
                }
            }
        });

        clip.setMicrosecondPosition(0);
        currentState = AudioState.PLAYING;
        System.out.println("[Audio] Starting playback of chunk " + chunk);
        clip.start();
    }

    /**
     * Resume playback from the saved position, or reload from file if needed.
     */
    public void resumeAudio() {
        System.out.println("[Audio] resumeAudio() called for chunk " + chunk);
        try {
            if (!isClipValid()) {
                System.out.println("[Audio] Clip not valid, reloading from file");
                // try to load and play from file if available
                playAudio();
                return;
            }

            System.out.println("[Audio] Resuming from position " + clipPosition + " microseconds");
            clip.setMicrosecondPosition(clipPosition);
            currentState = AudioState.PLAYING;
            clip.start();
            System.out.println("[Audio] Resumed playback of chunk " + chunk);
        } catch (Exception e) {
            System.err.println("[Audio] Failed to resume audio for chunk " + chunk + ": " + e.getMessage());
            currentState = AudioState.FAILED;
            if (playbackListener != null) {
                playbackListener.onPlaybackError("Failed to resume audio: " + e.getMessage());
            }
        }
    }

    /**
     * Pause audio playback and save the current position.
     */
    public void pauseAudio() {
        System.out.println("[Audio] pauseAudio() called for chunk " + chunk);
        if (!isClipValid()) {
            System.out.println("[Audio] Cannot pause chunk " + chunk + " - clip not valid");
            return;
        }

        clipPosition = clip.getMicrosecondPosition();
        currentState = AudioState.PAUSED;
        clip.stop();
        System.out.println("[Audio] Paused chunk " + chunk + " at position " + clipPosition + " microseconds");
    }

}
