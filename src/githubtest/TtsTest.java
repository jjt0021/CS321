package com.mycompany.ttstest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.ArrayList;
import java.util.Arrays;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class TtsTest {

    public static String getTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public class TextUtils {


        public static ArrayList<String> splitText(String inputText) {
            
            String[] chunks = inputText.split("(?<=[.!?])\\s+");
            
            int length = chunks.length;
            

            return new ArrayList<>(Arrays.asList(chunks));
        }
    }
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        
        String model = "voxcpm";
        String voice = "sheldon";
        String input = "This is a test";
        String url = "http://192.168.169.3:8999/v1/audio/speech";
        String pdfPath = "";
        
        String fullText = getTextFromPdf(new File(pdfPath));
        
        String jsonBody = String.format("""
            {
              "model": "%s",
              "input": "%s",
              "voice": "%s",
              "response_format": "mp3"
            }
            """, model, input, voice);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<byte[]> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        Path outputPath = Path.of("speech.mp3");
        Files.write(outputPath, response.body());

        System.out.println("Saved speech to " + outputPath.toAbsolutePath());
    }
}
