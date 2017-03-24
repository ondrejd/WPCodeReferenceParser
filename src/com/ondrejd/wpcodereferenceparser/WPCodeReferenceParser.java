/**
 * @author  Ondřej Doněk, <ondrejd@gmail.com>
 * @license https://www.gnu.org/licenses/gpl-3.0.en.html GNU General Public License 3.0
 * @link https://github.com/ondrejd/WPCodeReferenceParser for the canonical source repository
 */

package com.ondrejd.wpcodereferenceparser;

import com.ondrejd.wordpress.code.reference.Parser;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 *
 * @author ondrejd
 */
public class WPCodeReferenceParser extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createUi(), 300, 250);
        
        primaryStage.setTitle("WordPress Code Reference Parser Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private StackPane createUi() {
        StackPane root = new StackPane();

        // HBox with URL label/input
        HBox hbox = new HBox();
        Label lbl1 = new Label();
        TextField tfield1 = new TextField();
        lbl1.setText("WPCR Url:");
        lbl1.setLabelFor(tfield1);
        tfield1.setId("wpcrUrlTextField");
        tfield1.setText(Parser.getSearchUrl(Parser.TYPES.HOOKS, 1));
        hbox.getChildren().add(lbl1);
        hbox.getChildren().add(tfield1);
        root.getChildren().add(hbox);
        
        Button btn = new Button();
        btn.setText("Start parsing");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
        root.getChildren().add(btn);
        
        return root;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
