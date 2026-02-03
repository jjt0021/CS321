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
import java.util.concurrent.CompletableFuture;
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

    private String text;
    private String voice;
    private String url;
    private String model;

    private boolean isGenerating;

    private Clip clip;
    private long clipPosition;
    private String fileURL;

    public Audio(String text, String voice, String url, String model) {
        this.text = text;
        this.url = url;
        this.voice = voice;
        this.model = model;
        this.isGenerating = true;
    }

    public boolean getIsGenerating() {
        return isGenerating;
    }

    public void requestAudio() {
        // This will be where we request audio.
        HttpClient client = HttpClient.newHttpClient();

        /*     
        String model = "voxcpm";
        String voice = "sheldon";
        String input = "This is a test";
        String url = "http://192.168.169.3:8999/v1/audio/speech";
        **/
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

        CompletableFuture<HttpResponse<byte[]>> responseFuture
                = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());

        responseFuture.thenAccept(response -> {
            try {
                if (response.statusCode() != 200) {
                    System.err.println("HTTP error: " + response.statusCode());
                    return;
                }

                Path outputPath = Path.of("speech.mp3");
                Files.write(outputPath, response.body());
                System.out.println("Saved speech to " + outputPath.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).join();

    }

    public void setFileURL(String path) {
        this.fileURL = path;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void playAuido() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File audioFile = new File(fileURL);

        AudioInputStream chunk = AudioSystem.getAudioInputStream(audioFile);
        clip = AudioSystem.getClip();
        clip.open(chunk);

        clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                System.out.println("audio is finished");
                clip.close();
            }
        });

    }

    public void resumeAudio() {
        clip.setMicrosecondPosition(clipPosition);
        clip.start();
    }

    public void pauseAudio() {
        clipPosition = clip.getMicrosecondPosition();
        clip.stop();

    }

}
