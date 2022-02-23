package gui;

import static config.Configuration.LOG;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionPoolParameters;
import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;
import words.Poem;
import words.Stanza;
import words.SubWord.PartOfSpeech;
import words.SuperWord;
import words.Token;

public class Controller {

    private static final String SUGGESTION_CLASS = "suggestion";
    private static final String SEE_LOG = "See log for more information.";

    private Poem poem;
    private IndexedTokenLabel focusedToken;
    private IndexedTokenLabel secondFocusedToken; // the word after the focusedToken, or null
    private File poemFile;

    // Poem & stanza info
    @FXML
    ToggleButton tgbtnDirectEdit;
    @FXML
    AnchorPane anpnRoot, anpnPoem;
    @FXML
    ScrollPane scrlpnPoem;
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
    TextField txtfldDefaultRhymeScheme, txtfldIntRhymeScheme;
    @FXML
    Tooltip tltpIntRhymeScheme;

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
    GridPane grdPnSuggestionPools;
    HashMap<String, CheckBox> suggestionPoolCheckBoxes = new HashMap<>();
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
        txtfldDefaultRhymeScheme.setTextFormatter(getRhymeSchemeFormatter());
        txtfldIntRhymeScheme.setTextFormatter(getRhymeSchemeFormatter());

        initSuggestionPoolCheckBoxes();

        IndexedTokenLabel.setJoinWordsAction(actionEvent -> {
            if (focusedToken == null || secondFocusedToken == null) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Two neighbouring words must be selected to join.");
                alert.show();
            } else {
                joinWords();
            }
        });

        IndexedTokenLabel.setSplitOnSpaceAction(actionEvent -> {
            if (!splitWord(" ")) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

        IndexedTokenLabel.setSplitOnHyphenAction(actionEvent -> {
            if (!splitWord("-")) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

    }

    // element fabricators and utilities

    private Alert getCleanAlert(AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.initOwner(anpnRoot.getScene().getWindow());
        return alert;
    }

    private FlowPane getEmptyLine() {
        FlowPane guiLine = new FlowPane();
        guiLine.setPrefWrapLength(Double.MAX_VALUE);
        return guiLine;
    }

    private FlowPane addEmptyLine(int absLineIndex) {
        FlowPane guiLine = getEmptyLine();
        grdpnPoem.addRow(absLineIndex, guiLine);
        return guiLine;
    }

    private CheckBox getPoolCheckBox(SuggestionPool pool) {
        CheckBox checkBox = new CheckBox(pool.getLabel());
        checkBox.setOnAction(this::updateSuggestionPool);
        checkBox.setDisable(true);
        return checkBox;
    }

    private void initSuggestionPoolCheckBoxes() {
        int row = 1; // row 0 is the list title
        for (SuggestionPool pool : SuggestionPool.values()) {
            CheckBox checkBox = getPoolCheckBox(pool);
            grdPnSuggestionPools.addRow(row++, checkBox);
            suggestionPoolCheckBoxes.put(pool.getLabel(), checkBox);
        }
    }

    /**
     * Ensures that rhyme schemes are only made up of uppercase latin letters and
     * hashes.
     */
    private TextFormatter<String> getRhymeSchemeFormatter() {
        TextFormatter<String> formatter = new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }

            change.setText(change.getText().toUpperCase());
            String text = change.getControlNewText();
            if (!text.matches("[A-Z#]*")) {
                return null;
            }

            return change;
        });
        return formatter;
    }

    // getters

    public IndexedTokenLabel getFocusedToken() {
        return focusedToken;
    }

    public IndexedTokenLabel getSecondFocusedToken() {
        return secondFocusedToken;
    }

    // setters

    public void setFocusedToken(IndexedTokenLabel token) {
        this.focusedToken = token;
    }

    public void setSecondFocusedToken(IndexedTokenLabel token) {
        this.secondFocusedToken = token;
    }

    // internal getters

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

    private CheckBox getSuggestionPoolCheckBox(SuggestionPool pool) {
        return suggestionPoolCheckBoxes.get(pool.getLabel());
    }

    private FilterParameters getFilterParams() {
        return new FilterParameters(chbxRhymeWith.isSelected(), SuperWord.getSuperWord(txtfldRhymeWith.getText()),
                null);
    }

    public void getSuggestions() {
        if (focusedToken.getPos() == null) {
            Alert alert = getCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select a part of speech to get suggestions.");
            alert.show();
        } else if (focusedToken.getPoolParams().isEmpty()) {
            Alert alert = getCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select at least one suggestion pool to get suggestions.");
            alert.show();
        } else {
            SuperWord superWord = (SuperWord) focusedToken.getToken();
            PartOfSpeech pos = focusedToken.getPos();
            SuggestionPoolParameters suggestionParams = focusedToken.getPoolParams();
            FilterParameters filterParams = getFilterParams();

            focusedToken.setSuggestions(superWord.getFilteredSuggestions(pos, suggestionParams, filterParams));

            displaySuggestions();
        }
    }

    // per-token interactions

    /**
     * Tied to the PoS radio buttons within the FXML file.
     */
    public void updatePartOfSpeech(ActionEvent e) {
        focusedToken.setPos(PartOfSpeech.fromString(((RadioButton) e.getSource()).getText()));
        enableSuggestionPoolBoxes();
    }

    public void enableSuggestionPoolBoxes() {
        SuperWord superword = ((SuperWord) focusedToken.getToken());
        for (SuggestionPool pool : SuggestionPool.values()) {
            CheckBox checkBox = getSuggestionPoolCheckBox(pool);
            if (checkBox != null) {
                if (focusedToken.getPos() == null || !superword.validPool(pool, focusedToken.getPos())) {
                    focusedToken.getPoolParams().togglePool(pool, false);
                    checkBox.setDisable(true);
                    checkBox.setSelected(false);
                } else {
                    checkBox.setDisable(false);
                }
            }
        }
    }

    /**
     * Tied to the suggestion pool checkboxes in the FXML file.
     */
    public void updateSuggestionPool(ActionEvent e) {
        CheckBox source = (CheckBox) e.getSource();
        SuggestionPool pool = SuggestionPool.fromString(source.getText());
        focusedToken.getPoolParams().togglePool(pool, source.isSelected());
    }

    public void updateIncludeUnknowns(ActionEvent e) {
        CheckBox source = (CheckBox) e.getSource();
        focusedToken.getPoolParams().setInclusiveUnknown(source.isSelected());
    }

    public void focusOnStanza(int stanzaIndex) {
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

    public void focusOnWord(IndexedTokenLabel focusOnToken) {
        SuperWord superword = (SuperWord) focusOnToken.getToken();
        RadioButton radioButton;
        boolean hasSubWords = false;
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            radioButton = getPoSRadioButton(pos);
            radioButton.setSelected(focusOnToken.getPos() != null && focusOnToken.getPos().equals(pos));
            if (superword.getSubWords(pos, false) == null) {
                radioButton.setDisable(true);
            } else {
                radioButton.setDisable(false);
                hasSubWords = true;
            }
        }
        CheckBox checkBox;
        for (SuggestionPool pool : SuggestionPool.values()) {
            checkBox = getSuggestionPoolCheckBox(pool);
            if (checkBox != null) {
                checkBox.setSelected(focusOnToken.getPoolParams().includes(pool));
            }
        }

        enableSuggestionPoolBoxes();
        chbxInclUnknown.setSelected(focusOnToken.getInclUnknown());
        btnGetSuggestions.setDisable(!hasSubWords);
        displaySuggestions();
    }

    // model (i.e. poem) interactions

    public void updateIntendedRhymeScheme(ActionEvent e) {
        if (!poem.getStanzas().get(focusedToken.getStanzaIndex())
                .setDesiredRhymeScheme(((TextField) e.getSource()).getText())) {
            Window window = ((TextField) e.getSource()).getScene().getWindow();
            tltpIntRhymeScheme.show(window);
            tltpIntRhymeScheme.setAutoHide(true);
        }
    }

    public void updateDefaultRhymeScheme(ActionEvent e) {
        updateDefaultRhymeScheme((TextField) e.getSource());
    }

    public void updateDefaultRhymeScheme(TextField field) {
        poem.setDefaultRhymeScheme(field.getText());

        // refocus on current token to update intended rhyme scheme
        if (focusedToken != null) {
            Event.fireEvent(focusedToken, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false, null));
        }
    }

    private void makeSubstitution(SuperWord suggestion) {
        poem.substituteWord(focusedToken.getStanzaIndex(), focusedToken.getLineIndex(), focusedToken.getTokenIndex(),
                suggestion);

        IndexedTokenLabel newToken = new IndexedTokenLabel(this, suggestion, focusedToken.getStanzaIndex(),
                focusedToken.getLineIndex(), focusedToken.getTokenIndex(), true);
        newToken.setPos(focusedToken.getPos());

        // replace old label in GUI
        FlowPane guiLine = (FlowPane) focusedToken.getParent();
        guiLine.getChildren().remove(focusedToken.getTokenIndex());
        guiLine.getChildren().add(focusedToken.getTokenIndex(), newToken);

        // focus on new token by simulating it being clicked on
        Event.fireEvent(newToken, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
    }

    private void joinWords() {
        if (poem.joinWords(focusedToken.getStanzaIndex(), focusedToken.getLineIndex(),
                focusedToken.getTokenIndex(), secondFocusedToken.getTokenIndex())) {

            int insertionIndex = focusedToken.getTokenIndex();

            FlowPane guiLine = (FlowPane) focusedToken.getParent();
            IndexedTokenLabel middleToken = (IndexedTokenLabel) guiLine.getChildren()
                    .get(focusedToken.getTokenIndex() + 1);
            String joinedPlaintext = focusedToken.getToken().getPlaintext() + middleToken.getToken().getPlaintext()
                    + secondFocusedToken.getToken().getPlaintext();
            SuperWord joinedSuperWord = SuperWord.getSuperWord(joinedPlaintext);
            IndexedTokenLabel newToken = new IndexedTokenLabel(this, joinedSuperWord, focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex, true);

            // replace old tokens in GUI
            guiLine.getChildren().remove(insertionIndex);
            guiLine.getChildren().remove(insertionIndex); // i.e. seperator
            guiLine.getChildren().remove(insertionIndex); // i.e. secondFocusedToken
            guiLine.getChildren().add(insertionIndex, newToken);

            // update tokenIndex of subsequent tokens in line
            for (int i = insertionIndex + 1; i < guiLine.getChildren().size(); i++) {
                ((IndexedTokenLabel) guiLine.getChildren().get(i)).incrementTokenIndex(-2);
            }

            // focus on new token by simulating it being clicked on
            Event.fireEvent(newToken, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false, null));
        }
    }

    private boolean splitWord(String seperator) {
        if (poem.splitWord(focusedToken.getStanzaIndex(), focusedToken.getLineIndex(),
                focusedToken.getTokenIndex(), seperator)) {

            int insertionIndex = focusedToken.getTokenIndex();

            String toSplit = focusedToken.getToken().getPlaintext();
            int seperatorIndex = toSplit.indexOf(seperator); // won't be -1 if poem.splitWord returned true
            SuperWord subWord1 = SuperWord.getSuperWord(toSplit.substring(0, seperatorIndex));
            Token separatorToken = new Token(seperator);
            SuperWord subWord2 = SuperWord.getSuperWord(toSplit.substring(seperatorIndex + 1));

            IndexedTokenLabel token1 = new IndexedTokenLabel(this, subWord1, focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex, true);
            IndexedTokenLabel seperatorToken = new IndexedTokenLabel(this, separatorToken,
                    focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex + 1, false);
            IndexedTokenLabel token2 = new IndexedTokenLabel(this, subWord2, focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex + 2, true);

            // replace old label in GUI
            FlowPane guiLine = (FlowPane) focusedToken.getParent();
            guiLine.getChildren().remove(insertionIndex);
            guiLine.getChildren().add(insertionIndex, token2);
            guiLine.getChildren().add(insertionIndex, seperatorToken);
            guiLine.getChildren().add(insertionIndex, token1);

            // update tokenIndex of subsequent tokens in line
            for (int i = insertionIndex + 3; i < guiLine.getChildren().size(); i++) {
                ((IndexedTokenLabel) guiLine.getChildren().get(i)).incrementTokenIndex(2);
            }

            // focus on second subword by simulating it being clicked on
            Event.fireEvent(token2, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false, null));
            return true;
        } else {
            return false;
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

    // file IO

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
            txtfldDefaultRhymeScheme.clear();
            lblPoemStanzaCount.setText(String.valueOf(poem.getStanzaCount()));
            lblPoemLineCount.setText(String.valueOf(poem.getLineCount()));
            tokenizePoem();
            tgbtnDirectEdit.setDisable(false);
        } catch (IOException e) {
            Alert alert = getCleanAlert(AlertType.ERROR);
            alert.setHeaderText("Could not open file.");
            alert.setContentText(String.format("Failed to open %s.\n%s", poemFile.getName(), SEE_LOG));
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            alert.show();
        } catch (Exception e) {
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            Alert alert = getCleanAlert(AlertType.ERROR);
            alert.setHeaderText("An unexpected error occured.");
            alert.setContentText(SEE_LOG);
            alert.show();
        }
    }

    public void savePoem() {
        if (poemFile == null) {
            savePoemAs();
            return;
        }

        try {
            poem.savePoem(poemFile);
        } catch (IOException e) {
            Alert alert = getCleanAlert(AlertType.ERROR);
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
        fileChooser.setInitialFileName(poem.getTitle());
        poemFile = fileChooser.showSaveDialog(stage);
        if (poemFile == null) {
            return;
        }
        poem.setTitle(poemFile.getName());
        ttlpnPoem.setText(poem.getTitle());
        savePoem();
    }

    public void newPoem() {
        TextInputDialog poemTitleDialog = new TextInputDialog();
        poemTitleDialog.setHeaderText(null);
        poemTitleDialog.setGraphic(null);
        poemTitleDialog.setTitle("Poem Title");
        poemTitleDialog.setContentText("Enter poem title");
        poemTitleDialog.showAndWait().ifPresent(title -> {
            try {
                poem = new Poem(title, "");
                poemFile = null;
                txtfldDefaultRhymeScheme.clear();
                ttlpnPoem.setText(poem.getTitle());
                tgbtnDirectEdit.setDisable(false);
                if (!txtarPoem.isVisible()) {
                    tgbtnDirectEdit.setSelected(true);
                    toggleDirectEdit();
                }
            } catch (IOException e) {
                Alert alert = getCleanAlert(AlertType.ERROR);
                alert.setHeaderText("Could not create blank poem.");
                alert.setContentText(SEE_LOG);
                LOG.writeTempLog(e.toString() + "\n" + e.getStackTrace());
                alert.show();
            }
        });
    }

    // other

    public void toggleDirectEdit() {
        // refresh poem content
        if (!txtarPoem.isVisible()) {
            txtarPoem.setText(poem.toString());
            ttlpnCurrentStanza.setDisable(true);
            ttlpnSuggestionParameters.setDisable(true);
            ttlpnSuggestions.setDisable(true);
        } else {
            try {
                focusedToken = null;
                secondFocusedToken = null;
                poem = new Poem(ttlpnPoem.getText(), txtarPoem.getText());
                tokenizePoem();
                updateDefaultRhymeScheme(txtfldDefaultRhymeScheme);
                ttlpnCurrentStanza.setDisable(false);
                ttlpnSuggestionParameters.setDisable(false);
                ttlpnSuggestions.setDisable(false);
            } catch (IOException e) {
                LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
                Alert alert = getCleanAlert(AlertType.ERROR);
                alert.setHeaderText("Could not parse directly edited text.");
                alert.setContentText(String.format(SEE_LOG));
                alert.show();
            }
        }

        lblPoemStanzaCount.setText(String.valueOf(poem.getStanzaCount()));
        lblPoemLineCount.setText(String.valueOf(poem.getLineCount()));

        // (un)hide text area
        txtarPoem.setVisible(!txtarPoem.isVisible());

        // (un)hide scroll pane
        scrlpnPoem.setVisible(!scrlpnPoem.isVisible());
    }

    private void displaySuggestions() {
        boolean snapToPixel = true;
        boolean cache = false;
        flwpnSuggestions.getChildren().clear();
        flwpnSuggestions.setSnapToPixel(snapToPixel);
        flwpnSuggestions.setCache(cache);
        flwpnSuggestions.getParent().setCache(cache);

        if (focusedToken.getSuggestions() != null) {
            for (SuperWord suggestion : focusedToken.getSuggestions()) {
                Label label = new Label(suggestion.getPlaintext());
                label.getStyleClass().add(SUGGESTION_CLASS);
                label.setSnapToPixel(snapToPixel);
                label.setCache(cache);
                label.setOnMouseClicked(actionEvent -> makeSubstitution(suggestion));
                flwpnSuggestions.getChildren().add(label);
            }
        } else {
            Label noSuggestions = new Label("No results.");
            noSuggestions.setSnapToPixel(snapToPixel);
            noSuggestions.setCache(cache);

            flwpnSuggestions.getChildren().add(noSuggestions);
        }
    }

}
