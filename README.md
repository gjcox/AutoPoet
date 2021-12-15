# Hello 
To Do list (unordered): 
<ul>
            <li>Parse poems from text files</li>          
            <li>Make a GUI</li>
            <li>Recognise more poetic devices</li>
            <li>Improve suggestion filtering to do it in batches, so that the least likely suggestions don't waste user time</li>
</ul>

# Compilation 
I continue to use VS Code, but the following compilation instructions have been tested on Linux: 

Navigate to CS4099/ <br>
Run the command $ javac -cp lib/json-20210307.jar:lib/junit-platform-console.standalone-1.8.1.jar  src/\*/\*.java <br>
Run the command $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ swc | swp | wc | rhyme \] <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ swc | swp | wc \] *some_word*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos rhyme *word1* *word2*  <br> 
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos \[ synonyms | typeOf | hasTypes | commonlyTyped | inCategory | hasCategories | commonCategories | partOf | hasParts | similarTo \] *word* *part_of_speech (PoS)*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos rhyme *word1* *PoS1* *word2* *Pos2*  <br>
            OR  $ java -cp lib/json-20210307.jar:src/ testing.Demos suggestions *word* *PoS1* *rhyme_with* *Pos2*  <br>

<p>The recognised parts of speech are: 'noun', 'pronoun', 'verb', 'adjective', 'adverb', 'preposition', 'conjunction', 'definite article', and 'unknown'. The lattermost is to handle WordsAPI being unreliable.</p>
<p>N.B. words that WordsAPI doesn't recognise will still be marked as populated for swp, despite nothing of value being added to them. This is to prevent repeated failed attempts at querying, but might change in future. </p>
<p>N.B. the suggestions command can take several minutes. </p>

# Logs 
lib/logs/log.log is temporary, and reflects the last run of the program (including when SuperWords were retrieved from a cache) so it's in the .gitignore.  
lib/logs/persistent.log is not temporary, and is for automatically recording unexpected behaviour from WordsAPI (of a type that I have anticipated), such as missing data fields, unexpected data fields and inconsistent plural recognition, so it is not included in the .gitignore. 

e.g. It revealed that "the" has partOfSpeech "definite article", which I wasn't previously aware was an option with the API (see WordsApiNotes.txt for the parts of speech I was expecting, as well as other fun/infuriating examples of reponses). 

Please let me know if unexpected behaviour occurs! 
