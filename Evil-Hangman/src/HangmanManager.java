/*  Student information for assignment:

 *
 *  On my honor, <NAME>, this programming assignment is my own work
 *  and I have not provided this code to any other student.
 *
 *  Name: Ayush Patel
 *  email address: patayush01@utexas.edu
 *  UTEID: ap55837
 *  Section 5 digit ID: 50865
 *  Grader name: Tony 
 *  Number of slip days used on this assignment: 0
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Manages the details of EvilHangman. This class keeps tracks of the possible
 * words from a dictionary during rounds of hangman, based on guesses so far.
 *
 */
public class HangmanManager {
	private Set<String> words;
	private Set<String> activeList;
	private final boolean debugOn;
	// making guesses an arrayList to easily store guesses and calculate guesses
	// left
	private ArrayList<Character> guesses;
	private int numGuesses;
	private HangmanDifficulty diff;
	private String pattern;
	private final static String EMPTY_SPOT = "-";

	/**
	 * Create a new HangmanManager from the provided set of words and phrases.
	 * pre: words != null, words.size() > 0
	 * 
	 * @param words   A set with the words for this instance of Hangman.
	 * @param debugOn true if we should print out debugging to System.out.
	 */
	public HangmanManager(Set<String> words, boolean debugOn) {
		if (words == null || words.size() <= 0)
			throw new IllegalArgumentException(
					"set of words cannot be null or empty");
		this.words = new HashSet<>();
		this.debugOn = debugOn;
		// make deep copy of set
		for (String word : words)
			this.words.add(word);

	}

	/**
	 * Create a new HangmanManager from the provided set of words and phrases.
	 * Debugging is off. pre: words != null, words.size() > 0
	 * 
	 * @param words A set with the words for this instance of Hangman.
	 */
	public HangmanManager(Set<String> words) {
		// calls previous constructor with passing in false for debug
		this(words, false);

	}

	/**
	 * Get the number of words in this HangmanManager of the given length. pre:
	 * none
	 * 
	 * @param length The given length to check.
	 * @return the number of words in the original Dictionary with the given
	 *         length
	 */
	public int numWords(int length) {
		int countWords = 0;
		// for each word in word set (dictionary), if the word has same length
		// as length, add to count
		for (String word : words)
			if (word.length() == length)
				countWords++;
		return countWords;
	}

	/**
	 * Get for a new round of Hangman. Think of a round as a complete game of
	 * Hangman.
	 * 
	 * @param wordLen    the length of the word to pick this time.
	 *                   numWords(wordLen) > 0
	 * @param numGuesses the number of wrong guesses before the player loses the
	 *                   round. numGuesses >= 1
	 * @param diff       The difficulty for this round.
	 */
	public void prepForRound(int wordLen, int numGuesses,
			HangmanDifficulty diff) {
		if (numWords(wordLen) <= 0 || numGuesses < 1)
			throw new IllegalArgumentException(
					"word length has to be > 0, number of guesses has to be >= 1, and difficulty cannot be null");
		this.numGuesses = numGuesses;
		this.diff = diff;
		guesses = new ArrayList<>();
		activeList = new HashSet<>();
		// finding each word of wordLen length and storing it in activeList
		for (String word : words)
			if (word.length() == wordLen)
				activeList.add(word);
		StringBuilder pattern = new StringBuilder();
		// setting pattern to a string with wordLen amount of blank spaces
		for (int i = 0; i < wordLen; i++)
			pattern.append(EMPTY_SPOT);
		this.pattern = pattern.toString();

	}

	/**
	 * The number of words still possible (live) based on the guesses so far.
	 * Guesses will eliminate possible words.
	 * 
	 * @return the number of words that are still possibilities based on the
	 *         original dictionary and the guesses so far.
	 */
	public int numWordsCurrent() {
		return activeList.size();
	}

	/**
	 * Get the number of wrong guesses the user has left in this round (game) of
	 * Hangman.
	 * 
	 * @return the number of wrong guesses the user has left in this round
	 *         (game) of Hangman.
	 */
	public int getGuessesLeft() {
		return numGuesses - guesses.size();
	}

	/**
	 * Return a String that contains the letters the user has guessed so far
	 * during this round. The characters in the String are in alphabetical
	 * order. The String is in the form [let1, let2, let3, ... letN]. For
	 * example [a, c, e, s, t, z]
	 * 
	 * @return a String that contains the letters the user has guessed so far
	 *         during this round.
	 */
	public String getGuessesMade() {
		Collections.sort(guesses);
		return guesses.toString();
	}

	/**
	 * Check the status of a character.
	 * 
	 * @param guess The characater to check.
	 * @return true if guess has been used or guessed this round of Hangman,
	 *         false otherwise.
	 */
	public boolean alreadyGuessed(char guess) {
		return guesses.contains(guess);
	}

	/**
	 * Get the current pattern. The pattern contains '-''s for unrevealed (or
	 * guessed) characters and the actual character for "correctly guessed"
	 * characters.
	 * 
	 * @return the current pattern.
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Update the game status (pattern, wrong guesses, word storeKeys), based on
	 * the give guess.
	 * 
	 * @param guess pre: !alreadyGuessed(ch), the current guessed character
	 * @return return a tree map with the resulting patterns and the number of
	 *         words in each of the new patterns. The return value is for
	 *         testing and debugging purposes.
	 */
	public TreeMap<String, Integer> makeGuess(char guess) {
		if (alreadyGuessed(guess))
			throw new IllegalStateException("that guess has already been made");
		// TreeMap that is returned, key is pattern, integer is quantity of
		// words that can fit that pattern
		guesses.add(guess);
		TreeMap<String, Integer> patternQ = new TreeMap<>();
		// stores return of putWordsToPattern, which matches word to its pattern
		Map<String, ArrayList<String>> wordsForPattern = putWordsToPattern(
				guess);
		// stores FamilyPattern objects, important for sorting and returning the
		// appropriate one based on difficulty
		ArrayList<FamilyPattern> familypatterns = new ArrayList<>();
		for (String key : wordsForPattern.keySet()) {
			// create new FamilyPattern object from that pattern and array and
			// add it to arrayList storing FamilyPatterns
			FamilyPattern fam = new FamilyPattern(key,
					wordsForPattern.get(key));
			familypatterns.add(fam);
			// add pattern and quantity of words that can fit this pattern
			// to debugging TreeMap
			patternQ.put(fam.getPattern(), fam.getSize());

		}
		// utilizes compareTo in FamilyPattern class to sort FamilyPattern
		// objects in proper order
		Collections.sort(familypatterns);
		// sets pattern and activeList to new values
		setPatternAndActiveList(familypatterns);
		// if pattern has guess, then dont deduct from guessesleft (hence add to
		// numGuesses)
		if (pattern.contains(Character.toString(guess)))
			numGuesses++;
		return patternQ;

	}

	// helper method that associates each word to pattern it fits
	// pre : none
	// returns a Map<String, ArrayList<String>> of patterns (key) and arrayList
	// of words (value)
	private Map<String, ArrayList<String>> putWordsToPattern(char guess) {
		// key is pattern, value is arraylist of words that fit that pattern
		Map<String, ArrayList<String>> wordsToPattern = new HashMap<>();
		// for each word in activeList..
		for (String word : activeList) {
			// returns word mutated into pattern
			String possPattern = reformatToPattern(word, guess);
			// if wordsToPattern does not have the valid pattern stored, then
			// put that pattern in map alongside new ArrayList
			if (!wordsToPattern.containsKey(possPattern))
				wordsToPattern.put(possPattern, new ArrayList<String>());
			// add word that fits certain pattern to the arraylist of words for
			// that pattern
			wordsToPattern.get(possPattern).add(word);

		}
		return wordsToPattern;
	}

	// helper method to reformat word to pattern
	// pre: none
	// returns string, which is word reformatted to pattern
	private String reformatToPattern(String word, char guess) {
		for (int i = 0; i < word.length(); i++) {
			// if the character at certain index is not the same for both word
			// and pattern..
			if (word.charAt(i) != pattern.charAt(i)) {
				// and if certain index of word does not equal most recent
				// guess, change that index of word to empty spot (that index
				// has a letter that has not been guessed yet, so in pattern
				// this is an empty spot)
				if (word.charAt(i) != guess)
					word = word.replace(word.substring(i, i + 1), EMPTY_SPOT);
			}
			// implicit else, then pattern and word have same character at that
			// index so keep that index of word the same
		}

		return word;
	}

	// helper method to set pattern and activeList to new values
	// pre: none
	// return: none
	private void setPatternAndActiveList(
			ArrayList<FamilyPattern> familypatterns) {
		// difficulty int, used to determine on what guess should game choose
		// second hardest over hardest answer
		final int DIFF_INT = 4;
		int indexGetFrom = 0;
		// if there is more than valid pattern left...
		if (familypatterns.size() > 1) {
			// if difficulty is easy and then there has been an even number of
			// guesses (every other guess should be second hardest)
			if (diff == HangmanDifficulty.EASY
					&& (guesses.size()) % (DIFF_INT / 2) == 0) {
				// set index to second hardest index
				indexGetFrom = 1;
				// if difficulty is medium and its a 4th guess (every 4th guess
				// should be second hardest)
			} else if (diff == HangmanDifficulty.MEDIUM
					&& (guesses.size()) % DIFF_INT == 0) {
				// set index to second hardest index
				indexGetFrom = 1;
			}
		}
		// set pattern to new pattern (determined by index)
		pattern = familypatterns.get(indexGetFrom).getPattern();
		// set activeList to new HashSet of words from that pattern
		activeList = new HashSet<String>(
				familypatterns.get(indexGetFrom).getWords());
	}

	/**
	 * Return the secret word this HangmanManager finally ended up picking for
	 * this round. If there are multiple possible words left one is selected at
	 * random. <br>
	 * pre: numWordsCurrent() > 0
	 * 
	 * @return return the secret word the manager picked.
	 */

	public String getSecretWord() {
		if (numWordsCurrent() <= 0)
			throw new IllegalArgumentException(
					"There are no possible words left");
		// random int used to get random secret word based on number of words
		// left
		int run = (int) (Math.random() * numWordsCurrent());
		Iterator<String> activeListIter = activeList.iterator();
		// output set to first value in iterator
		String output = activeListIter.next();
		// starts at 1 because output is set to first value in iterator,
		// traverse till get to random generated index
		for (int i = 1; i < run; i++)
			output = activeListIter.next();
		return output;
	}

	// based on code Claire provided on Piazza @503
	private void debug(String... strings) {
		if (debugOn && strings.length > 0) {
			System.out.print("DEBUGGING: ");
			for (String string : strings) {
				System.out.println(string);
			}
			if (strings.length != 1)
				System.out.println("END DEBUGGING");
		}
	}

	private class FamilyPattern implements Comparable<FamilyPattern> {
		private final String pattern;
		private final ArrayList<String> words;
		// storing blankSpace as instance variable so do not have to recalculate
		// blank space in every iteration of compareTo
		private final int blankSpace;

		private FamilyPattern(String pattern, ArrayList<String> words) {
			this.pattern = pattern;
			this.words = words;
			// calculates blankSpace as stores it in instant variable
			this.blankSpace = calculateBlankSpace();

		}

		// getter method, return words;
		private ArrayList<String> getWords() {
			return words;
		}

		// getter method, return words.size();
		private int getSize() {
			return words.size();
		}

		// getter method, return pattern;
		private String getPattern() {
			return pattern;
		}

		// getter method, return blankSpace
		private int getBlankSpace() {
			return blankSpace;
		}

		// calculates the number of blank spaces in pattern
		private int calculateBlankSpace() {
			int countBlankSpace = 0;
			// traverses through each index of pattern, if it equals EMPTY_SPOT,
			// add to counter,
			for (int i = 0; i < getPattern().length(); i++)
				if (getPattern().substring(i, i + 1).equals(EMPTY_SPOT))
					countBlankSpace++;
			return countBlankSpace;
		}

		@Override
		// pre: none
		// return: and an int, if <0 then this FamilyPattern > o, if > 0 then o
		// > this FamilyPattern
		public int compareTo(FamilyPattern o) {
			// its always parameter compared to this in order to sort in
			// descending order
			// ex) o.getSize() - getSize(),
			// o.getPattern().compareTo(getPattern()) etc

			// compare amount of words for each pattern
			int compare = o.getSize() - getSize();
			// if they do not have equal amount of words, return compare
			if (compare != 0)
				return compare;
			// if the have equal amount of words, compare amount of blank spaces
			// in each pattern
			compare = o.getBlankSpace() - getBlankSpace();
			// if they do not have equal amount of blanks, return compare
			if (compare != 0)
				return compare;
			// if they do have equal amount of blank spaces, return patterns
			// lexicographically compared
			return o.getPattern().compareTo(getPattern());

		}
	}
}
