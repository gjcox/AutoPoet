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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionParameters;
import utils.ParameterWrappers.SuggestionParameters.SuggestionPool;
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
    TextField txtfldIntRhymeScheme;
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

        IndexedTokenLabel.mnitmJoinWords.setOnAction(actionEvent -> {
            if (focusedToken == null || secondFocusedToken == null) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Two neighbouring words must be selected to join.");
                alert.show();
            } else {
                joinWords();
            }
        });

        IndexedTokenLabel.mnitmSplitOnSpace.setOnAction(actionEvent -> {
            if (!splitWord(" ")) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

        IndexedTokenLabel.mnitmSplitOnHyphen.setOnAction(actionEvent -> {
            if (!splitWord("-")) {
                Alert alert = getCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

    }

    private static class IndexedTokenLabel extends Label {
        private static final String SELECTABLE_CLASS = "selectableToken";
        private static final String SELECTED_CLASS = "selectedToken";

        private static MenuItem mnitmJoinWords = new MenuItem("Join words");
        private static MenuItem mnitmSplitOnSpace = new MenuItem("Split on space");
        private static MenuItem mnitmSplitOnHyphen = new MenuItem("Split on hypen");
        private static ContextMenu cntxtmnLabel = new ContextMenu();

        private Token token;
        private int stanzaIndex;
        private int lineIndex;
        private int tokenIndex;
        private PartOfSpeech pos;
        private SuggestionParameters pools = new SuggestionParameters();
        private boolean inclUnknown = true;
        private ArrayList<SuperWord> suggestions;
        private Controller controller;

        /**
         * 
         * @param other a potential neighbouring word.
         * @return true if two words are adjacent within a line and there is a single
         *         whitespace character or hyphen between them, otherwise false.
         */
        private boolean isNeighbour(IndexedTokenLabel other) {
            if (other == null)
                return false;
            if (this.getParent().equals(other.getParent())
                    && Math.abs(this.tokenIndex - other.tokenIndex) == 2) {

                IndexedTokenLabel middleToken = (IndexedTokenLabel) ((FlowPane) this.getParent()).getChildren()
                        .get(Math.min(this.tokenIndex, other.tokenIndex) + 1);
                return middleToken.token.getPlaintext().equals(" ") || middleToken.token.getPlaintext().equals("-");
            } else {
                return false;
            }
        }

        private void populateContextMenu() {
            cntxtmnLabel.getItems().clear();
            if (controller.secondFocusedToken != null) {
                cntxtmnLabel.getItems().add(mnitmJoinWords);
            } else {
                if (controller.focusedToken.token.getPlaintext().contains(" ")) {
                    cntxtmnLabel.getItems().add(mnitmSplitOnSpace);
                }
                if (controller.focusedToken.token.getPlaintext().contains("-")) {
                    cntxtmnLabel.getItems().add(mnitmSplitOnHyphen);
                }
            }
        }

        private void selectNeighbour() {
            if (isNeighbour(controller.focusedToken)) {
                if (this.tokenIndex > controller.focusedToken.tokenIndex) {
                    controller.secondFocusedToken = this;
                } else {
                    controller.secondFocusedToken = controller.focusedToken;
                    controller.focusedToken = this;
                }
                controller.highlightWord(controller.secondFocusedToken);
                controller.highlightWord(controller.focusedToken);
            } else if (isNeighbour(controller.secondFocusedToken)) {
                if (this.tokenIndex > controller.secondFocusedToken.tokenIndex) {
                    controller.focusedToken = controller.secondFocusedToken;
                    controller.secondFocusedToken = this;
                } else {
                    controller.focusedToken = this;
                }
                controller.highlightWord(controller.secondFocusedToken);
                controller.highlightWord(controller.focusedToken);
            }
        }

        private IndexedTokenLabel(Controller parentController, Token token, int stanzaIndex, int lineIndex,
                int tokenIndex, boolean clickable) {
            super();
            this.setText(token.getPlaintext());
            this.token = token;
            this.stanzaIndex = stanzaIndex;
            this.lineIndex = lineIndex;
            this.tokenIndex = tokenIndex;
            this.controller = parentController;
            if (clickable) {
                this.getStyleClass().add(SELECTABLE_CLASS);
                this.setContextMenu(cntxtmnLabel);

                this.setOnMouseClicked(actionEvent -> {
                    controller.unhighlightWord(controller.focusedToken);
                    controller.unhighlightWord(controller.secondFocusedToken);

                    if (actionEvent.isControlDown()) {
                        selectNeighbour();
                        populateContextMenu();

                    } else if (actionEvent.getButton() == MouseButton.SECONDARY
                            && controller.secondFocusedToken != null
                            && (this.equals(controller.focusedToken)
                                    || this.equals(controller.secondFocusedToken))) {
                        // context menu will be opened to allow joining words
                        controller.highlightWord(controller.focusedToken);
                        controller.highlightWord(controller.secondFocusedToken);

                    } else {
                        // deselect previous word and focus on clicked word
                        controller.secondFocusedToken = null;

                        controller.focusedToken = this;
                        controller.focusOnStanza(this.stanzaIndex);
                        controller.highlightWord(controller.focusedToken);
                        controller.focusOnWord(controller.focusedToken);
                        populateContextMenu();

                    }
                });
            }
        }
    }

    private Alert getCleanAlert(AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.initOwner(anpnRoot.getScene().getWindow());
        return alert;
    }

    public void test(ActionEvent e) {
        System.out.println(String.format("%s had event %s", e.getSource(), e.getEventType()));
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

    private CheckBox getSuggestionPoolCheckBox(SuggestionPool pool) {
        switch (pool) {
            case COMMONLY_TYPED:
                return chbxCommonlyTyped;
            case COMMON_CATEGORIES:
                return chbxCommonlyCategorised;
            case HAS_PARTS:
                return chbxHasParts;
            case PART_OF:
                return chbxPartOf;
            case SIMILAR_TO:
                return chbxSimilarTo;
            case SYNONYMS:
                return chbxSynonyms;
            default:
                LOG.writeTempLog(String.format(
                        "getSuggestionPoolCheckBox was passed an invalid SuggestionPool \"%s\" and returned null",
                        pool));
                return null;
        }
    }

    /**
     * Tied to the PoS radio buttons within the FXML file.
     */
    public void updatePartOfSpeech(ActionEvent e) {
        focusedToken.pos = SubWord.parsePoS(((RadioButton) e.getSource()).getText());
        enableSuggestionPoolBoxes();
    }

    public void enableSuggestionPoolBoxes() {
        SuperWord superword = ((SuperWord) focusedToken.token);
        for (SuggestionPool pool : SuggestionPool.values()) {
            CheckBox checkBox = getSuggestionPoolCheckBox(pool);
            if (checkBox != null) {
                if (focusedToken.pos == null || !superword.validPool(pool, focusedToken.pos)) {
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
        focusedToken.pools.togglePool(pool, source.isSelected());
    }

    private void unhighlightWord(IndexedTokenLabel token) {
        if (token != null) {
            // remove highlight from previously selected word
            token.getStyleClass().remove(IndexedTokenLabel.SELECTED_CLASS);
        }
    }

    private void highlightWord(IndexedTokenLabel token) {
        if (token != null) {
            token.getStyleClass().add(IndexedTokenLabel.SELECTED_CLASS);
        }
    }

    private void focusOnWord(IndexedTokenLabel focusOnToken) {
        SuperWord superword = (SuperWord) focusOnToken.token;
        RadioButton radioButton;
        boolean hasSubWords = false;
        for (PartOfSpeech pos : PartOfSpeech.values()) {
            radioButton = getPoSRadioButton(pos);
            radioButton.setSelected(focusOnToken.pos != null && focusOnToken.pos.equals(pos));
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
                checkBox.setSelected(focusOnToken.pools.getIncludingPool(pool));
            }
        }
        enableSuggestionPoolBoxes();

        chbxInclUnknown.setSelected(focusOnToken.inclUnknown);
        btnGetSuggestions.setDisable(!hasSubWords);
        displaySuggestions();
    }

    TextFormatter<String> rhymeSchemeFormatter = new TextFormatter<>(change -> {
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

    public void updateIntendedRhymeScheme(ActionEvent e) {
        if (!poem.getStanzas().get(focusedToken.stanzaIndex)
                .setDesiredRhymeScheme(((TextField) e.getSource()).getText())) {
            Window window = ((TextField) e.getSource()).getScene().getWindow();
            tltpIntRhymeScheme.show(window);
            tltpIntRhymeScheme.setAutoHide(true);
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

    private FilterParameters getFilterParams() {
        return new FilterParameters(chbxRhymeWith.isSelected(), SuperWord.getSuperWord(txtfldRhymeWith.getText()),
                null);
    }

    private void makeSubstitution(SuperWord suggestion) {
        poem.substituteWord(focusedToken.stanzaIndex, focusedToken.lineIndex, focusedToken.tokenIndex,
                suggestion);

        IndexedTokenLabel newToken = new IndexedTokenLabel(this, suggestion, focusedToken.stanzaIndex,
                focusedToken.lineIndex, focusedToken.tokenIndex, true);
        newToken.pos = focusedToken.pos;

        // replace old label in GUI
        FlowPane guiLine = (FlowPane) focusedToken.getParent();
        guiLine.getChildren().remove(focusedToken.tokenIndex);
        guiLine.getChildren().add(focusedToken.tokenIndex, newToken);

        // focus on new token by simulating it being clicked on
        Event.fireEvent(newToken, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, false, false, false, null));
    }

    private void joinWords() {
        if (poem.joinWords(focusedToken.stanzaIndex, focusedToken.lineIndex,
                focusedToken.tokenIndex, secondFocusedToken.tokenIndex)) {

            FlowPane guiLine = (FlowPane) focusedToken.getParent();
            IndexedTokenLabel middleToken = (IndexedTokenLabel) guiLine.getChildren().get(focusedToken.tokenIndex + 1);
            String joinedPlaintext = focusedToken.token.getPlaintext() + middleToken.token.getPlaintext()
                    + secondFocusedToken.token.getPlaintext();
            SuperWord joinedSuperWord = SuperWord.getSuperWord(joinedPlaintext);
            IndexedTokenLabel newToken = new IndexedTokenLabel(this, joinedSuperWord, focusedToken.stanzaIndex,
                    focusedToken.lineIndex, focusedToken.tokenIndex, true);

            // replace old tokens in GUI
            guiLine.getChildren().remove(focusedToken.tokenIndex);
            guiLine.getChildren().remove(focusedToken.tokenIndex); // i.e. seperator
            guiLine.getChildren().remove(focusedToken.tokenIndex); // i.e. secondFocusedToken
            guiLine.getChildren().add(focusedToken.tokenIndex, newToken);

            // focus on new token by simulating it being clicked on
            Event.fireEvent(newToken, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false, null));
        }
    }

    private boolean splitWord(String seperator) {
        if (poem.splitWord(focusedToken.stanzaIndex, focusedToken.lineIndex,
                focusedToken.tokenIndex, seperator)) {

            String toSplit = focusedToken.token.getPlaintext();
            int seperatorIndex = toSplit.indexOf(seperator); // won't be -1 if poem.splitWord returned true
            SuperWord subWord1 = SuperWord.getSuperWord(toSplit.substring(0, seperatorIndex));
            Token separatorToken = new Token(seperator);
            SuperWord subWord2 = SuperWord.getSuperWord(toSplit.substring(seperatorIndex + 1));
            IndexedTokenLabel token1 = new IndexedTokenLabel(this, subWord1, focusedToken.stanzaIndex,
                    focusedToken.lineIndex, focusedToken.tokenIndex, true);
            IndexedTokenLabel seperatorToken = new IndexedTokenLabel(this, separatorToken, focusedToken.stanzaIndex,
                    focusedToken.lineIndex, focusedToken.tokenIndex + 1, false);
            IndexedTokenLabel token2 = new IndexedTokenLabel(this, subWord2, focusedToken.stanzaIndex,
                    focusedToken.lineIndex, focusedToken.tokenIndex + 2, true);

            // replace old label in GUI
            FlowPane guiLine = (FlowPane) focusedToken.getParent();
            guiLine.getChildren().remove(focusedToken.tokenIndex);
            guiLine.getChildren().add(focusedToken.tokenIndex, token2);
            guiLine.getChildren().add(focusedToken.tokenIndex, seperatorToken);
            guiLine.getChildren().add(focusedToken.tokenIndex, token1);

            // focus on second subword by simulating it being clicked on
            Event.fireEvent(token2, new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                    false, false, false, false, true, false, false, false, false, false, null));
            return true;
        } else {
            return false;
        }

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
            Alert alert = getCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select a part of speech to get suggestions.");
            alert.show();
        } else if (focusedToken.pools.isEmpty()) {
            Alert alert = getCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select at least one suggestion pool to get suggestions.");
            alert.show();
        } else {
            SuperWord superWord = (SuperWord) focusedToken.token;
            SuggestionParameters suggestionParams = focusedToken.pools;
            FilterParameters filterParams = getFilterParams();

            focusedToken.suggestions = superWord.getFilteredSuggestions(focusedToken.pos, suggestionParams,
                    filterParams);

            displaySuggestions();
        }
    }

}
