/**
 * @author  Ondřej Doněk, <ondrejd@gmail.com>
 * @license https://www.gnu.org/licenses/gpl-3.0.en.html GNU General Public License 3.0
 * @link https://github.com/ondrejd/WPCodeReferenceParser for the canonical source repository
 */

package com.ondrejd.wpcodereferenceparser;

import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class WPCodeReferenceParser extends Application {
    private static final String WINDOW_POSITION_X = "Window_Position_X";
    private static final String WINDOW_POSITION_Y = "Window_Position_Y";
    private static final String WINDOW_WIDTH = "Window_Width";
    private static final String WINDOW_HEIGHT = "Window_Height";
    private static final double DEFAULT_X = 10;
    private static final double DEFAULT_Y = 10;
    private static final double DEFAULT_WIDTH = 640;
    private static final double DEFAULT_HEIGHT = 480;
    private static final String NODE_NAME = "WPCodeReferenceParser";
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        Parent root = loader.load(getClass().getResource("main.fxml").openStream());
        // Set title
        stage.setTitle("WordPress Code Reference Parser Test");
        // Pull the saved preferences and set the stage size and start location
        Preferences pref = Preferences.userRoot().node(NODE_NAME);
        double x = pref.getDouble(WINDOW_POSITION_X, DEFAULT_X);
        double y = pref.getDouble(WINDOW_POSITION_Y, DEFAULT_Y);
        double width = pref.getDouble(WINDOW_WIDTH, DEFAULT_WIDTH);
        double height = pref.getDouble(WINDOW_HEIGHT, DEFAULT_HEIGHT);
        stage.setX(x);
        stage.setY(y);
        stage.setWidth(width);
        stage.setHeight(height);
        // Set scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setOnCloseRequest((final WindowEvent event) -> {
            // Save window size and position
            Preferences preferences = Preferences.userRoot().node(NODE_NAME);
            preferences.putDouble(WINDOW_POSITION_X, stage.getX());
            preferences.putDouble(WINDOW_POSITION_Y, stage.getY());
            preferences.putDouble(WINDOW_WIDTH, stage.getWidth());
            preferences.putDouble(WINDOW_HEIGHT, stage.getHeight());
        });
        stage.setResizable(false);
        // Show application
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
