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

    public Audio(String text, String voice, String url, String model,int chunk, String bookName) {
        this.text = text;
        this.url = url;
        this.voice = voice;
        this.model = model;
        this.chunk = chunk;
        this.bookName = bookName;
    }

    public boolean getIsGenerating() {
        if(currentState == currentState.GENERATING){
            return true;
        }else {
        
        return false;
        }
        
    }

    public void requestAudio() {
        // This will be where we request audio.
        currentState = AudioState.GENERATING;
        HttpClient client = HttpClient.newHttpClient();

        //These are just test values.
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

        // This happens when the request is completed
        responseFuture.thenAccept(response -> {
            try {
                if (response.statusCode() != 200) {
                    System.err.println("HTTP error: " + response.statusCode());
                    return;
                }

                Path outputPath = Path.of("book_%s_chunk_%s.mp3", bookName, String.valueOf(chunk));
                Files.write(outputPath, response.body());
                currentState = AudioState.READY;
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
