## Hello 
This is very much a WIP, especially since I recently began the process of doing more or less everything again from scratch.
It's currently exam season, so most of my time is going to revision, but I work on this as a break here and there. 

I'm yet to reimplement most of the IPA handling - I've got all new data structures to account for, but the logic is unchanged so it shouldn't take too long now that the basic implementations of SuperWord and Word are done.

Most of the getX() functions in apis.WordsAPI are now redundant, and will be deleted soon. 

For some reason the IPA characters are being a PIA and displaying as "?" in outputs, where they previously didn't. I know the internal representations are still working because splitting IPA into syllables still works so I really don't know what the issue is. 

# Compilation 
I continue to use VS Code, but the following compilation instructions *should* work on Linux: 

Navigate to CS4099/ 
Run the command $ javac src/*/*.java
Run the command $ java -cp src/ testing.Demos [ swc | swp | wc ]  
            OR  $ java -cp src/ testing.Demos [ swc | swp | wc ] *some_word*   

testing.Demos.superWordConstructor() and .superWordPopulator() (i.e. swc and swp) demonstrate the functionality of SuperWord, the latter of which involves WordsAPI. 
testing.Demos.wordConstructor() (i.e. wc) shows the subwords in full, i.e. the functionality of Word. 
Omitting the second argument will use predefined examples; including it will attempt to create a SuperWord from the passed argument. 

N.B. words that WordsAPI doesn't recognise will still be marked as populated for swp, despite nothing of value being added to them. This is to prevent repeated failed attempts at querying, but might change in future. 

# Logs 
lib/logs/log.log is temporary, and reflects the last run of the program (including when SuperWords were retrieved from a cache) so it's in the .gitignore.  
lib/logs/persistent.log is not temporary, and is for automatically recording unexpected behaviour from WordsAPI (of a type that I have anticipated), such as missing data fields and inconsistent plural recognition, so it is not included in the .gitignore. 

e.g. It revealed that "the" has partOfSpeech "definite article", which I wasn't previously aware was an option with the API (see WordsApiNotes.txt for the parts of speech I was expecting, as well as other fun/infuriating examples of reponses). 

Please let me know if unexpected behvaiour occurs! 
