package gui;

import java.io.File;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * This class acts as the executable for the GUI application.
 * 
 * @author 190021081
 */
import static config.Configuration.LOG;

public class AutoPoet extends Application {

    
    /** 
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("AutoPoet.fxml"));
            Scene scene = new Scene(root);

            Image icon = new Image("gui" + File.separator + "st_andrews_cs_logo.png");
            stage.getIcons().add(icon);
            stage.setTitle("AutoPoet");
            String css = this.getClass().getResource("stylesheet.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setOnCloseRequest(WindowEvent -> {
                try {
                    LOG.closeLogWriters();
                } catch (Exception e) {
                    // do nothing
                }
            });

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
