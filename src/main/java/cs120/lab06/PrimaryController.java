package cs120.lab06;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<String> lastWordLetters = new ArrayList<>();
    private int letterCount = 3;
    private boolean targetWordGuessed = false;
    private int secondsLeft;
    private boolean playing;

    // --- Lifecycle ---

    @FXML
    public void initialize() {
        foundWords = FXCollections.observableArrayList();
        foundWordsList.setItems(foundWords);

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
                lastWordLetters = pressedButtons.stream().map(Button::getText).collect(Collectors.toList());
                lastWordBtn.setDisable(false);
                if (guessWord.equalsIgnoreCase(twistController.getTargetWord())) {
                    targetWordGuessed = true;
                    endEpisode();
                    return;
                }
                guessDisplay.getStyleClass().add("guess-valid");
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e2 -> {
                    guessDisplay.getStyleClass().remove("guess-valid");
                    pressedButtons.clear();
                    guessDisplay.getChildren().clear();
                    if (playing) { letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false)); }
                });
                pause.play();
            } else {
                guessDisplay.getStyleClass().add("guess-invalid");
                PauseTransition pause = new PauseTransition(Duration.millis(500));
                pause.setOnFinished(e2 -> {
                    guessDisplay.getStyleClass().remove("guess-invalid");
                    pressedButtons.clear();
                    guessDisplay.getChildren().clear();
                    if (playing) { letterButtonArea.getChildren().forEach(n -> ((Button) n).setDisable(false)); }
                });
                pause.play();
            }
        });

        lastWordBtn.setOnAction(e -> {
            guessDisplay.getChildren().clear();
            pressedButtons.clear();
            List<Node> available = new ArrayList<>(letterButtonArea.getChildren());
            for (String letter : lastWordLetters) {
                for (Node node : available) {
                    Button btn = (Button) node;
                    if (!btn.isDisabled() && btn.getText().equals(letter)) {
                        handleLetterButton(btn);
                        available.remove(btn);
                        break;
                    }
                }
            }
        });

        startGame();
    }

    // --- Game setup ---

    private void startGame() {
        if (gameTimeline != null) gameTimeline.stop();
        twistController = new TwistController("twister_words");
        scoreLabel.setText("Score: 0");
        beginNextEpisode(3);
    }

    private void beginNextEpisode(int newLetterCount) {
        if (gameTimeline != null) gameTimeline.stop();
        letterCount = newLetterCount;
        twistController.beginEpisode(letterCount);
        playing = true;
        targetWordGuessed = false;
        secondsLeft = 120;
        pressedButtons.clear();
        lastWordLetters.clear();
        foundWords.clear();
        guessDisplay.getChildren().clear();
        letterButtonArea.getChildren().clear();
        lastWordBtn.setDisable(true);
        twistBtn.setDisable(false);
        enterBtn.setDisable(false);
        clearBtn.setDisable(false);
        timeLabel.setText("2:00");
        timeLabel.getStyleClass().removeAll("timer-warning", "timer-critical");
        levelLabel.setText("Level: " + (letterCount - 2));
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

        int threshold = (int)(0.25 * letterCount * letterCount * 10);
        boolean thresholdMet = twistController.getLevelScore() >= threshold;

        javafx.stage.Window owner = timeLabel.getScene().getWindow();
        if (letterCount == 10 && (targetWordGuessed || thresholdMet)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(owner);
            alert.setTitle("TextTwist");
            alert.setHeaderText(null);
            alert.setContentText("You Win!");
            alert.showAndWait();
            startGame();
        } else if (thresholdMet) {
            letterCount++;
            int newLevel = letterCount - 2;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(owner);
            alert.setTitle("TextTwist");
            alert.setHeaderText(null);
            alert.setContentText("Advanced to Level " + newLevel + "!");
            alert.showAndWait();
            beginNextEpisode(letterCount);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(owner);
            alert.setTitle("TextTwist");
            alert.setHeaderText(null);
            alert.setContentText("Game Over — not enough points.");
            alert.showAndWait();
            startGame();
        }
    }
}
