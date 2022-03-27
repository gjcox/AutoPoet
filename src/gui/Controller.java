package gui;

import static config.Configuration.LOG;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
import utils.ParameterWrappers.FilterParameters.RhymeType;
import utils.ParameterWrappers.SuggestionPoolParameters;
import utils.ParameterWrappers.SuggestionPoolParameters.SuggestionPool;
import words.PartOfSpeech;
import words.Poem;
import words.RhymingScheme;
import words.Stanza;
import words.SuperWord;
import words.Token;

/**
 * This class acts as the (view and) controller for the GUI.
 * 
 * @author 190021081
 */
public class Controller {

    private static final String SUGGESTION_CLASS = "suggestion";
    private static final String SEE_LOG = "See log for more information.";

    private Poem poem;
    private IndexedTokenLabel focusedToken;
    private IndexedTokenLabel secondFocusedToken; // for joining two token together
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
    GridPane grdPnFilters;
    EnumMap<RhymeType, CheckBox> rhymeTypeCheckBoxes = new EnumMap<>(RhymeType.class);
    CheckBox chbxSyllableCount;
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
        txtfldDefaultRhymeScheme.setTextFormatter(buildRhymeSchemeFormatter());
        txtfldIntRhymeScheme.setTextFormatter(buildRhymeSchemeFormatter());

        initSuggestionPoolCheckBoxes();
        initFilterCheckBoxes();

        IndexedTokenLabel.setJoinWordsAction(actionEvent -> {
            if (focusedToken == null || secondFocusedToken == null) {
                Alert alert = buildCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Two neighbouring words must be selected to join.");
                alert.show();
            } else {
                joinWords();
            }
        });

        IndexedTokenLabel.setSplitOnSpaceAction(actionEvent -> {
            if (!splitWord(" ")) {
                Alert alert = buildCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

        IndexedTokenLabel.setSplitOnHyphenAction(actionEvent -> {
            if (!splitWord("-")) {
                Alert alert = buildCleanAlert(AlertType.INFORMATION);
                alert.setContentText("Selected word could not be split.");
                alert.show();
            }
        });

    }

    // element fabricators and utilities

    private Alert buildCleanAlert(AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.initOwner(anpnRoot.getScene().getWindow());
        return alert;
    }

    private FlowPane buildEmptyLine() {
        FlowPane guiLine = new FlowPane();
        guiLine.setPrefWrapLength(Double.MAX_VALUE);
        return guiLine;
    }

    private FlowPane addEmptyLine(int absLineIndex) {
        FlowPane guiLine = buildEmptyLine();
        grdpnPoem.addRow(absLineIndex, guiLine);
        return guiLine;
    }

    private CheckBox buildSuggestionPoolCheckBox(SuggestionPool pool) {
        CheckBox checkBox = new CheckBox(pool.getLabel());
        checkBox.setOnAction(this::updateSuggestionPool);
        checkBox.setDisable(true);
        return checkBox;
    }

    private void initSuggestionPoolCheckBoxes() {
        int row = 1; // row 0 is the list title
        for (SuggestionPool pool : SuggestionPool.values()) {
            CheckBox checkBox = buildSuggestionPoolCheckBox(pool);
            grdPnSuggestionPools.addRow(row++, checkBox);
            suggestionPoolCheckBoxes.put(pool.getLabel(), checkBox);
        }
    }

    private CheckBox buildRhymeTypeCheckBox(RhymeType rhymeType) {
        return buildFilterCheckBox(rhymeType.getLabel(), rhymeType.getExplanation());
    }

    private CheckBox buildFilterCheckBox(String label, String tooltipText) {
        CheckBox checkBox = new CheckBox(label);
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setPrefWidth(220);
        tooltip.setWrapText(true);
        checkBox.setTooltip(tooltip);
        checkBox.setDisable(false);
        return checkBox;
    }

    private void initFilterCheckBoxes() {
        int row = 3; // row 0 is the list title, 1 is textfield, 2 is a separator

        // add rhyme type checkboxes
        for (RhymeType filter : RhymeType.values()) {
            if (filter.equals(RhymeType.FORCED_RHYME)) {
                continue; // not implemented yet
            }
            CheckBox checkBox = buildRhymeTypeCheckBox(filter);
            grdPnFilters.addRow(row++, checkBox);
            rhymeTypeCheckBoxes.put(filter, checkBox);
        }

        // add separator and syllable count checkbox
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 5, 2, 0));
        grdPnFilters.addRow(row++, separator);
        chbxSyllableCount = buildFilterCheckBox("syllable count", "Suggestions must match the word's syllable count.");
        chbxSyllableCount.setDisable(true); // word dependent
        grdPnFilters.addRow(row, chbxSyllableCount);
    }

    private TextFormatter<String> buildRhymeSchemeFormatter() {
        /*
         * Ensures that rhyme schemes are only made up of uppercase latin letters and
         * hashes.
         */
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

    // getters, public for use by IndexedTokenLabel

    /**
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @return a token selected by the user.
     */
    public IndexedTokenLabel getFocusedToken() {
        return focusedToken;
    }

    /**
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @return a secondary token selected by the user.
     */
    public IndexedTokenLabel getSecondFocusedToken() {
        return secondFocusedToken;
    }

    // setters, public for use by IndexedTokenLabel

    /**
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @param token to be focussed on.
     */
    public void setFocusedToken(IndexedTokenLabel token) {
        this.focusedToken = token;
    }

    /**
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @param token to be focussed on secondarily (for joining operation).
     */
    public void setSecondFocusedToken(IndexedTokenLabel token) {
        this.secondFocusedToken = token;
    }

    // internal getters

    private RadioButton getPoSRadioButton(PartOfSpeech pos) {
        // statically defined in FXML rather than generated in initialisation, hence not
        // ... using an EnumMap
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

    /*
     * Checks backwards from end of a line until a superword is found (so that
     * commas etc. are ignored).
     */
    private IndexedTokenLabel getLastToken(FlowPane guiLine) {
        int index = guiLine.getChildren().size() - 1;
        IndexedTokenLabel token = null;
        boolean superword = false;
        while (index >= 0 && !superword) {
            token = (IndexedTokenLabel) guiLine.getChildren().get(index--);
            superword = (token.getToken().getClass() == SuperWord.class);
        }
        return superword ? (IndexedTokenLabel) token : null;
    }

    /*
     * Constructs the filter parameters from the relevant checkboxes.
     */
    private FilterParameters getFilterParams() {
        FilterParameters params = new FilterParameters();

        params.setSyllableCountFilter(chbxSyllableCount.isSelected() && !chbxSyllableCount.isDisabled());

        List<RhymeType> chosenRhymeTypes = new ArrayList<>();

        // iterate over rhyme checkboxes to see which (if any) are ticked
        for (RhymeType rhymeType : RhymeType.values()) {
            if (rhymeType.equals(RhymeType.FORCED_RHYME))
                continue; // not implemented yet
            if (rhymeTypeCheckBoxes.get(rhymeType).isSelected() && !rhymeTypeCheckBoxes.get(rhymeType).isDisabled()) {
                chosenRhymeTypes.add(rhymeType);
            }
        }

        if (!chosenRhymeTypes.isEmpty()) {
            // populate rhyme filters

            if (txtfldRhymeWith.getText().equals("")) {
                // get rhymes from scheme
                if (!focusedToken.equals(getLastToken((FlowPane) focusedToken.getParent()))) {
                    Alert alert = buildCleanAlert(AlertType.WARNING);
                    alert.setHeaderText("Could not filter suggestions.");
                    alert.setContentText(
                            "Only the last word in a line can be matched against the intended rhyme scheme.");
                    alert.show();
                    return new FilterParameters();
                }

                // for all words of same value in rhyme scheme (e.g. A or B)
                for (IndexedTokenLabel matchWith : getRhymesFromScheme()) {
                    params.setMatchPoS(matchWith.getPos());
                    // iterate over rhyme checkboxes to see which are ticked
                    for (RhymeType rhymeType : chosenRhymeTypes) {
                        // add word from scheme to rhyme filter
                        params.setRhymeFilter(rhymeType, (SuperWord) matchWith.getToken());

                    }
                }

            } else {
                // get rhyme from text field
                SuperWord matchWith = SuperWord.getSuperWord(txtfldRhymeWith.getText());
                params.setMatchPoS(null);
                // iterate over rhyme checkboxes to see which are ticked
                for (RhymeType filter : RhymeType.values()) {
                    if (rhymeTypeCheckBoxes.get(filter).isSelected()) {
                        // add word from text field to rhyme filter
                        params.setRhymeFilter(filter, matchWith);
                    }
                }
            }
        }

        return params;
    }

    // per-token interactions

    /**
     * Tied to the PoS radio buttons within the FXML file.
     */
    public void updatePartOfSpeech(ActionEvent e) {
        focusedToken.setPos(PartOfSpeech.fromString(((RadioButton) e.getSource()).getText()));
        enableSuggestionPoolBoxes();
    }

    private void enableSuggestionPoolBoxes() {
        SuperWord superword = ((SuperWord) focusedToken.getToken());
        for (SuggestionPool pool : SuggestionPool.values()) {
            CheckBox checkBox = suggestionPoolCheckBoxes.get(pool.getLabel());
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

    /**
     * Tied to the include unknowns checkbox in the FXML file.
     */
    public void updateIncludeUnknowns(ActionEvent e) {
        CheckBox source = (CheckBox) e.getSource();
        try {
            focusedToken.getPoolParams().setInclusiveUnknown(source.isSelected());
        } catch (NullPointerException npe) {
            // checkbox used before poem loaded
            // do nothing
        }
    }

    private void enableFilterBoxes() {
        // prevents the user from attempting to match syllables against an unknown
        SuperWord superword = ((SuperWord) focusedToken.getToken());
        chbxSyllableCount.setDisable(superword.getSyllableCount(null) <= 0);
    }

    /**
     * Updates the stanza information in the right-hand pane.
     * 
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @param stanzaIndex zero-indexed.
     */
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

    /**
     * Updates the word-level information in the right-hand pane (checkboxes, radio
     * buttons, suggestions).
     * 
     * Public for use in {@link gui.IndexedTokenLabel}
     * 
     * @param focusOnToken typically {@link gui.Controller#focusedToken}.
     */
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
            checkBox = suggestionPoolCheckBoxes.get(pool.getLabel());
            if (checkBox != null) {
                checkBox.setSelected(focusOnToken.getPoolParams().includes(pool));
            }
        }

        enableSuggestionPoolBoxes();
        chbxInclUnknown.setSelected(focusOnToken.getInclUnknown());
        btnGetSuggestions.setDisable(!hasSubWords);
        enableFilterBoxes();
        displaySuggestions();
    }

    // model (i.e. poem) interactions

    /**
     * Attempts to set the intended rhyme scheme for the stanza that the focused
     * token is within.
     * 
     * Public for reference within the FXML file.
     * 
     * @param e from {@link gui.Controller#txtfldIntRhymeScheme}.
     */
    public void updateIntendedRhymeScheme(ActionEvent e) {
        if (focusedToken == null) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setHeaderText("Could not set intended rhyme scheme.");
            alert.setContentText("Please select a word (and by extension a stanza) to set the intended rhyme scheme.");
            alert.show();
        } else if (!poem.getStanzas().get(focusedToken.getStanzaIndex())
                .setDesiredRhymeScheme(((TextField) e.getSource()).getText())) {
            Window window = ((TextField) e.getSource()).getScene().getWindow();
            tltpIntRhymeScheme.show(window);
            tltpIntRhymeScheme.setAutoHide(true);
        }
    }

    /**
     * Attempts to set the default rhyme scheme for the poem.
     * 
     * Public for reference within the FXML file.
     * 
     * @param e from {@link gui.Controller#txtfldDefaultRhymeScheme}.
     */
    public void updateDefaultRhymeScheme(ActionEvent e) {
        updateDefaultRhymeScheme((TextField) e.getSource());
    }

    /**
     * Attempts to set the default rhyme scheme for the poem.
     * 
     * Public for reference within the FXML file.
     * 
     * @param field should be {@link gui.Controller#txtfldDefaultRhymeScheme}.
     */
    private void updateDefaultRhymeScheme(TextField field) {
        if (poem == null) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setHeaderText("Could not set default rhyme scheme.");
            alert.setContentText("Please load or create poem to set a default rhyme scheme.");
            alert.show();
        } else {
            poem.setDefaultRhymeScheme(field.getText());

            // refocus on current token to update intended rhyme scheme
            if (focusedToken != null) {
                Event.fireEvent(focusedToken,
                        new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                                false, false, false, false, true, false, false, false, false, false, null));
            }
        }
    }

    private ArrayList<IndexedTokenLabel> getRhymesFromScheme() {
        ArrayList<IndexedTokenLabel> rhymes = new ArrayList<>();
        Stanza currentStanza = poem.getStanzas().get(focusedToken.getStanzaIndex());
        RhymingScheme scheme = currentStanza.getDesiredRhymeScheme();
        int schemeValue;

        if (scheme == null) {
            Alert alert = buildCleanAlert(AlertType.WARNING);
            alert.setHeaderText("Could not filter suggestions.");
            alert.setContentText("The current stanza does not have a set intended rhyme scheme.");
            alert.show();
            return rhymes;
        } else if ((schemeValue = scheme.getValue(focusedToken.getLineIndex())) == '#') {
            return rhymes;
        } else {
            for (int i = 0; i < currentStanza.lineCount(); i++) {
                if (i == focusedToken.getLineIndex() || scheme.getValue(i) != schemeValue) {
                    // skip current line
                } else {
                    FlowPane guiLine = (FlowPane) grdpnPoem.getChildren().get(currentStanza.getStartLine() + i);
                    rhymes.add(getLastToken(guiLine));
                }
            }
        }

        return rhymes;
    }

    /**
     * Gets the suggested substitutions for the focused token based on the search
     * parameter checkboxes. Suggestion finding is done in a daemon thread, as it
     * can be slow.
     */
    public void getSuggestions() {
        if (focusedToken == null) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select a word to get suggestions.");
            alert.show();
        } else if (secondFocusedToken != null) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please only select one word to get suggestions.");
            alert.show();
        } else if (focusedToken.getPos() == null) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select a part of speech to get suggestions.");
            alert.show();
        } else if (focusedToken.getPoolParams().isEmpty()) {
            Alert alert = buildCleanAlert(AlertType.INFORMATION);
            alert.setContentText("Please select at least one suggestion pool to get suggestions.");
            alert.show();
        } else {
            SuperWord superWord = (SuperWord) focusedToken.getToken();
            PartOfSpeech pos = focusedToken.getPos();
            SuggestionPoolParameters suggestionParams = focusedToken.getPoolParams();
            FilterParameters filterParams = getFilterParams();

            Task<ArrayList<SuperWord>> task = new GetSuggestionsTask(superWord, pos, suggestionParams, filterParams);

            IndexedTokenLabel currentFocusPointer = focusedToken;

            task.setOnSucceeded(v -> {
                currentFocusPointer.setSuggestions(task.getValue());
                if (currentFocusPointer.equals(focusedToken))
                    displaySuggestions();
            });

            flwpnSuggestions.getChildren().clear();
            flwpnSuggestions.getChildren().add(new Label("Searching and/or filtering..."));
            Thread suggestionsThread = new Thread(task);
            suggestionsThread.setDaemon(true);
            suggestionsThread.start();
        }
    }

    /*
     * Substitutes the currently selected word in the poem model for a new one, and
     * updates the selected GUI token accordingly.
     */
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

    /*
     * Combines two superwords into one, to help users make use of WordsAPI's
     * recognition of some short phrases.
     */
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
            guiLine.getChildren().remove(insertionIndex); // i.e. separator
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

    /*
     * Seperates superword into two, to help users get around WordsAPI's
     * recognition of some short phrases.
     */
    private boolean splitWord(String separator) {
        if (poem.splitWord(focusedToken.getStanzaIndex(), focusedToken.getLineIndex(),
                focusedToken.getTokenIndex(), separator)) {

            int insertionIndex = focusedToken.getTokenIndex();

            String toSplit = focusedToken.getToken().getPlaintext();
            int separatorIndex = toSplit.indexOf(separator); // won't be -1 if poem.splitWord returned true
            SuperWord subWord1 = SuperWord.getSuperWord(toSplit.substring(0, separatorIndex));
            Token separatorToken = new Token(separator);
            SuperWord subWord2 = SuperWord.getSuperWord(toSplit.substring(separatorIndex + 1));

            IndexedTokenLabel token1 = new IndexedTokenLabel(this, subWord1, focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex, true);
            IndexedTokenLabel separatorGuiToken = new IndexedTokenLabel(this, separatorToken,
                    focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex + 1, false);
            IndexedTokenLabel token2 = new IndexedTokenLabel(this, subWord2, focusedToken.getStanzaIndex(),
                    focusedToken.getLineIndex(), insertionIndex + 2, true);

            // replace old label in GUI
            FlowPane guiLine = (FlowPane) focusedToken.getParent();
            guiLine.getChildren().remove(insertionIndex);
            guiLine.getChildren().add(insertionIndex, token2);
            guiLine.getChildren().add(insertionIndex, separatorGuiToken);
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

    /**
     * Generates the GUI version of the poem as a collection of clickable word
     * tokens.
     */
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

    /**
     * Opens a dialogue for the user to select a source text file, then attempts to
     * read a poem from said file, generate a poem model from its contents, and
     * then tokenise said model and update the GUI.
     */
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
            Alert alert = buildCleanAlert(AlertType.ERROR);
            alert.setHeaderText("Could not open file.");
            alert.setContentText(String.format("Failed to open %s.\n%s", poemFile.getName(), SEE_LOG));
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            alert.show();
        } catch (Exception e) {
            LOG.writeTempLog(e.toString() + "\n" + Arrays.toString(e.getStackTrace()));
            Alert alert = buildCleanAlert(AlertType.ERROR);
            alert.setHeaderText("An unexpected error occured.");
            alert.setContentText(SEE_LOG);
            alert.show();
        }
    }

    /**
     * Attempts to write the current poem to a text file.
     */
    public void savePoem() {
        if (poemFile == null) {
            savePoemAs();
            return;
        }

        try {
            poem.savePoem(poemFile);
        } catch (IOException e) {
            Alert alert = buildCleanAlert(AlertType.ERROR);
            alert.setHeaderText("Could not save poem.");
            alert.setContentText(String.format("Failed to write to %s.\n%s", poemFile.getName(), SEE_LOG));
            LOG.writeTempLog(e.toString() + "\n" + e.getStackTrace());
            alert.show();
        }
    }

    /**
     * Opens a dialogue for the user to choose the name of the text file to save the
     * poem under.
     */
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

    /**
     * Opens a dialogue for a user to choose a title name, and then switches the GUI
     * to direct edit mode.
     */
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
                Alert alert = buildCleanAlert(AlertType.ERROR);
                alert.setHeaderText("Could not create blank poem.");
                alert.setContentText(SEE_LOG);
                LOG.writeTempLog(e.toString() + "\n" + e.getStackTrace());
                alert.show();
            }
        });
    }

    // other

    /**
     * Swaps the left-hand pane between direct edit and token modes.
     */
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
                Alert alert = buildCleanAlert(AlertType.ERROR);
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

        if (focusedToken.getSuggestions() == null) {
            Label noSuggestions = new Label("Suggestions appear here.");
            noSuggestions.setSnapToPixel(snapToPixel);
            noSuggestions.setCache(cache);

            flwpnSuggestions.getChildren().add(noSuggestions);
        } else if (focusedToken.getSuggestions().isEmpty()) {
            Label noSuggestions = new Label("No results.");
            noSuggestions.setSnapToPixel(snapToPixel);
            noSuggestions.setCache(cache);

            flwpnSuggestions.getChildren().add(noSuggestions);
        } else {
            for (SuperWord suggestion : focusedToken.getSuggestions()) {
                Label label = new Label(suggestion.getPlaintext());
                label.getStyleClass().add(SUGGESTION_CLASS);
                label.setSnapToPixel(snapToPixel);
                label.setCache(cache);
                label.setOnMouseClicked(actionEvent -> makeSubstitution(suggestion));
                flwpnSuggestions.getChildren().add(label);
            }
        }
    }

}
