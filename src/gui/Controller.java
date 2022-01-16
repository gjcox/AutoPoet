package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionParameters;
import words.Poem;
import words.Stanza;
import words.SubWord;
import words.SuperWord;
import words.Token;
import words.SubWord.PartOfSpeech;

import static config.Configuration.LOG;

public class Controller {

    private static final String SUGGESTION_CLASS = "suggestion";
    private static final String SEE_LOG = "See log for more information.";

    private Poem poem;
    private IndexedTokenLabel focusedToken;
    private File poemFile;

    // Poem & stanza info
    @FXML
    ToggleButton tgbtnDirectEdit;
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
    TitledPane ttlpnCurrentStanza, ttlpnSuggestionParameters, ttlpnSuggestions;
    @FXML
    RadioButton rdbtnNoun, rdbtnPronoun, rdbtnVerb, rdbtnAdjective, rdbtnAdverb, rdbtnPreposition, rdbtnConjunction,
            rdbtnDefiniteArticle, rdbtnUnknown;
    @FXML
    ToggleGroup tglgrpPoS;
    @FXML
    CheckBox chbxInclUnknown;
    @FXML
    CheckBox chbxSynonyms, chbxCommonlyTyped, chbxCommonlyCategorised, chbxPartOf, chbxHasParts, chbxSimilarTo;
    @FXML
    CheckBox chbxRhymeWith;
    @FXML
    TextField txtfldRhymeWith;

    // Suggestions
    @FXML
    ScrollPane scrlpnSuggestions;
    @FXML
    FlowPane flwpnSuggestions;
    @FXML
    Button btnGetSuggestions;

    @FXML
    public void initialize() {
        txtfldIntRhymeScheme.setTextFormatter(rhymeSchemeFormatter);

    }

    private static class IndexedTokenLabel extends Label {
        private static final String SELECTABLE_CLASS = "selectableToken";
        private static final String SELECTED_CLASS = "selectedToken";
        private Token token;
        private int stanzaIndex;
        private int lineIndex;
        private int tokenIndex;
        private PartOfSpeech pos;
        private boolean inclUnknown = true;
        private ArrayList<SuperWord> suggestions;

        private IndexedTokenLabel(Controller parentController, Token token, int stanzaIndex, int lineIndex,
                int tokenIndex,
                boolean clickable) {
            super();
            this.setText(token.getPlaintext());
            this.token = token;
            this.stanzaIndex = stanzaIndex;
            this.lineIndex = lineIndex;
            this.tokenIndex = tokenIndex;
            if (clickable) {
                this.getStyleClass().add(SELECTABLE_CLASS);

                this.setOnMouseClicked(actionEvent -> {
                    parentController.unfocusToken();
                    parentController.focusedToken = this;
                    parentController.focusOnStanza(this.stanzaIndex);
                    parentController.focusOnWord();
                });
            }
        }
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

    private RadioButton getPoSRadioButton(PartOfSpeech pos) {
        switch (pos) {
            case ADJECTIVE:
                return rdbtnAdjective;
            case ADVERB:
                return rdbtnAdverb;
            case CONJUCTION:
                return rdbtnConjunction;
            case DEFINITE_ARTICLE:
                return rdbtnDefiniteArticle;
            case NOUN:
                return rdbtnNoun;
            case PREPOSITION:
                return rdbtnPreposition;
            case PRONOUN:
                return rdbtnPronoun;
            case UNKNOWN:
                return rdbtnUnknown;
            case VERB:
                return rdbtnVerb;
            default:
                LOG.writeTempLog(String.format(
                        "getPoSCheckBox was passed an invalid PartOfSpeech \"%s\" and returned rdbtnUnknown", pos));
                return rdbtnUnknown;
        }
    }

    public void updatePartOfSpeech(ActionEvent e) {
        focusedToken.pos = SubWord.parsePoS(((RadioButton) e.getSource()).getText());
    }

    private void unfocusToken() {
        if (focusedToken != null) {
            // remove highlight from previously selected word
            focusedToken.getStyleClass().remove(IndexedTokenLabel.SELECTED_CLASS);
        }
    }

    private void focusOnWord() {
        focusedToken.getStyleClass().add(IndexedTokenLabel.SELECTED_CLASS);
        SuperWord superword = (SuperWord) focusedToken.token;
        RadioButton radioButton;
        boolean hasSubWords = false;
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            radioButton = getPoSRadioButton(pos);
            radioButton.setSelected(focusedToken.pos != null && focusedToken.pos.equals(pos));
            if (superword.getSubWords(pos, false) == null) {
                radioButton.setDisable(true);
            } else {
                radioButton.setDisable(false);
                hasSubWords = true;
            }
        }
        chbxInclUnknown.setSelected(focusedToken.inclUnknown);
        btnGetSuggestions.setDisable(!hasSubWords);
        displaySuggestions();
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
        if (!poem.getStanzas().get(focusedToken.stanzaIndex)
                .setDesiredRhymeScheme(((TextField) e.getSource()).getText())) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setHeaderText("Could not parse rhyme scheme.");
            alert.setContentText(String.format(SEE_LOG));
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
        poemFile = fileChooser.showOpenDialog(stage);
        if (poemFile == null) {
            return;
        }
        try {
            poem = new Poem(poemFile.toPath());
            ttlpnPoem.setText(poem.getTitle());
            txtarPoem.setText(poem.toString());
            lblPoemStanzaCount.setText(String.valueOf(poem.getStanzaCount()));
            lblPoemLineCount.setText(String.valueOf(poem.getLineCount()));
            tokenizePoem();
            tgbtnDirectEdit.setDisable(false);
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Could not open file.");
            alert.setContentText(String.format("Failed to open %s.", poemFile.getName()));
            alert.show();
        } catch (Exception e) {
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("An unexpected error occured.");
            alert.setContentText(SEE_LOG);
            alert.show();
        }
    }

    public void savePoem() {
        try {
            poem.savePoem(poemFile);
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Could not save poem.");
            alert.setContentText(String.format("Failed to write to %s.\n%s", poemFile.getName(), SEE_LOG));
            LOG.writeTempLog(e.toString() + "\n" + e.getStackTrace());
            alert.show();
        }
    }

    public void savePoemAs() {
        Stage stage = (Stage) anpnRoot.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Poem As");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*"));
        poemFile = fileChooser.showSaveDialog(stage);
        if (poemFile == null) {
            return;
        }
        poem.setTitle(poemFile.getName()); 
        ttlpnPoem.setText(poem.getTitle());
        savePoem();
    }

    public void toggleDirectEdit() {
        // refresh poem content
        if (!txtarPoem.isVisible()) {
            txtarPoem.setText(poem.toString());
            ttlpnCurrentStanza.setDisable(true);
            ttlpnSuggestionParameters.setDisable(true);
            ttlpnSuggestions.setDisable(true);
        } else {
            try {
                poem = new Poem(ttlpnPoem.getText(), txtarPoem.getText());
                tokenizePoem();
                ttlpnCurrentStanza.setDisable(false);
                ttlpnSuggestionParameters.setDisable(false);
                ttlpnSuggestions.setDisable(false);
            } catch (IOException e) {
                LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText("Could not parse directly edited text.");
                alert.setContentText(String.format(SEE_LOG));
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

    private SuggestionParameters getSuggestionParams() {
        return new SuggestionParameters(chbxSynonyms.isSelected(), chbxCommonlyTyped.isSelected(),
                chbxCommonlyCategorised.isSelected(), chbxPartOf.isSelected(),
                chbxHasParts.isSelected(), chbxSimilarTo.isSelected());
    }

    private FilterParameters getFilterParams() {
        return new FilterParameters(chbxRhymeWith.isSelected(), SuperWord.getSuperWord(txtfldRhymeWith.getText()),
                null);
    }

    private void makeSubstitution(SuperWord suggestion) {
        poem.substituteWord(focusedToken.stanzaIndex, focusedToken.lineIndex, focusedToken.tokenIndex,
                suggestion);

        IndexedTokenLabel newTokenLabel = new IndexedTokenLabel(this, suggestion, focusedToken.stanzaIndex,
                focusedToken.lineIndex, focusedToken.tokenIndex, true);
        newTokenLabel.pos = focusedToken.pos;

        // replace old label in GUI
        FlowPane guiLine = (FlowPane) focusedToken.getParent();
        guiLine.getChildren().remove(focusedToken.tokenIndex);
        guiLine.getChildren().add(focusedToken.tokenIndex, newTokenLabel);

        // focus on new token by simulating it being clicked on
        Event.fireEvent(newTokenLabel, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
    }

    private void displaySuggestions() {
        boolean snapToPixel = true;
        boolean cache = false;
        flwpnSuggestions.getChildren().clear();
        flwpnSuggestions.setSnapToPixel(snapToPixel);
        flwpnSuggestions.setCache(cache);
        flwpnSuggestions.getParent().setCache(cache);

        if (focusedToken.suggestions != null) {
            for (SuperWord suggestion : focusedToken.suggestions) {
                Label label = new Label(suggestion.getPlaintext());
                label.getStyleClass().add(SUGGESTION_CLASS);
                label.setSnapToPixel(snapToPixel);
                label.setCache(cache);
                label.setOnMouseClicked(actionEvent -> {
                    makeSubstitution(suggestion);
                });
                flwpnSuggestions.getChildren().add(label);
            }
        } else {
            Label noSuggestions = new Label("No suggestions.");
            noSuggestions.setSnapToPixel(snapToPixel);
            noSuggestions.setCache(cache);

            flwpnSuggestions.getChildren().add(noSuggestions);
        }
    }

    public void getSuggestions() {
        if (focusedToken.pos == null) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText("Please select a part of speech to get suggestions.");
            alert.show();
        } else {
            SuperWord superWord = (SuperWord) focusedToken.token;
            SuggestionParameters suggestionParams = getSuggestionParams();
            FilterParameters filterParams = getFilterParams();

            focusedToken.suggestions = superWord.getFilteredSuggestions(focusedToken.pos, suggestionParams,
                    filterParams);

            displaySuggestions();
        }
    }

}
