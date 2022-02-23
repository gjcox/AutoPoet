package gui;

import java.util.ArrayList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import utils.ParameterWrappers.SuggestionPoolParameters;
import words.SubWord.PartOfSpeech;
import words.SuperWord;
import words.Token;

public class IndexedTokenLabel extends Label {
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
    private SuggestionPoolParameters poolParams = new SuggestionPoolParameters();
    private boolean inclUnknown = true;
    private ArrayList<SuperWord> suggestions;
    private Controller controller;

    // getters
    public Token getToken() {
        return token;
    }

    public PartOfSpeech getPos() {
        return pos;
    }

    public SuggestionPoolParameters getPoolParams() {
        return poolParams;
    }

    public boolean getInclUnknown() {
        return inclUnknown;
    }

    public int getStanzaIndex() {
        return stanzaIndex;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    public ArrayList<SuperWord> getSuggestions() {
        return suggestions;
    }

    // setters
    public static void setJoinWordsAction(EventHandler<ActionEvent> function) {
        mnitmJoinWords.setOnAction(function);
    }

    public static void setSplitOnSpaceAction(EventHandler<ActionEvent> function) {
        mnitmSplitOnSpace.setOnAction(function);
    }

    public static void setSplitOnHyphenAction(EventHandler<ActionEvent> function) {
        mnitmSplitOnHyphen.setOnAction(function);
    }

    public void setTokenIndex(int newIndex) {
        this.tokenIndex = newIndex;
    }

    public void incrementTokenIndex(int increment) {
        this.tokenIndex += increment;
    }

    public void setPos(PartOfSpeech pos) {
        this.pos = pos;
    }

    public void setSuggestions(ArrayList<SuperWord> suggestions) {
        this.suggestions = suggestions;
    }

    // other

    /**
     * 
     * @param other a potential neighbouring word.
     * @return true if two words are adjacent within a line and there is a single
     *         whitespace character or hyphen between them, otherwise false.
     */
    public boolean isNeighbour(IndexedTokenLabel other) {
        if (other == null)
            return false;
        if (this.getParent().equals(other.getParent())
                && Math.abs(this.tokenIndex - other.tokenIndex) == 2) {

            IndexedTokenLabel middleToken = (IndexedTokenLabel) ((FlowPane) this.getParent()).getChildren()
                    .get(Math.min(this.tokenIndex, other.tokenIndex) + 1);
            return middleToken.token.getPlaintext().equals(" ")
                    || middleToken.token.getPlaintext().equals("-");
        } else {
            return false;
        }
    }

    private void populateContextMenu() {
        cntxtmnLabel.getItems().clear();
        if (controller.getSecondFocusedToken() != null) {
            cntxtmnLabel.getItems().add(mnitmJoinWords);
        } else {
            if (controller.getFocusedToken().token.getPlaintext().contains(" ")) {
                cntxtmnLabel.getItems().add(mnitmSplitOnSpace);
            }
            if (controller.getFocusedToken().token.getPlaintext().contains("-")) {
                cntxtmnLabel.getItems().add(mnitmSplitOnHyphen);
            }
        }
    }

    private void unhighlightWord(IndexedTokenLabel token) {
        if (token != null) {
            token.getStyleClass().remove(SELECTED_CLASS);
        }
    }

    private void highlightWord(IndexedTokenLabel token) {
        if (token != null) {
            token.getStyleClass().add(SELECTED_CLASS);
        }
    }

    private void selectNeighbour() {
        if (isNeighbour(controller.getFocusedToken())) {
            if (this.tokenIndex > controller.getFocusedToken().tokenIndex) {
                controller.setSecondFocusedToken(this);
            } else {
                controller.setSecondFocusedToken(controller.getFocusedToken());
                controller.setFocusedToken(this);
            }
            highlightWord(controller.getSecondFocusedToken());
            highlightWord(controller.getFocusedToken());
        } else if (isNeighbour(controller.getSecondFocusedToken())) {
            if (this.tokenIndex > controller.getSecondFocusedToken().tokenIndex) {
                controller.setFocusedToken(controller.getSecondFocusedToken());
                controller.setSecondFocusedToken(this);
            } else {
                controller.setFocusedToken(this);
            }
            highlightWord(controller.getSecondFocusedToken());
            highlightWord(controller.getFocusedToken());
        }
    }

    private void onClick(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            unhighlightWord(controller.getFocusedToken());
            unhighlightWord(controller.getSecondFocusedToken());

            if (event.isControlDown()) {
                selectNeighbour();
                populateContextMenu();
            } else {
                // deselect previous words and focus on clicked word
                controller.setSecondFocusedToken(null);

                controller.setFocusedToken(this);
                controller.focusOnStanza(this.stanzaIndex);
                highlightWord(controller.getFocusedToken());
                controller.focusOnWord(controller.getFocusedToken());
                populateContextMenu();
            }
        }
    }

    public IndexedTokenLabel(Controller parentController, Token token, int stanzaIndex, int lineIndex,
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
            this.setOnMouseClicked(this::onClick);
        }
    }
}
