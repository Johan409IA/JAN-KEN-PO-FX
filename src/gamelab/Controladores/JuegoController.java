package gamelab.Controladores;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class JuegoController implements Initializable {

    // --- Componentes FXML ---
    @FXML private HBox playerScoreBox;
    @FXML private HBox botScoreBox;
    @FXML private Label timerLabel;
    @FXML private GridPane gameGrid;
    @FXML private Label statusLabel;
    
    // --- Lógica del Juego ---
    private Button[][] buttons = new Button[3][3];
    private String[][] board = new String[3][3];
    private final String PLAYER_SYMBOL = "X";
    private final String BOT_SYMBOL = "O";
    private boolean playerTurn = true;
    private boolean gameActive = true;
    private int playerWins = 0;
    private int botWins = 0;
    private final int ROUNDS_TO_WIN = 3;
    
    // --- Lógica del Cronómetro ---
    private Timeline timeline;
    private int seconds = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupNewRound();
        startTimer();
    }

    private void setupNewRound() {
        gameActive = true;
        playerTurn = true;
        statusLabel.setText("¡Tu turno (" + PLAYER_SYMBOL + ")!");
        
        // Limpiar el tablero lógico y visual
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = "";
                // Si es la primera ronda, crea los botones
                if (buttons[row][col] == null) {
                    buttons[row][col] = createGameButton(row, col);
                    gameGrid.add(buttons[row][col], col, row);
                }
                // Limpia el texto y estilo del botón
                buttons[row][col].setText("");
                buttons[row][col].getStyleClass().removeAll("game-button-x", "game-button-o");
            }
        }
    }

    private Button createGameButton(int row, int col) {
        Button button = new Button();
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.getStyleClass().add("game-button");
        button.setOnAction(event -> handleCellClick(row, col));
        return button;
    }

    private void handleCellClick(int row, int col) {
        if (!gameActive || !playerTurn || !board[row][col].isEmpty()) {
            return; // No hacer nada si no es el turno del jugador o la casilla está ocupada
        }

        // Movimiento del jugador
        updateBoard(row, col, PLAYER_SYMBOL);
        playerTurn = false;
        
        // Comprobar estado del juego
        if (checkForWinner(PLAYER_SYMBOL)) {
            endRound(PLAYER_SYMBOL);
        } else if (isBoardFull()) {
            endRound("EMPATE");
        } else {
            statusLabel.setText("Turno del Bot (" + BOT_SYMBOL + ")...");
            // Pausa para que el bot no juegue instantáneamente
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(event -> botMove()); // funcion lambda
            pause.play();
        }
    }
    
    private void botMove() {
        if (!gameActive) return;

        // Lógica simple del bot: encuentra la primera casilla vacía
        Random rand = new Random();
        int row, col;
        do {
            row = rand.nextInt(3);
            col = rand.nextInt(3);
        } while (!board[row][col].isEmpty());

        updateBoard(row, col, BOT_SYMBOL);
        
        if (checkForWinner(BOT_SYMBOL)) {
            endRound(BOT_SYMBOL);
        } else if (isBoardFull()) {
            endRound("EMPATE");
        } else {
            playerTurn = true;
            statusLabel.setText("¡Tu turno (" + PLAYER_SYMBOL + ")!");
        }
    }
    
    private void updateBoard(int row, int col, String symbol) {
        board[row][col] = symbol;
        buttons[row][col].setText(symbol);
        buttons[row][col].getStyleClass().add(symbol.equals(PLAYER_SYMBOL) ? "game-button-x" : "game-button-o");
    }

    private boolean checkForWinner(String symbol) {
        // Comprobar filas y columnas
        for (int i = 0; i < 3; i++) {
            if ((board[i][0].equals(symbol) && board[i][1].equals(symbol) && board[i][2].equals(symbol)) ||
                (board[0][i].equals(symbol) && board[1][i].equals(symbol) && board[2][i].equals(symbol))) {
                return true;
            }
        }
        // Comprobar diagonales
        if ((board[0][0].equals(symbol) && board[1][1].equals(symbol) && board[2][2].equals(symbol)) ||
            (board[0][2].equals(symbol) && board[1][1].equals(symbol) && board[2][0].equals(symbol))) {
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (board[row][col].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void endRound(String winner) {
        gameActive = false;
        
        switch (winner) {
            case PLAYER_SYMBOL:
                playerWins++;
                statusLabel.setText("¡Ganaste la ronda!");
                addRoundIcon(playerScoreBox, "✓", "round-win");
                addRoundIcon(botScoreBox, "✗", "round-loss");
                break;
            case BOT_SYMBOL:
                botWins++;
                statusLabel.setText("El Bot ganó la ronda.");
                addRoundIcon(playerScoreBox, "✗", "round-loss");
                addRoundIcon(botScoreBox, "✓", "round-win");
                break;
            default: // Empate
                statusLabel.setText("¡Ronda empatada!");
                addRoundIcon(playerScoreBox, "-", "round-draw");
                addRoundIcon(botScoreBox, "-", "round-draw");
                break;
        }

        // Comprobar si la partida terminó
        if (playerWins >= ROUNDS_TO_WIN || botWins >= ROUNDS_TO_WIN) {
            timeline.stop();
            endMatch(winner.equals(PLAYER_SYMBOL));
        } else {
            // Preparar la siguiente ronda después de una pausa
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(event -> setupNewRound());
            pause.play();
        }
    }

    private void addRoundIcon(HBox scoreBox, String text, String styleClass) {
        Label icon = new Label(text);
        icon.getStyleClass().addAll("round-icon", styleClass);
        scoreBox.getChildren().add(icon);
    }
    
    private void endMatch(boolean playerWon) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Partida Terminada");
        if(playerWon) {
            alert.setHeaderText("¡FELICIDADES, HAS GANADO LA PARTIDA!");
        } else {
            alert.setHeaderText("Has perdido la partida. ¡Mejor suerte la próxima!");
        }
        alert.showAndWait();
        
        // Cerrar la ventana del juego
        Stage stage = (Stage) gameGrid.getScene().getWindow();
        stage.close();
    }

    // --- Métodos del Cronómetro ---
    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            seconds++;
            int minutes = seconds / 60;
            int secs = seconds % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, secs));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
}