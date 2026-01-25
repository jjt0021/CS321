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
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


    
    public class chunk {
        
        private Clip clip;
        private long clipPosition;
        private int chunk;
        private String fileURL;
        
        public void chunk(int chunk, String fileURL){
            this.chunk = chunk;
            this.fileURL = fileURL;
        }
        
        public void setFileURL(String path){
            fileURL = path;
        }
        
        public String getFileURL(){
            return fileURL;
        }
        
        public void setChunk(int chukNum){
            chunk = chukNum;
        }
        
        public int getChunk(){
            return chunk;
        }

        
        public void playAuido() throws UnsupportedAudioFileException, IOException, LineUnavailableException{
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
        
        public void playAudio(){
            clip.setMicrosecondPosition(clipPosition);
            clip.start();
        }
        public void pauseAudio(){
            clipPosition = clip.getMicrosecondPosition();
            clip.stop();
            
        }
        
  }

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
        responseFuture.thenAccept((HttpResponse<byte[]> response) -> {
            try {
                Path outputPath = Path.of("speech.mp3");
                Files.write(outputPath, response.body());
                System.out.println("Saved speech to " + outputPath.toAbsolutePath());
            } catch (IOException e) {
            }
        });
    }
}
