package Controladores;

import app.com.juegofx.juego.MainApp;
import app.com.juegofx.juego.multiplayer.ClienteJuego;
import app.com.juegofx.juego.multiplayer.Eleccion;
import app.com.juegofx.juego.multiplayer.GameState;
import app.com.juegofx.juego.multiplayer.Resultado;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MultiplayerController {

    @FXML private Label lblEstado, lblResultadoRonda, lblPuntuacionJugador, lblPuntuacionOponente;
    @FXML private HBox playerChoiceBox, oponenteChoiceBox;
    @FXML private ImageView imgOponentePiedra, imgOponentePapel, imgOponenteTijera;
    
    @FXML private ImageView playerResult1, playerResult2, playerResult3, playerResult4, playerResult5;
    @FXML private ImageView opponentResult1, opponentResult2, opponentResult3, opponentResult4, opponentResult5;
    private List<ImageView> playerResultIcons;
    private List<ImageView> opponentResultIcons;

    private ClienteJuego cliente;
    private Map<Eleccion, ImageView> oponenteImageMap;
    private Image CHECK_ICON, X_ICON, DASH_ICON;

    @FXML
    public void initialize() {
        // Cargar imágenes de iconos de forma segura
        try {
            CHECK_ICON = loadImage("/imagenes/check-casilla.png");
            X_ICON = loadImage("/imagenes/X-casilla.png");
            DASH_ICON = loadImage("/imagenes/raya-casilla.png");
        } catch (Exception e) {
            System.err.println("Error crítico al cargar imágenes de resultado: " + e.getMessage());
            // Mostrar un error y cerrar si los recursos básicos no están disponibles
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "No se pudieron cargar los iconos de resultado.");
            errorAlert.showAndWait();
            Platform.exit();
            return;
        }

        oponenteImageMap = Map.of(
            Eleccion.PIEDRA, imgOponentePiedra, Eleccion.PAPEL, imgOponentePapel, Eleccion.TIJERA, imgOponenteTijera
        );
        playerResultIcons = Arrays.asList(playerResult1, playerResult2, playerResult3, playerResult4, playerResult5);
        opponentResultIcons = Arrays.asList(opponentResult1, opponentResult2, opponentResult3, opponentResult4, opponentResult5);
        
        playerChoiceBox.setDisable(true);
        oponenteChoiceBox.setOpacity(0.5);
    }
    
    // --- MÉTODO FALTANTE AÑADIDO AQUÍ ---
    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        Objects.requireNonNull(stream, "No se puede encontrar el recurso de imagen en la ruta: " + path);
        return new Image(stream);
    }

    public void setCliente(ClienteJuego cliente) {
        this.cliente = cliente;
        this.cliente.setUpdateListener(this::actualizarUI);
    }


    // El placeholder para la lógica de los iconos del marcador visual.
    // Esto es más complejo y necesita que el GameState envíe la lista completa de resultados.
    private void updateScoreIndicators(GameState estado) {
        // Implementación futura si se decide enviar la lista de resultados de ronda
    }


  
    @FXML
    private void handlePlayerChoice(MouseEvent event) {
        ImageView clickedImage = (ImageView) event.getSource();
        Eleccion eleccion = Eleccion.valueOf(clickedImage.getUserData().toString());
        try {
            lblEstado.setText("¡Elección enviada! Esperando al oponente...");
            playerChoiceBox.setDisable(true);
            cliente.enviarEleccion(eleccion);
        } catch (IOException e) {
            // Manejar error de envío
            System.err.println("Error al enviar elección: " + e.getMessage());
        }
    }
    @FXML
    private void actualizarUI(GameState estado) {
        lblPuntuacionJugador.setText("Tú: " + estado.getMiPuntuacion());
        lblPuntuacionOponente.setText("Oponente: " + estado.getPuntuacionOponente());

        if (estado.isRondaTerminada()) {
            lblEstado.setText("Ronda " + estado.getRondaActual() + " terminada");
            lblResultadoRonda.setText(estado.getResultadoRonda().getMensaje());
            lblResultadoRonda.setTextFill(estado.getResultadoRonda() == app.com.juegofx.juego.multiplayer.Resultado.GANASTE ? Color.LIGHTGREEN : (estado.getResultadoRonda() == app.com.juegofx.juego.multiplayer.Resultado.PERDISTE ? Color.CRIMSON : Color.WHITE));
            
            mostrarEleccionOponente(estado.getEleccionOponente());

            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                Platform.runLater(() -> prepararNuevaRonda(estado.getRondaActual() + 1));
            }).start();
        } else {
            prepararNuevaRonda(estado.getRondaActual());
        }
    }

    private void prepararNuevaRonda(int numeroRonda) {
        lblEstado.setText("Ronda " + numeroRonda + ": ¡Elige tu jugada!");
        lblResultadoRonda.setText("");
        playerChoiceBox.setDisable(false);
        resetearEstiloOponente();
    }

    private void mostrarEleccionOponente(Eleccion eleccion) {
        resetearEstiloOponente();
        oponenteImageMap.get(eleccion).setStyle("-fx-effect: dropshadow(gaussian, #00ffff, 25, 0.9, 0, 0);");
        oponenteChoiceBox.setOpacity(1.0);
    }

    private void resetearEstiloOponente() {
        oponenteImageMap.values().forEach(img -> img.setStyle(""));
        oponenteChoiceBox.setOpacity(0.5);
    }
}