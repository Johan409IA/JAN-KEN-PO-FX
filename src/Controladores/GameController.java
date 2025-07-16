package Controladores;

import app.com.juegofx.juego.MainApp;
import app.com.juegofx.juego.Model.Eleccion;
import app.com.juegofx.juego.Model.GameState;
import app.com.juegofx.juego.Model.LogicaJuego;
import app.com.juegofx.juego.Model.Resultado;
import app.com.juegofx.juego.service.GameSaveService; // <-- Importar el nuevo servicio
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GameController {

    // --- FXML Elements and other fields remain the same ---
    @FXML private VBox playerChoiceBox;
    @FXML private Label roundResultText, choicesMadeText, playerScoreLabel, botScoreLabel, timerLabel;
    @FXML private ImageView playerPaper, playerPiedra, playerTijera;
    @FXML private ImageView botPaper, botPiedra, botTijera;
    @FXML private ImageView playerResult1, playerResult2, playerResult3, playerResult4, playerResult5;
    @FXML private ImageView botResult1, botResult2, botResult3, botResult4, botResult5;
    private List<ImageView> playerResultIcons;
    private List<ImageView> botResultIcons;
    @FXML private ImageView pauseButton;

    private LogicaJuego gameLogic;
    private Timeline timeline;
    private int timeSeconds = 0;
    private Map<Eleccion, ImageView> botChoiceMap;
    
    private Image CHECK_ICON;
    private Image X_ICON;
    private Image DASH_ICON;
    
    // --- Initialize, Start/Load Game, and other methods remain the same ---
    @FXML
    public void initialize() {
        try {
            CHECK_ICON = loadImage("/imagenes/check-casilla.png");
            X_ICON = loadImage("/imagenes/X-casilla.png");
            DASH_ICON = loadImage("/imagenes/raya-casilla.png");
        } catch (NullPointerException e) {
            System.err.println("Error: No se pudo cargar una de las imágenes de resultado. Verifica los nombres de los archivos.");
            Platform.exit();
            return;
        }
        gameLogic = new LogicaJuego();
        playerResultIcons = Arrays.asList(playerResult1, playerResult2, playerResult3, playerResult4, playerResult5);
        botResultIcons = Arrays.asList(botResult1, botResult2, botResult3, botResult4, botResult5);
        botChoiceMap = Map.of(Eleccion.PAPEL, botPaper, Eleccion.PIEDRA, botPiedra, Eleccion.TIJERA, botTijera);
    }
    
    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        Objects.requireNonNull(stream, "No se puede encontrar el recurso: " + path);
        return new Image(stream);
    }
    
    public void startNewGame() { setupInitialGame(null); }
    public void loadGame(GameState state) { setupInitialGame(state); }
    
    private void setupInitialGame(GameState state) {
        if (state != null) { gameLogic.loadState(state); } 
        else { gameLogic.resetGame(); }
        setPlayerChoicesClickable(true);
        updateUIForNewRound();
        setupTimer();
        timeline.play();
    }
    
    @FXML
    private void handlePlayerChoice(MouseEvent event) {
        setPlayerChoicesClickable(false);
        ImageView clickedImage = (ImageView) event.getSource();
        Eleccion playerChoice = Eleccion.valueOf(clickedImage.getUserData().toString());
        Eleccion botChoice = gameLogic.getBotChoice();
        gameLogic.playRound(playerChoice, botChoice);
        displayRoundResult(playerChoice, botChoice);
    }

    // --- CAMBIO CLAVE: Lógica de Pausa actualizada ---
    @FXML
    private void handlePause() {
        timeline.pause();
        setPlayerChoicesClickable(false);

        Alert pauseDialog = new Alert(Alert.AlertType.CONFIRMATION);
        pauseDialog.setTitle("Partida en Pausa");
        pauseDialog.setHeaderText("El juego está en pausa. ¿Qué deseas hacer?");
        
        ButtonType resumeButton = new ButtonType("Regresar a la Partida");
        ButtonType saveAndExitButton = new ButtonType("Guardar y Salir");
        ButtonType exitWithoutSavingButton = new ButtonType("Salir sin Guardar");

        pauseDialog.getButtonTypes().setAll(resumeButton, saveAndExitButton, exitWithoutSavingButton);

        Optional<ButtonType> result = pauseDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == saveAndExitButton) {
                // Llama al nuevo método de guardado automático
                GameSaveService.saveGameAutomatically(gameLogic.saveState());
                MainApp.switchToMenuScreen();
            } else if (result.get() == exitWithoutSavingButton) {
                MainApp.switchToMenuScreen();
            } else { // Si es "Regresar" o se cierra el diálogo
                timeline.play();
                setPlayerChoicesClickable(true);
            }
        } else {
            // Comportamiento por defecto si se cierra el diálogo
            timeline.play();
            setPlayerChoicesClickable(true);
        }
    }
    
    // El resto de los métodos (endGame, displayRoundResult, updateScoreboard, etc.) permanecen igual que antes.
    // ... pégalos aquí desde la versión anterior ...
    private void endGame() {
        timeline.stop();
        setPlayerChoicesClickable(false);

        Platform.runLater(() -> {
            Alert endDialog = new Alert(Alert.AlertType.CONFIRMATION);
            endDialog.setTitle("Fin de la Partida");
            endDialog.setHeaderText(gameLogic.getGameWinnerMessage());
            endDialog.setContentText("¿Qué deseas hacer ahora?");
            ButtonType restartButton = new ButtonType("Reiniciar Partida");
            ButtonType exitButton = new ButtonType("Salir al Menú");
            endDialog.getButtonTypes().setAll(restartButton, exitButton);
            Optional<ButtonType> result = endDialog.showAndWait();
            if (result.isPresent() && result.get() == restartButton) {
                setupInitialGame(null);
            } else {
                MainApp.switchToMenuScreen();
            }
        });
    }

    private void displayRoundResult(Eleccion playerChoice, Eleccion botChoice) {
        botChoiceMap.get(botChoice).getStyleClass().add("bot-choice-selected");
        roundResultText.setText(gameLogic.getRoundResultText(playerChoice, botChoice));
        choicesMadeText.setText("Tú: " + playerChoice + " | Bot: " + botChoice);
        updateScoreboard();

        Timeline roundPause = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (gameLogic.isGameOver()) {
                endGame();
            } else {
                setPlayerChoicesClickable(true);
                updateUIForNewRound();
            }
        }));
        roundPause.play();
    }
    
    private void updateScoreboard() {
        playerScoreLabel.setText("Jugador: " + gameLogic.getPlayerScore());
        botScoreLabel.setText("Bot: " + gameLogic.getBotScore());
        List<Resultado> roundResults = gameLogic.getRoundResults();
        for (int i = 0; i < playerResultIcons.size(); i++) {
            if (i < roundResults.size()) {
                Resultado result = roundResults.get(i);
                playerResultIcons.get(i).setImage(result == Resultado.GANASTE ? CHECK_ICON : (result == Resultado.PERDISTE ? X_ICON : DASH_ICON));
                botResultIcons.get(i).setImage(result == Resultado.PERDISTE ? CHECK_ICON : (result == Resultado.GANASTE ? X_ICON : DASH_ICON));
            } else {
                playerResultIcons.get(i).setImage(null);
                botResultIcons.get(i).setImage(null);
            }
        }
    }

    private void updateUIForNewRound() {
        roundResultText.setText("¡Elige tu jugada!");
        choicesMadeText.setText("");
        botChoiceMap.values().forEach(img -> img.getStyleClass().setAll("bot-choice-image"));
        updateScoreboard();
    }

    private void setupTimer() {
        if (timeline != null) timeline.stop();
        timeSeconds = gameLogic.getCurrentRound() == 0 ? 0 : timeSeconds;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeSeconds++;
            timerLabel.setText(String.format("%d:%02d", timeSeconds / 60, timeSeconds % 60));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    private void setPlayerChoicesClickable(boolean clickable) {
        playerChoiceBox.setDisable(!clickable);
        playerChoiceBox.setOpacity(clickable ? 1.0 : 0.5);
    }
}