/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
/** *****************IMPORTANT*******************
 *
 * iT IS BEST TO HAVE AUDIO BASED ON CURRENT CHUNK RATHER THAN THE BUTTON.
 * IT SHOULD BE IN PLAYSTATE
 *
 * */
package com.mycompany.pdftest;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.mycompany.pdftest.Settings.SettingsValues;

/**
 *
 * @author elimo
 */
public class PDFTest {

    public static void main(String[] args) throws IOException, InterruptedException {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();

        JFrame frame = new JFrame("Audio Book");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);

        String pdfPath = "src/main/java/com/mycompany/pdftest/Challenges and Strategies for African American Women in Higher Ed _ Free Essay Example.pdf";
        File pdf = new File(pdfPath);
        ArrayList<String> book = new ArrayList<>();
        book = TextUtils.splitText(TextUtils.getTextFromPdf(pdf));

        // 0 for testing
        //0 Is just for testing later it will be changed to get from the saved data.
        Settings settingsManager = new Settings();
        SettingsValues initialSettings = settingsManager.getSettingsValues();
        String bookName = pdf.getName();
        PlayState playStateManager = new PlayState(0, book, initialSettings.loadedRange, initialSettings.reloadRange, initialSettings.cacheSize, bookName, settingsManager.getModel(initialSettings.TtsModel), initialSettings.voice);

        JLayeredPane fileExplorer = new JLayeredPane();
        BookUI bookUIManager = new BookUI(playStateManager, settingsManager);
        JLayeredPane audioBook = bookUIManager.makePane(frame, playStateManager);

        JScrollPane settings = SettingsUI.createSettingsGUI(settingsManager);
        CardLayout cardLayout = new CardLayout();
        JPanel screens = new JPanel(cardLayout);

        // The Fist add is shown first, so we should add the FIle explorer first.
        //screens.add(fileExplorer, "File Explorer");
        screens.add(settings, "Settings");
        screens.add(audioBook, "audioBook");
        // We will need to add listeners to every menue
        // The code will look something like this         
        // startButton.addActionListener(e -> cardLayout.show(cards, "Settings"));

        frame.setContentPane(screens);

        frame.setVisible(true);

        /*
       ************ IMPORTANT - AUTO SCROLL CODE ******************* 

        // Auto Scroll Test
        JViewport viewport = scrollPane.getViewport();
        Point p = viewport.getViewPosition();

        p.y += 100;   // move down 50 pixels
        viewport.setViewPosition(p);
        
        
        **/
    }

}

/*
Implementaion Notes

We should update the current chunk with highlighting
The current chunk will be updated in the highlighted chunk function
We should create the adio objects when we make the 

The file explorer should handle checking if the pdf file is good.

**/
