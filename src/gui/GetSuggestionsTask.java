package gui;

import java.util.ArrayList;

import javafx.concurrent.Task;
import utils.ParameterWrappers.FilterParameters;
import utils.ParameterWrappers.SuggestionPoolParameters;
import words.PartOfSpeech;
import words.SuperWord;

public class GetSuggestionsTask extends Task<ArrayList<SuperWord>> {

    private final SuperWord superWord;
    private final PartOfSpeech pos;
    private final SuggestionPoolParameters suggestionParams;
    private final FilterParameters filterParams;

    public GetSuggestionsTask(SuperWord superWord, PartOfSpeech pos, SuggestionPoolParameters suggestionParams,
            FilterParameters filterParams) {
        this.superWord = superWord;
        this.pos = pos;
        this.suggestionParams = suggestionParams;
        this.filterParams = filterParams;
    }

    @Override
    protected ArrayList<SuperWord> call() throws Exception {
        return superWord.getFilteredSuggestions(pos, suggestionParams, filterParams);
    }

}
