/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest;

import java.net.URI;
import java.io.File;
import java.io.IOException;
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

    public enum AudioState {
        MISSING,
        GENERATING,
        READY,
        PLAYING,
        PAUSED,
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
        this.text = text;
        this.voice = voice;
        this.chunk = chunk;
        this.bookName = bookName;

        this.url = url;
        this.model = modelName;

    }

    public boolean getIsGenerating() {
        return (currentState == currentState.GENERATING);
    }

    public void requestAudio() {
        // This will be where we request audio.
        currentState = AudioState.GENERATING;
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

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                System.err.println("HTTP error: " + response.statusCode());
                currentState = AudioState.FAILED;
                return;
            }

            Path outputPath = Paths.get(String.format("book_%s_chunk_%d.mp3", bookName, chunk));
            Files.write(outputPath, response.body());
            setFileURL(outputPath.toString());
            currentState = AudioState.READY;
            System.out.println("Saved speech to " + outputPath.toAbsolutePath());

        } catch (Exception e) {
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

    public void playAudio() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        if (fileURL == null) {
            throw new IOException("No audio file set for playback");
        }

        File audioFile = new File(fileURL);
        if (!audioFile.exists()) {
            throw new IOException("Audio file not found: " + fileURL);
        }

        AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
        clip = AudioSystem.getClip();
        clip.open(audioStream);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                System.out.println("audio is finished");
                clip.close();
            }
        });

        clip.setMicrosecondPosition(0);
        currentState = AudioState.PLAYING;
        clip.start();
    }

    public void resumeAudio() {
        try {
            if (clip == null) {
                // try to load and play from file if available
                playAudio();
                return;
            }

            clip.setMicrosecondPosition(clipPosition);
            currentState = AudioState.PLAYING;
            clip.start();
        } catch (Exception e) {
            System.err.println("Failed to resume audio: " + e.getMessage());
            currentState = AudioState.FAILED;
        }
    }

    public void pauseAudio() {
        if (clip == null) {
            return;
        }

        clipPosition = clip.getMicrosecondPosition();
        currentState = AudioState.PAUSED;
        clip.stop();

    }

}
