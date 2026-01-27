/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.pdftest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.Loader;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author elimo
 */
public class PDFTest {

    public static String getTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }//Tell-Tale_Heart.pdf


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

                    combined.add(firstPart.strip());
                    
                }
                
               
                combined.add(combinedText.strip());
                
                combinedText = chunks[chunkPos];
                chunkPos++;
            }
            

            return new ArrayList<>(Arrays.asList(chunks));
        }
    }
    

    public static void main(String[] args) throws IOException {
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        String pdfPath = "src/main/java/com/mycompany/pdftest/Tell-Tale_Heart.pdf";
        File pdf = new File(pdfPath);

        ArrayList<String> book = new ArrayList<>();
        book = TextUtils.splitText(getTextFromPdf(pdf));
        System.out.print(book);

    }
}
