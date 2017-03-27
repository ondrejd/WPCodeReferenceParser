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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
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
    public static String DEFAULT_FILE = "wordpress-api.xml";
    public static String SEARCH_URL = "https://developer.wordpress.org/reference/";

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

        @Override
        public String toString() {
            return "- refitem (" + type + ") \"" + name + "\"; " + 
                    "description \"" + description + "\"; " + 
                    "url \"" + url;
        }
    }

    @FXML
    private ComboBox<String> ComboBox1;
    @FXML
    private TextArea TextArea1;
    @FXML
    private ProgressBar ProgressBar1;

    @FXML
    private void handleComboBox1Action(ActionEvent event) {
        ProgressBar1.setProgress(-1f);

        /**
         * @link http://stackoverflow.com/questions/4005350/java-how-to-run-thread-separately-from-main-program-class
         */
        String type = ComboBox1.getValue().toString();
        ReferenceParser parser = new ReferenceParser(type);
        Thread parserThread = new Thread(parser);
        parserThread.setDaemon(true);
        parserThread.start();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Setup menuitems of ComboBox1
        ObservableList<String> resourceTypes = FXCollections.observableArrayList("functions", "classes", "hooks");
        ComboBox1.setItems(resourceTypes);
        // Set focus on ComboBox1
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ComboBox1.requestFocus();
            }
        });
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
     */
    private String prepareUrl(String type) {
        return SEARCH_URL + type + "/";
    }

    /**
     * @param type Type of reference ("classes", "functions", "hooks").
     * @param page Number of currently processed page.
     */
    private String prepareUrl(String type, int page) {
        String url = prepareUrl(type);
        return url + "/page/" + Integer.toString(page) + "/";
    }

    /**
     * Implements WordPress reference site parser.
     */
    class ReferenceParser implements Runnable {
        /**
         * Type of reference to parse ("classes", "functions", "hooks").
         */
        private String type;

        /**
         * Currently processed page (according to pagination on target HTML pages).
         */
        private int page;

        /**
         * URL of currently processed page.
         */
        private String url;

        /**
         * Count of all pages to parse (according to pagination on target HTML pages).
         */
        private int total;

        /**
         * Holds already parsed reference items
         */
        private ObservableList<ReferenceItem> items;

        /**
         * @return Index of currently parsed page.
         */
        public int getPage() {
            return page;
        }

        /**
         * @return URL of currently processed page.
         */
        public String getUrl() {
            return url;
        }

        /**
         * @return Total count of pages to parse.
         */
        public int getTotal() {
            return total;
        }

        public int getItemsCount() {
            return items.size();
        }

        public ObservableList<ReferenceItem> getItems() {
            return items;
        }

        /**
         * Constructor.
         * @param type 
         */
        public ReferenceParser(String type) {
            this.type = type;
            this.url = prepareUrl(type);
        }

        /**
         * Starts parsing.
         */
        public void run() {
            try {
                Document doc = Jsoup.connect(url).get();
                //log(doc.toString());

                page = 1;
                total = ( total <= 0 ) ? parseTotalPages(doc) : total;
                items = FXCollections.observableArrayList();
                //log("Total pages count  : " + Integer.toString(total) + "");
                //log("Current page index : " + Integer.toString(page) + "");
                //log("Parsing URL        : " + url);

                // Parse the first page
                parse(doc);
                page++;

                // Parse other pages
                for ( int i = page; i < total; i++ ) {
                    //log("Current page index : " + Integer.toString(page) + "");
                    //log("Parsing URL        : " + url);

                    url = prepareUrl(type, page);
                    parse(url);
                    page++;
                }

                // Parsing is finished - save the XML
                //...
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
        private void parse(String url) {
            try {
                Document doc = Jsoup.connect(url).get();
                parse(doc);
            } catch (IOException ex) {
                Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        /**
         * Parses single reference's page.
         * @param doc HTML document to parse.
         */
        private void parse(Document doc) {
            Elements articles = doc.getElementsByClass("wp-parser-class");

            for (Element article : articles) {
                Element h1 = article.getElementsByTag("h1").first();
                String name = h1.text();
                String desc = article.getElementsByClass("description").first().text().replace("Class: ", "");
                String url = h1.getElementsByTag("a").first().attr("href");

                ReferenceItem item = new ReferenceItem(type, name, desc, url);
                items.add(item);
                //log(item.toString());
            }
        }

        /**
         * @param doc HTML document to parse.
         * @return Returns total count of pages to parse.
         * @throws NullPointerException 
         */
        private int parseTotalPages(Document doc) {
            int totalPages = 0;

            try {
                Element elm1 = doc.getElementsByClass("pagination").first();
                Element elm2 = elm1.children().last().previousElementSibling();
                totalPages   = Integer.parseInt(elm2.text());
            } catch(NullPointerException ex) {
                Logger.getLogger(ReferenceParser.class.getName()).log(Level.SEVERE, null, ex);
            }

            return totalPages;
        }
    }
}
