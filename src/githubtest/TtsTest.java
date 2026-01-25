package com.mycompany.ttstest;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

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
            
            List<String> combined = new ArrayList<>();

            int chunkPos = 1;
            int chunkLength = 31;
            int maxChunkLength = 60;
            String combinedText = chunks[0];
            
            
            while (chunkPos < chunks.length) {

                while (combinedText.length() + chunks[chunkPos].length() < chunkLength) {
                    combinedText += chunks[chunkPos];
                    chunkPos++;
                }
                
                // This part is to split the text if it is too long. 
                if (combinedText.length() > maxChunkLength){
                    int stringPos = combinedText.length()/2;
                    while (stringPos < combinedText.length() && combinedText.charAt(stringPos) != ' ') {
                        stringPos++;
                    }
                
                    String firstPart = combinedText.substring(0, stringPos);  
                    combinedText = combinedText.substring(stringPos);  

                    combined.add(firstPart);
                    
                }
                
               
                combined.add(combinedText);
                
                combinedText = chunks[chunkPos];
                chunkPos++;
            }
            

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

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // Send async
        CompletableFuture<HttpResponse<byte[]>> responseFuture =
                client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray());

        // Handle response when it arrives
        responseFuture.thenAccept(response -> {
            try {
                Path outputPath = Path.of("speech.mp3");
                Files.write(outputPath, response.body());
                System.out.println("Saved speech to " + outputPath.toAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
