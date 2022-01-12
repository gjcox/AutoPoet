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
        private Token token;
        private int stanzaIndex;
        private int lineIndex;
        private int tokenIndex;
        private boolean clickable;

        private IndexedTokenLabel(Token token, int stanzaIndex, int lineIndex, int tokenIndex,
                boolean clickable) {
            super();
            this.setText(token.getPlaintext());
            this.token = token;
            this.stanzaIndex = stanzaIndex;
            this.lineIndex = lineIndex;
            this.tokenIndex = tokenIndex;
            this.clickable = clickable;
            if (clickable) {
                this.setTextFill(Color.RED);
            }
        }
    }

    Poem poem;

    // Poem & stanza info
    @FXML
    AnchorPane anpnRoot, anpnPoemBlank;
    @FXML
    TitledPane ttlpnPoem;
    @FXML
    TextArea txtarPoem;
    @FXML
    GridPane grdpnPoem;
    @FXML
    FlowPane flwpnLine0;
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

    private FlowPane getEmptyLine() {
        FlowPane guiLine = new FlowPane();
        // guiLine.setMaxHeight(-1);
        // guiLine.setMinHeight(-1);
        return guiLine;
    }

    private FlowPane addEmptyLine(int absLineIndex) {
        RowConstraints textLineConstraint = grdpnPoem.getRowConstraints().get(0);

        FlowPane guiLine = getEmptyLine();
        grdpnPoem.addRow(absLineIndex, guiLine);
        // grdpnPoem.getRowConstraints().add(absLineIndex, textLineConstraint);
        return guiLine;
    }

    private void tokenizePoem() {
        FlowPane guiLine = flwpnLine0; // a flow pane that acts as a line of text in the GUI
        int absLineIndex = 0; // absolute line index, including empty lines

        int stanzaIndex = 0;
        for (Stanza stanza : this.poem.getStanzas()) {
            int lineIndex = 0;
            for (ArrayList<Token> line : stanza.getLines()) {
                // add all tokens to line in the GUI
                int tokenIndex = 0;
                for (Token token : line) {
                    IndexedTokenLabel indToken = new IndexedTokenLabel(token, stanzaIndex, lineIndex, tokenIndex++,
                            token.getClass().equals(SuperWord.class));
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

        System.out.println(grdpnPoem.getChildren().toString());
        System.out.println(grdpnPoem.getRowConstraints().toString());
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
            alert.setContentText("See log for more information.");
            alert.show();
        }
    }

    public void changePoemTextArea() {
        // en/disable and (un)hide text area
        txtarPoem.setDisable(!txtarPoem.isDisabled());
        txtarPoem.setVisible(!txtarPoem.isDisabled());

        // en/disable and (un)hide grid pane
        grdpnPoem.setDisable(!txtarPoem.isDisabled());
        grdpnPoem.setVisible(txtarPoem.isDisabled());
    }

}
