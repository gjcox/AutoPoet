# Auto-Poet 
This was my senior honours project in my BSc. It is a poetry writing assistant built on top of a basic text editor both implemented in Java with a JavaFX GUI. It uses [WordsAPI](https://www.wordsapi.com/) to find IPA strings for the words in the poem and then uses the IPA to detect the existing rhyming scheme. The user can specify a desired rhyming scheme and then Auto-Poet can suggest words that would fit that scheme whilst trying to preserve the original meaning of the poem. Various types of rhyme are supported. 

## To Do list (unordered): 
<ul>
    <li>Improve the GUI
        <ul>
            <li><s>Add "rhyme with scheme"</s></li>
            <li>Add "I'm feeling lucky"</li>
            <li>Reset stanza info/focused token on poem refresh</li>
            <li>Set text field action when focus lost</li>
        </ul>
    </li>
    <li>Recognise more poetic devices</li>
        <ul>
            <li><s>General rhymes</s></li>
            <li>Alliteration</li>
        </ul>
    <li>Handle compound words better</li>
        <ul>
            <li><s>Add emphasis to monosyllabic words</s></li>
        </ul>
    </li>
    <li>Add special case handling for pronunciation of words ending in s</li>
</ul>

Aspirational/low priority list: 
<ul>
    <li><s>Improve suggestion filtering to do it in batches, so that the least likely suggestions don't waste user time</s></li>
    <li>Handle conjugated verbs</li>
    <li>Add dictionary tab to GUI</li>
</ul>

## Compilation 
I used VS Code for development, but the following compilation instructions have been tested on Linux: 

Navigate to CS4099/ <br>
Run the command $ javac -cp lib/json-20210307.jar:lib/junit-platform-console.standalone-1.8.1.jar  src/testing/Demos.java <br>

## Running the program 
For terminal demos, the command $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ swc | swp | wc | rhyme \] <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ swc | swp | wc \] *some_word*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos rhyme *word1* *word2*  <br> 
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ synonyms | typeOf | hasTypes | commonlyTyped | inCategory | hasCategories | commonCategories | partOf | hasParts | similarTo \] *word* *part_of_speech_(PoS)*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos rhyme *word1* *PoS1* *word2* *PoS2*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos suggestions *word* *PoS1* *rhyme_with* *PoS2*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos poem *poem_file*  <br>

<p>The recognised parts of speech are: 'noun', 'pronoun', 'verb', 'adjective', 'adverb', 'preposition', 'conjunction', 'definite article', and 'unknown'. The lattermost is to handle WordsAPI being unreliable.</p>
<p>N.B. words that WordsAPI doesn't recognise will still be marked as populated for swp, despite nothing of value being added to them. This is to prevent repeated failed attempts at querying, but might change in future. </p>
<p>N.B. the suggestions command can take several minutes. </p></s>

For the GUI, use the runAutoPoet.sh script from within src/. 

## Logs 
lib/logs/log.log is temporary, and reflects the last run of the program (including when SuperWords were retrieved from a cache) so it's in the .gitignore.  
lib/logs/persistent.log is not temporary, and is for automatically recording unexpected behaviour from WordsAPI (of a type that I have anticipated), such as missing data fields, unexpected data fields and inconsistent plural recognition, so it is not included in the .gitignore. 

e.g. It revealed that "the" has partOfSpeech "definite article", which I wasn't previously aware was an option with the API (see WordsApiNotes.txt for the parts of speech I was expecting, as well as other fun/infuriating examples of reponses). 

Please let me know if unexpected behaviour occurs! 
