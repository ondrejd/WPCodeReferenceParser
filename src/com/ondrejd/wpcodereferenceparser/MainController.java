/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ondrejd.wpcodereferenceparser;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * FXML Controller class
 *
 * @author ondrejd
 */
public class MainController implements Initializable {
    public static enum TYPES { CLASSES, FUNCTIONS, HOOKS };
    public static String DEFAULT_FILE = "wordpress-api.xml";
    public static String SEARCH_URL = "https://developer.wordpress.org/reference/%s/page/%d/";

    private String currentType;
    private int currentPage;
    private int totalPages;
    private ObservableList<ReferenceItem> items;

    /**
     * Single reference item.
     */
    public class ReferenceItem {
        private String type;
        private String name;
        private String description;
        private String url;

        public String getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getUrl() { return url; }

        public ReferenceItem(String type, String name, String description, String url) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.url = url;
        }
    }

    @FXML
    private Button Button1;
    @FXML
    private ComboBox<String> ComboBox1;
    @FXML
    private TextArea TextArea1;
    @FXML
    private TextField TextField1;

    @FXML
    private void handleComboBox1Action(ActionEvent event) {
        String currentType = ComboBox1.getValue().toString();
        String url  = "";

        switch (currentType) {
            case "classes"   : url = getUrl(TYPES.CLASSES); break;
            case "functions" : url = getUrl(TYPES.FUNCTIONS); break;
            case "hooks"     : url = getUrl(TYPES.HOOKS); break;
        }

        TextField1.setText(url);
        log("Search URL: " + url);
        Button1.setDisable(false);
    }

    @FXML
    private void handleButton1Action(ActionEvent event) {
        String url = TextField1.getText();
        log("Start parsing URL " + url);

        parse(url);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup menuitems of ComboBox1
        ObservableList<String> resourceTypes = FXCollections.observableArrayList("functions", "classes", "hooks");
        ComboBox1.setItems(resourceTypes);
        // Disable Button1
        Button1.setDisable(true);
        // Set focus on ComboBox1
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ComboBox1.requestFocus();
            }
        });
    }

    /**
     * @return Index of currently parsed page.
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * @return Total count of pages to parse.
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Simple loggin in our testing application.
     * @param text
     */
    private void log(String text) {
        TextArea1.setText(TextArea1.getText() + text + "\n");
    }

    /**
     * @param type Type of reference ("classes", "functions", "hooks").
     * @return Returns URL of page to parse.
     */
    private String getUrl(TYPES type) {
        String typeStr = "";

        switch(type) {
            case FUNCTIONS: typeStr = "functions"; break;
            case HOOKS: typeStr = "hooks"; break;
            case CLASSES:
            default:
                typeStr = "classes";
                break;
        }

        return SEARCH_URL.replace("%s", typeStr);
    }

    /**
     * Parses for WordPress Code Reference.
     * @param url
     */
    private void parse(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            //log(doc.toString());

            currentPage = 1;

            totalPages = ( totalPages <= 0 ) ? parseTotalPages(doc) : totalPages;
            items = FXCollections.observableArrayList();

            log("Current page index : " + Integer.toString(currentPage) + "");
            log("Total pages count  : " + Integer.toString(totalPages) + "");

            // Parse the first page
            parsePage(doc);
            currentPage++;

            // Parse other pages
            for ( int i = currentPage; i < totalPages; i++ ) {
                url = url + Integer.toString(currentPage);
                parsePage(url);
                currentPage++;
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Parses single reference's page (form page's URL).
     * @param url 
     */
    private void parsePage(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            parsePage(doc);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Parses single reference's page (from {@see Document}).
     * @param doc 
     */
    private void parsePage(Document doc) {
        Elements articles = doc.getElementsByClass("wp-parser-class");

        for (Element article : articles) {
            Element h1 = article.getElementsByTag("h1").first();
            String type = currentType;
            String name = h1.text();
            String desc = article.getElementsByClass("description").first().text().replace("Class: ", "");
            String url = h1.getElementsByTag("a").first().attr("href");

            ReferenceItem item = new ReferenceItem(type, name, desc, url);
            items.add(item);
        }
    }

    /**
     * 
     * @param doc
     * @return
     * @throws NullPointerException 
     */
    private int parseTotalPages(Document doc) {
        int totalPages = 0;

        try {
            Element elm1 = doc.getElementsByClass("pagination").first();
            Element elm2 = elm1.children().last().previousElementSibling();
            totalPages   = Integer.parseInt(elm2.text());
        } catch(NullPointerException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return totalPages;
    }
}
