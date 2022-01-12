package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import words.Poem;
import words.Stanza;
import words.SuperWord;
import words.Token;

import static config.Configuration.LOG;

public class Controller {

    private static class IndexedTokenLabel extends Label {
        private Controller parentController;
        private Token token;
        private int stanzaIndex;
        private int lineIndex;
        private int tokenIndex;
        private boolean clickable;

        private IndexedTokenLabel(Controller parentController, Token token, int stanzaIndex, int lineIndex,
                int tokenIndex,
                boolean clickable) {
            super();
            this.parentController = parentController;
            this.setText(token.getPlaintext());
            this.token = token;
            this.stanzaIndex = stanzaIndex;
            this.lineIndex = lineIndex;
            this.tokenIndex = tokenIndex;
            this.clickable = clickable;
            if (clickable) {
                this.setOnMouseClicked(ActionEvent -> {
                    parentController.focusedToken = this; 
                    parentController.focusOnStanza(this.stanzaIndex);
                });
            }
        }
    }

    private Poem poem;
    private String seeLog = "See log for more information.";
    private IndexedTokenLabel focusedToken;

    // Poem & stanza info
    @FXML
    AnchorPane anpnRoot, anpnPoem;
    @FXML
    TitledPane ttlpnPoem;
    @FXML
    TextArea txtarPoem;
    @FXML
    GridPane grdpnPoem;
    @FXML
    FlowPane flwpnLine0;
    @FXML
    Label lblPoemStanzaCount, lblPoemLineCount, lblStanzaNumber, lblStanzaLineCount, lblActRhymeScheme;
    @FXML
    TextField txtfldIntRhymeScheme; 

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

    @FXML
    public void initialize() {
        txtfldIntRhymeScheme.setTextFormatter(rhymeSchemeFormatter);
    }

    public void test(ActionEvent e) {
        System.out.println(String.format("%s had event %s", e.getSource(), e.getEventType()));
    }

    private FlowPane getEmptyLine() {
        FlowPane guiLine = new FlowPane();
        return guiLine;
    }

    private FlowPane addEmptyLine(int absLineIndex) {
        FlowPane guiLine = getEmptyLine();
        grdpnPoem.addRow(absLineIndex, guiLine);
        return guiLine;
    }

    private void focusOnStanza(int stanzaIndex) {
        lblStanzaNumber.setText(String.valueOf(stanzaIndex + 1));
        Stanza focusedStanza = poem.getStanzas().get(stanzaIndex);
        lblStanzaLineCount.setText(String.valueOf(focusedStanza.getLines().size()));
        if (focusedStanza.getDesiredRhymeScheme() != null) {
            txtfldIntRhymeScheme.setText(focusedStanza.getDesiredRhymeScheme().toString());
        } else {
            txtfldIntRhymeScheme.setText("");
        }
        lblActRhymeScheme.setText(focusedStanza.getActualRhymeScheme().toString());
    }

    TextFormatter<String> rhymeSchemeFormatter = new TextFormatter<>(change -> {
        if (!change.isContentChange()) {
            return change;
        }
    
        String text = change.getControlNewText();
    
        if (!text.matches("[A-Z#]*")) { 
            return null;
        }
    
    
        return change;
    });
    
    public void updateIntendedRhymeScheme(ActionEvent e) {
        if (!poem.getStanzas().get(focusedToken.stanzaIndex).setDesiredRhymeScheme(((TextField)e.getSource()).getText())) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setHeaderText("Could not parse rhyme scheme.");
            alert.setContentText(String.format(seeLog));
            alert.show();
        }
    }

    private void tokenizePoem() {
        grdpnPoem.getChildren().clear();
        int absLineIndex = 0; // absolute line index, including empty lines
        FlowPane guiLine = addEmptyLine(absLineIndex); // a flow pane that acts as a line of text in the GUI

        int stanzaIndex = 0;
        for (Stanza stanza : this.poem.getStanzas()) {
            int lineIndex = 0;
            for (ArrayList<Token> line : stanza.getLines()) {
                // add all tokens to line in the GUI
                int tokenIndex = 0;
                for (Token token : line) {
                    IndexedTokenLabel indToken = new IndexedTokenLabel(this, token, stanzaIndex, lineIndex,
                            tokenIndex++, token.getClass().equals(SuperWord.class));
                    guiLine.getChildren().add(indToken);
                }

                lineIndex++;
                // add placeholder for next line in the GUI
                guiLine = addEmptyLine(++absLineIndex);
            }

            stanzaIndex++;

            // add empty label to current line i.e. empty line between stanzas
            guiLine.getChildren().add(new Label());

            // add placeholder for next line in the GUI
            guiLine = addEmptyLine(++absLineIndex);
        }
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
            lblPoemLineCount.setText(String.valueOf(poem.getLineCount()));
            tokenizePoem();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Could not open file.");
            alert.setContentText(String.format("Failed to open %s.", file.getName()));
            alert.show();
        } catch (Exception e) {
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("An unexpected error occured.");
            alert.setContentText(seeLog);
            alert.show();
        }
    }

    public void changePoemTextArea() {
        // refresh poem content
        if (txtarPoem.isDisabled()) {
            txtarPoem.setText(poem.toString());
        } else {
            try {
                poem = new Poem(ttlpnPoem.getText(), txtarPoem.getText());
                tokenizePoem();
            } catch (IOException e) {
                LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Could not parse directly edited text.");
                alert.setContentText(String.format(seeLog));
                alert.show();
            }
        }

        lblPoemStanzaCount.setText(String.valueOf(poem.getStanzaCount()));
        lblPoemLineCount.setText(String.valueOf(poem.getLineCount()));

        // (un)hide text area
        txtarPoem.setVisible(!txtarPoem.isVisible());

        // (un)hide anchor pane
        anpnPoem.setVisible(!anpnPoem.isVisible());
    }

}
