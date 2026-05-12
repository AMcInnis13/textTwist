package cs120.lab06;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * JUnit 4.12 tests for TwistController — scoring, duplicate detection, and shuffleLetters.
 *
 * Tests use controller.getTargetWord() rather than a hardcoded word because beginEpisode(3)
 * selects a random target from the 3-letter dictionary. The target word is always valid, so
 * checkGuessWord(getTargetWord()) must return targetWord.length()^2 * 10.
 */
public class TwistControllerTest {

    private TwistController controller;

    @Before
    public void setUp() {
        // Surface missing-file errors clearly (CLAUDE.md Pitfall 14)
        assertTrue(
            "Dictionary file not found — run tests from project root. Expected: "
                + new File("twister_words/3.txt").getAbsolutePath(),
            new File("twister_words/3.txt").exists()
        );

        controller = new TwistController("twister_words");
        controller.beginEpisode(3);
    }

    /**
     * checkGuessWord on the target word must return n*n*10 where n = word length.
     * Using getTargetWord() avoids hardcoding a specific dictionary entry.
     */
    @Test
    public void testCheckGuessWord_validWord() {
        String target = controller.getTargetWord();
        int expected = target.length() * target.length() * 10;
        assertEquals("checkGuessWord(targetWord) should return n*n*10",
            expected, controller.checkGuessWord(target));
    }

    /**
     * Invalid, null, and empty words must all return 0.
     */
    @Test
    public void testCheckGuessWord_invalidWord() {
        assertEquals("Non-existent word should return 0",
            0, controller.checkGuessWord("ZZZZZZZZZ"));
        assertEquals("Empty string should return 0",
            0, controller.checkGuessWord(""));
        assertEquals("null should return 0",
            0, controller.checkGuessWord(null));
    }

    /**
     * Guessing the same word twice: first call returns > 0, second call returns 0.
     * Score must not increase after the duplicate guess.
     */
    @Test
    public void testCheckGuessWord_duplicateRejected() {
        String target = controller.getTargetWord();

        int firstResult = controller.checkGuessWord(target);
        assertTrue("First guess of target word must earn points", firstResult > 0);

        int scoreAfterFirst = controller.getScore();

        int secondResult = controller.checkGuessWord(target);
        assertEquals("Duplicate guess must return 0", 0, secondResult);
        assertEquals("Score must not increase after duplicate", scoreAfterFirst, controller.getScore());
    }

    /**
     * shuffleLetters() must return a list of the same length as targetWord and contain
     * exactly the same multiset of characters (sorted comparison).
     */
    @Test
    public void testShuffleLetters_sameLetters() {
        String target = controller.getTargetWord();
        List<String> shuffled = controller.shuffleLetters();

        assertNotNull("shuffleLetters() must not return null", shuffled);
        assertEquals("Shuffled list length must match targetWord length",
            target.length(), shuffled.size());

        // Build sorted char list from target
        List<String> fromTarget = new ArrayList<>();
        for (char c : target.toCharArray()) {
            fromTarget.add(String.valueOf(c));
        }
        Collections.sort(fromTarget);

        // Sort the shuffled list for multiset comparison
        List<String> sortedShuffled = new ArrayList<>(shuffled);
        Collections.sort(sortedShuffled);

        assertEquals("Shuffled list must contain same multiset of characters as targetWord",
            fromTarget, sortedShuffled);
    }

    /**
     * Score starts at 0; guessing the target word accumulates correctly in both score
     * and levelScore.
     */
    @Test
    public void testScoreAccumulation() {
        assertEquals("Initial score must be 0", 0, controller.getScore());
        assertEquals("Initial levelScore must be 0", 0, controller.getLevelScore());

        String target = controller.getTargetWord();
        int pts = controller.checkGuessWord(target);

        assertTrue("Points for target word must be positive", pts > 0);
        assertEquals("getScore() must equal points earned", pts, controller.getScore());
        assertEquals("getLevelScore() must equal points earned", pts, controller.getLevelScore());
    }
}
