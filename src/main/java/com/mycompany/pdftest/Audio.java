/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

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

/**
 *
 * @author elimo
 */
public class Audio {

    /**
     * Listener interface for audio playback completion events
     * Allows Audio to notify PlayState when a chunk finishes playing
     */
    public interface PlaybackListener {
        void onChunkFinished(int chunkNumber);
        void onPlaybackError(String errorMessage);
    }
    
    private PlaybackListener playbackListener;

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
     * Constructor with playback listener for continuous playback
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

    public boolean getIsGenerating() {
        return (currentState == AudioState.GENERATING);
    }

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

        String jsonBody = String.format("""
            {
              "model": "%s",
              "input": "%s",
              "voice": "%s",
              "response_format": "mp3"
            }
            """, model, text, voice);

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
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            System.out.println("[Audio] *** HTTP RESPONSE RECEIVED: status=" + response.statusCode() + " ***");
            System.out.flush();

            if (response.statusCode() != 200) {
                System.err.println("[Audio] HTTP error for chunk " + chunk + ": " + response.statusCode());
                currentState = AudioState.FAILED;
                return;
            }

            System.out.println("[Audio] Response successful, received " + response.body().length + " bytes");
            Path outputPath = Paths.get(String.format("book_%s_chunk_%d.mp3", bookName, chunk));
            Files.write(outputPath, response.body());
            setFileURL(outputPath.toString());
            currentState = AudioState.READY;
            System.out.println("[Audio] Chunk " + chunk + " successfully saved to " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("[Audio] *** EXCEPTION during requestAudio for chunk " + chunk + ": " + e.getClass().getSimpleName() + " ***");
            System.err.println("[Audio] Exception message: " + e.getMessage());
            System.err.flush();
            e.printStackTrace();
            currentState = AudioState.FAILED;
        }
        System.out.println("[Audio] *** requestAudio() END for chunk " + chunk + " ***");
        System.out.flush();
    }

    public void setFileURL(String path) {
        this.fileURL = path;
    }

    public String getFileURL() {
        return fileURL;
    }

    /**
     * Check if the audio clip is valid and can be used
     */
    public boolean isClipValid() {
        return clip != null && clip.isOpen();
    }

    /**
     * Stop audio playback and close the clip
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
     * Set the playback listener for completion notifications
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
