package cs120.lab06;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JavaFX controller for the TextTwist game screen.
 * Wired to primary.fxml via FXMLLoader.
 * Phase 2: @FXML field declarations and ObservableList binding only.
 * Phase 3 adds event handlers, Timeline timer, and game model wiring.
 */
public class PrimaryController {

    // --- FXML-injected nodes (fx:id must match field name exactly) ---

    @FXML private ListView<String> foundWordsList;
    @FXML private FlowPane letterButtonArea;
    @FXML private HBox guessDisplay;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label timeLabel;
    @FXML private Button twistBtn;
    @FXML private Button enterBtn;
    @FXML private Button lastWordBtn;
    @FXML private Button clearBtn;

    // --- Non-FXML fields ---

    private ObservableList<String> foundWords;

    private TwistController twistController;
    private Timeline gameTimeline;
    private List<Button> pressedButtons = new ArrayList<>();
    private List<Button> lastWordButtons = new ArrayList<>();
    private int secondsLeft;
    private boolean playing;

    // --- Lifecycle ---

    @FXML
    public void initialize() {
        foundWords = FXCollections.observableArrayList();
        foundWordsList.setItems(foundWords);
        startGame();
    }

    // --- Game setup ---

    private void startGame() {
        twistController = new TwistController("twister_words");
        twistController.beginEpisode(3);
        playing = true;
        secondsLeft = 120;
        timeLabel.setText("2:00");
        scoreLabel.setText("Score: 0");
        levelLabel.setText("Level: 1");

        clearBtn.setOnAction(e -> {
            if (!playing) return;
            for (Node node : letterButtonArea.getChildren()) {
                ((Button) node).setDisable(false);
            }
            pressedButtons.clear();
            guessDisplay.getChildren().clear();
        });

        twistBtn.setOnAction(e -> {
            if (!playing) return;
            pressedButtons.clear();
            guessDisplay.getChildren().clear();
            letterButtonArea.getChildren().clear();
            buildLetterButtons();
            lastWordBtn.setDisable(true);
        });

        enterBtn.setOnAction(e -> {
            if (!playing) return;
            if (pressedButtons.isEmpty()) return;
            String guessWord = pressedButtons.stream()
                    .map(Button::getText)
                    .collect(Collectors.joining());
            int points = twistController.checkGuessWord(guessWord);
            if (points > 0) {
                foundWords.add(guessWord);
                scoreLabel.setText("Score: " + twistController.getScore());
                lastWordButtons = new ArrayList<>(pressedButtons);
                lastWordBtn.setDisable(false);
                if (guessWord.equalsIgnoreCase(twistController.getTargetWord())) {
                    endEpisode();
                    return;
                }
                guessDisplay.getStyleClass().add("guess-valid");
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e2 -> {
                    guessDisplay.getStyleClass().remove("guess-valid");
                    pressedButtons.clear();
                    guessDisplay.getChildren().clear();
                    letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false));
                });
                pause.play();
            } else {
                guessDisplay.getStyleClass().add("guess-invalid");
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e2 -> {
                    guessDisplay.getStyleClass().remove("guess-invalid");
                    pressedButtons.clear();
                    guessDisplay.getChildren().clear();
                    letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false));
                });
                pause.play();
            }
        });

        lastWordBtn.setOnAction(e -> {
            pressedButtons = new ArrayList<>(lastWordButtons);
            for (Button btn : pressedButtons) {
                btn.setDisable(true);
            }
            guessDisplay.getChildren().clear();
            for (Button btn : pressedButtons) {
                Label tile = new Label(btn.getText());
                tile.getStyleClass().add("letter-slot");
                guessDisplay.getChildren().add(tile);
            }
        });

        buildLetterButtons();

        gameTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()));
        gameTimeline.setCycleCount(Timeline.INDEFINITE);
        gameTimeline.play();
    }

    private void buildLetterButtons() {
        letterButtonArea.getChildren().clear();
        List<String> letters = twistController.shuffleLetters();
        for (String letter : letters) {
            Button btn = new Button(letter);
            btn.getStyleClass().add("letter-btn");
            btn.setOnAction(e -> handleLetterButton(btn));
            letterButtonArea.getChildren().add(btn);
        }
    }

    // --- Event handlers ---

    private void handleLetterButton(Button btn) {
        if (!playing) return;
        btn.setDisable(true);
        pressedButtons.add(btn);
        Label tile = new Label(btn.getText());
        tile.getStyleClass().add("letter-slot");
        guessDisplay.getChildren().add(tile);
    }

    // --- Timer ---

    private void onTick() {
        secondsLeft--;
        timeLabel.setText(String.format("%d:%02d", secondsLeft / 60, secondsLeft % 60));
        timeLabel.getStyleClass().removeAll("timer-warning", "timer-critical");
        if (secondsLeft <= 10) {
            timeLabel.getStyleClass().add("timer-critical");
        } else if (secondsLeft <= 20) {
            timeLabel.getStyleClass().add("timer-warning");
        }
        if (secondsLeft <= 0) {
            endEpisode();
        }
    }

    // --- Episode end ---

    private void endEpisode() {
        playing = false;
        gameTimeline.stop();
        twistBtn.setDisable(true);
        enterBtn.setDisable(true);
        lastWordBtn.setDisable(true);
        clearBtn.setDisable(true);
        letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(true));
    }
}
