package gui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.Action;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import words.Poem;

import static config.Configuration.LOG;

public class Controller {

    Poem poem;

    @FXML
    AnchorPane anpnRoot;
    @FXML
    TitledPane ttlpnPoem;

    // Poem & stanza info
    @FXML
    TextArea txtarPoem;
    @FXML
    Label lblPoemStanzaCount, lblPoemLineCount, lblStanzaNumber, lblStanzaLineCount, lblIntRhymeScheme,
            lblActRhymeScheme;

    // Suggestion parameters
    @FXML
    CheckBox chbxNoun, chbxPronoun, chbxVerb, chbxAdjective, chbxAdverb, chbxPreposition, chbxConjunction,
            chbxDefiniteArticle, chbxUnknown;
    @FXML
    CheckBox chbxSynonyms, chbxCommonlyTyped, chbxCommonlyCategorised, chbxPartOf, chbxHasParts, chbxSimilarTo;
    @FXML
    CheckBox chbxRhymeWith;
    @FXML
    TextField txtfldRhymeWith;

    // Buttons
    ToggleButton tgbtnDirectEdit;
    Button btnGetSuggestions;

    public void test(ActionEvent e) {
        System.out.println(String.format("%s had event %s", e.getSource(), e.getEventType()));
    }

    public void openPoem() {
        Stage stage = (Stage) anpnRoot.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Poem File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*"));
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }
        try {
            poem = new Poem(file.toPath());
            ttlpnPoem.setText(poem.getTitle());
            txtarPoem.setText(poem.toString());
            lblPoemStanzaCount.setText(String.valueOf(poem.getStanzaCount()));
            lblPoemLineCount.setText(String.valueOf(poem.getLines()));
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Could not open file.");
            alert.setContentText(String.format("Failed to open %s.", file.getName()));
            alert.show();
        } catch (Exception e) {
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("An unexpected error occured.");
            alert.setContentText("See log for more information.");
            alert.show();
        }
    }

    public void changePoemTextArea() {
        txtarPoem.setDisable(!txtarPoem.isDisabled());
    }

}
