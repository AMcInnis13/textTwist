package cs120.lab06;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4.12 tests for WordDictionary.
 *
 * Tests read from the actual twister_words/ files. Must be run from the project
 * root so that relative paths resolve correctly (see PITFALLS.md #14).
 */
public class WordDictionaryTest {

    private WordDictionary dict;

    /**
     * Creates a WordDictionary loaded with 3-letter words before each test.
     * Asserts the dictionary file exists — fails with an actionable message if
     * the test runner is not invoked from the project root (PITFALLS.md #14).
     */
    @Before
    public void setUp() {
        File dictFile = new File("twister_words/3.txt");
        assertTrue(
            "Dictionary file not found at: " + dictFile.getAbsolutePath()
                + " — run mvn test from the project root",
            dictFile.exists()
        );
        dict = new WordDictionary("twister_words");
        dict.initialize(3);
    }

    /**
     * DICT-07: dictionary file is accessible from the test working directory.
     */
    @Test
    public void testFileExists() {
        assertTrue(
            "twister_words/3.txt must exist at the project root",
            new File("twister_words/3.txt").exists()
        );
    }

    /**
     * DICT-05: isValidWord returns true for a known word in the dictionary.
     * Also verifies D-06: lowercase input is normalized to uppercase.
     *
     * CAT is confirmed present in twister_words/3.txt.
     */
    @Test
    public void testIsValidWord_knownWord() {
        assertTrue("CAT (uppercase) should be a valid 3-letter word", dict.isValidWord("CAT"));
        assertTrue("cat (lowercase) should also be valid — input normalized to uppercase",
                   dict.isValidWord("cat"));
    }

    /**
     * DICT-06: isValidWord returns false for strings that are not in the dictionary.
     */
    @Test
    public void testIsValidWord_invalidWord() {
        assertFalse("ZZZZZZZZZ should not be a real word", dict.isValidWord("ZZZZZZZZZ"));
        assertFalse("Empty string should return false", dict.isValidWord(""));
    }

    /**
     * DICT-03 / DICT-04: randomWord returns a non-null word of the expected length.
     * Runs 10 times to increase confidence.
     */
    @Test
    public void testRandomWord_returnsCorrectLength() {
        for (int i = 0; i < 10; i++) {
            String word = dict.randomWord(3);
            assertNotNull("randomWord(3) must not return null", word);
            assertEquals(
                "randomWord(3) should return a 3-letter word, got: " + word,
                3, word.length()
            );
        }
    }

    /**
     * PITFALLS.md #15 mitigation: randomWord returns at least 5 distinct words
     * when called 100 times, proving that Random selection is working.
     */
    @Test
    public void testRandomWord_returnsDistinctWords() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            seen.add(dict.randomWord(3));
        }
        assertTrue(
            "randomWord(3) called 100 times should return at least 5 distinct words; got: "
                + seen.size(),
            seen.size() >= 5
        );
    }
}
