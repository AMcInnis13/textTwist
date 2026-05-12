package cs120.lab06;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

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

    // --- Lifecycle ---

    @FXML
    public void initialize() {
        foundWords = FXCollections.observableArrayList();
        foundWordsList.setItems(foundWords);
    }
}
