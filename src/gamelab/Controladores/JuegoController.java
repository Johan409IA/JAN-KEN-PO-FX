package gamelab.Controladores;

import gamelab.Modelo.Partida;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class JuegoController implements Initializable {

    @FXML private GridPane tableroGrid;
    @FXML private Label lblEstado;
    @FXML private ImageView pauseIcon;
    @FXML private HBox puntuacionX;
    @FXML private HBox puntuacionO;
    @FXML private Label lblCronometro;
    @FXML private Button btnSiguienteRonda;

    private Partida partida;
    private Timeline cronometro;
    private int segundos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Constructor del cronómetro
        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundos++;
            int mins = segundos / 60;
            int secs = segundos % 60;
            lblCronometro.setText(String.format("%02d:%02d", mins, secs));
        }));
        cronometro.setCycleCount(Timeline.INDEFINITE);
    }
    
    // El método `initNuevaPartida` ahora no existe, usamos uno más específico
    public void initPartida(boolean contraBot, String fichaHumano) {
        this.partida = new Partida(contraBot, fichaHumano);
        iniciarJuego();
    }

    public void initPartidaCargada(Partida partidaCargada) {
        this.partida = partidaCargada;
        iniciarJuego();
    }
    
    private void iniciarJuego() {
        actualizarVistaCompleta();
        cronometro.play();
        // Si el bot empieza la partida
        if(partida.isContraBot() && partida.getJugadorActual().equals(partida.getJugadorBot())) {
            hacerJugadaBot();
        }
    }

    @FXML
    private void handleCasillaClick(ActionEvent event) {
        if (partida.isRondaTerminada() || (partida.isContraBot() && partida.getJugadorActual().equals(partida.getJugadorBot()))) {
            return; // No permitir clic si la ronda terminó o si es turno del bot
        }
        
        Button botonClicado = (Button) event.getSource();
        if (partida.realizarMovimiento(GridPane.getRowIndex(botonClicado), GridPane.getColumnIndex(botonClicado))) {
            actualizarVistaCompleta();
            
            // Si es una partida contra el bot y no ha terminado, el bot juega
            if (partida.isContraBot() && !partida.isRondaTerminada()) {
                hacerJugadaBot();
            }
        }
    }
    
    private void hacerJugadaBot() {
        // Pequeña pausa para que la jugada del bot no sea instantánea
        Timeline pausaBot = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            partida.realizarMovimientoBot();
            actualizarVistaCompleta();
        }));
        pausaBot.play();
    }
    
    @FXML
    private void handleSiguienteRonda(ActionEvent event) {
        partida.avanzarSiguienteRonda();
        actualizarVistaCompleta();
    }

    private void actualizarVistaCompleta() {
        // Actualizar el tablero
        String[][] estadoTablero = partida.getTablero();
        for (Node node : tableroGrid.getChildren()) {
            if (node instanceof Button) {
                Button boton = (Button) node;
                boton.setText(estadoTablero[GridPane.getRowIndex(boton)][GridPane.getColumnIndex(boton)]);
                boton.setDisable(partida.isRondaTerminada()); // Desactivar tablero al final de la ronda
            }
        }

        // Actualizar puntuación
        actualizarPuntuacion(puntuacionX, partida.getPuntaje()[0]);
        actualizarPuntuacion(puntuacionO, partida.getPuntaje()[1]);

        // Actualizar label de estado y botones
        if (partida.isJuegoTerminado()) {
            lblEstado.setText("¡Juego Terminado! Ganador final: " + (partida.getPuntaje()[0] > partida.getPuntaje()[1] ? "X" : "O"));
            btnSiguienteRonda.setVisible(false);
            cronometro.stop();
        } else if (partida.isRondaTerminada()) {
            lblEstado.setText("Ronda " + partida.getRondaActual() + " terminada.");
            btnSiguienteRonda.setVisible(true);
        } else {
            lblEstado.setText("Ronda " + partida.getRondaActual() + " - Turno de " + partida.getJugadorActual());
            btnSiguienteRonda.setVisible(false);
        }
    }
    
    private void actualizarPuntuacion(HBox hboxPuntuacion, int puntaje) {
        hboxPuntuacion.getChildren().clear();
        for(int i = 0; i < puntaje; i++) {
            Circle c = new Circle(10, Color.GREEN);
            hboxPuntuacion.getChildren().add(c);
        }
    }
    
    @FXML
    private void handlePausa(MouseEvent event) {
        cronometro.pause(); // Pausar el cronómetro
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/PausaMenu.fxml"));
            Parent root = loader.load();
            PausaMenuController controller = loader.getController();
            controller.initData(this.partida, (Stage) pauseIcon.getScene().getWindow());

            Stage pauseStage = new Stage(StageStyle.TRANSPARENT);
            pauseStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, Color.TRANSPARENT);
            pauseStage.setScene(scene);
            pauseStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
        cronometro.play(); // Reanudar el cronómetro al cerrar la pausa
    }
}