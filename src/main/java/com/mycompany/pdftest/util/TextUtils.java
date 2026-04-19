/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.pdftest.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Utility class for PDF text extraction and chunking.
 * Gets text from PDFs and splits it into manageable chunks for TTS processing.
 *
 * @author elimo
 */
public class TextUtils {

    /** Minimum length for combining text chunks */
    private static final int MINIMUM_CHUNK_LENGTH = 31;

    /** Maximum length before splitting text chunks */
    private static final int MAXIMUM_CHUNK_LENGTH = 60;

    /** Starting position for chunk iteration */
    private static final int INITIAL_CHUNK_POSITION = 1;

    /** Divisor for finding split point (middle of text) */
    private static final int SPLIT_POSITION_DIVISOR = 2;

    /**
     * Extracts all text from a PDF file using PDFBox.
     * @param pdfFile the PDF file to extract text from
     * @return the complete text content of the PDF
     * @throws IOException if the file can't be read
     */
    public static String getTextFromPdf(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }//Tell-Tale_Heart.pdf just use this for testing.

    /**
     * Splits text into optimized chunks for text-to-speech conversion.
     * Combines short sentences and splits long chunks to match TTS model requirements.
     * @param inputText the raw text to chunk
     * @return list of text chunks ready for TTS
     */
    public static ArrayList<String> splitText(String inputText) {

        String[] chunks = inputText.split("(?<=[.!?])\\s+");

        List<String> combined = new ArrayList<>();

        int chunkPos = INITIAL_CHUNK_POSITION;
        String combinedText = chunks[0];

        while (chunkPos < chunks.length) {

            //Combines it if it is too short.
            while (combinedText.length() + chunks[chunkPos].length() < MINIMUM_CHUNK_LENGTH) {
                combinedText += chunks[chunkPos];
                chunkPos++;
            }

            // This part is to split the text if it is too long. 
            if (combinedText.length() > MAXIMUM_CHUNK_LENGTH) {
                int stringPos = combinedText.length() / SPLIT_POSITION_DIVISOR;
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

        return new ArrayList<>(combined);
    }
}
