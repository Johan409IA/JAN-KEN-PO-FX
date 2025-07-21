package gamelab.Controladores;

import gamelab.GameLab;
import gamelab.Modelo.Partida;
import gamelab.Modelo.Preferencias;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import gamelab.Red.ClienteJuego; 
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;


public class JuegoController implements Initializable {

    // --- Componentes FXML Vinculados ---
    @FXML private Label lblEstado;
    @FXML private ImageView pauseIcon;
    @FXML private VBox puntuacionHumano;
    @FXML private VBox puntuacionBot;
    @FXML private Label lblNombreHumano;
    @FXML private Label lblNombreBot;
    @FXML private Label lblCronometro;
    

    // --- Celdas del Tablero Vinculadas ---
    @FXML private ImageView cell00, cell01, cell02;
    @FXML private ImageView cell10, cell11, cell12;
    @FXML private ImageView cell20, cell21, cell22;
    private ImageView[][] celdasTablero;
    

    // --- Variables de Estado del Juego ---
    private Partida partida;
    private Timeline cronometro;
    private int segundos;
    private List<String> resultadosRondas;
    private Image imagenX, imagenO, imagenCheck, imagenCruz, imagenGuion;
    private ClienteJuego clienteRed;
    private boolean isMultiplayer = false;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        segundos = 0;
        cronometro = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundos++;
            lblCronometro.setText(String.format("%02d:%02d", segundos / 60, segundos % 60));
        }));
        cronometro.setCycleCount(Timeline.INDEFINITE);
        
        try {
            imagenX = new Image(getClass().getResource("/Imagen/ficha_x.png").toExternalForm());
            imagenO = new Image(getClass().getResource("/Imagen/ficha_o.png").toExternalForm());
            imagenCheck = new Image(getClass().getResource("/Imagen/check.png").toExternalForm());
            imagenCruz = new Image(getClass().getResource("/Imagen/equis.png").toExternalForm());
            imagenGuion = new Image(getClass().getResource("/Imagen/raya.png").toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
        }

        celdasTablero = new ImageView[][]{
            {cell00, cell01, cell02},
            {cell10, cell11, cell12},
            {cell20, cell21, cell22}
        };
    }
    
    // --- Métodos de Inicialización de Partida ---
    
    public void initPartida(boolean contraBot, String fichaHumano) {
        this.isMultiplayer = false;
        this.partida = new Partida(contraBot, fichaHumano);
        iniciarJuego();
    }


    public void initPartidaCargada(Partida partidaCargada) {
        this.isMultiplayer = false;
        this.partida = partidaCargada;
        iniciarJuego();
    }
    
    private void iniciarJuego() {
        // --- INICIO DE LA CORRECCIÓN DEL BUG VISUAL ---
        // Usamos los getters del modelo que son la fuente de verdad.
        lblNombreHumano.setText("JUGADOR (" + partida.getJugadorHumano() + ")");
        lblNombreBot.setText("BOT (" + partida.getJugadorBot() + ")");
        // --- FIN DE LA CORRECCIÓN ---
        
        actualizarVistaCompleta();
        cronometro.play();
        verificarTurnoBotInicial();
    }
    
    
    
    private void verificarTurnoBotInicial(){
        if(partida.isContraBot() && partida.getJugadorActual().equals(partida.getJugadorBot())) {
            hacerJugadaBot();
        }
    }

    // --- Lógica de Juego y Eventos ---
    
    @FXML
    private void handleCellClick(MouseEvent event) {
        ImageView clickedCell = (ImageView) event.getSource();
        Integer fila = GridPane.getRowIndex(clickedCell);
        Integer col = GridPane.getColumnIndex(clickedCell);
        if (fila == null || col == null) return;

        if (isMultiplayer) {
            try {
                // En multijugador, solo enviamos el movimiento
                clienteRed.enviarMovimiento(fila, col);
            } catch (IOException e) {
                mostrarAlertaError("Error de Red", "No se pudo enviar el movimiento.");
            }
        } else { // Modo un jugador
            if (partida.isRondaTerminada() || (partida.isContraBot() && partida.getJugadorActual().equals(partida.getJugadorBot()))) {
                return;
            }
            if (partida.realizarMovimiento(fila, col)) {
                procesarFinDeTurno();
            }
        }
    }
    
    public void initPartidaMultijugador(ClienteJuego cliente) {
        this.isMultiplayer = true;
        this.clienteRed = cliente;
        this.clienteRed.setUpdateListener(this::actualizarDesdeRed);
        lblEstado.setText("Esperando al otro jugador...");
        // Ocultar el icono de pausa en multijugador para simplificar
        pauseIcon.setVisible(false);
    }
    
    /**
     * Este método es llamado por el ClienteJuego cada vez que llega
     * un nuevo estado de la partida desde el servidor.
     * @param partidaDesdeServidor El objeto Partida actualizado.
     */
    private void actualizarDesdeRed(Partida partidaDesdeServidor) {
        this.partida = partidaDesdeServidor;
        actualizarVistaCompleta();
        
        // --- INICIO DE LA CORRECCIÓN ---
        boolean esMiTurno = clienteRed.getMiFicha().equals(partida.getJugadorActual());
        boolean juegoTerminado = partida.isJuegoTerminado() || partida.isRondaTerminada();
        
        // Itera sobre el array de celdas para habilitar/deshabilitar
        for (ImageView[] fila : celdasTablero) {
            for (ImageView celda : fila) {
                celda.setDisable(!esMiTurno || juegoTerminado);
            }
        }
        // --- FIN DE LA CORRECCIÓN ---
    }
    
    private void hacerJugadaBot() {
        PauseTransition pausa = new PauseTransition(Duration.seconds(0.7));
        pausa.setOnFinished(e -> {
            partida.realizarMovimientoBot();
            procesarFinDeTurno();
        });
        pausa.play();
    }
    
    private void procesarFinDeTurno() {
        actualizarVistaCompleta();
        if (partida.isJuegoTerminado()) return;
        if (partida.isRondaTerminada()) {
            // Ya no necesitamos registrar el resultado aquí, el modelo lo hace solo.
            iniciarSiguienteRondaConPausa();
        } else if (partida.isContraBot() && partida.getJugadorActual().equals(partida.getJugadorBot())) {
            hacerJugadaBot();
        }
    }

    private void iniciarSiguienteRondaConPausa() {
        PauseTransition pausa = new PauseTransition(Duration.seconds(2.5));
        pausa.setOnFinished(e -> {
            partida.avanzarSiguienteRonda();
            actualizarVistaCompleta();
            verificarTurnoBotInicial();
        });
        pausa.play();
    }
    
    private void registrarResultadoRonda() {
        int[] puntajeActual = partida.getPuntaje();
        
        int xPrevio = 0, oPrevio = 0;
        for (String res : resultadosRondas) {
            if (res.equals("X")) xPrevio++;
            else if (res.equals("O")) oPrevio++;
        }
        
        if (puntajeActual[0] > xPrevio) resultadosRondas.add("X");
        else if (puntajeActual[1] > oPrevio) resultadosRondas.add("O");
        else resultadosRondas.add("Empate");
    }

    private void actualizarVistaCompleta() {
        if (partida == null) return; // Guardia para evitar errores antes de que la partida inicie
        actualizarTablero();
        actualizarPuntuacion();
        actualizarEstado();
    }
    
     private void actualizarTablero() {
        String[][] estadoTablero = partida.getTablero();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ImageView imageView = celdasTablero[i][j];
                if (estadoTablero[i][j].equals("X")) imageView.setImage(imagenX);
                else if (estadoTablero[i][j].equals("O")) imageView.setImage(imagenO);
                else imageView.setImage(null);
            }
        }
    }
    
    private void actualizarPuntuacion() {
        puntuacionHumano.getChildren().clear();
        puntuacionBot.getChildren().clear();
    
        List<String> resultados = partida.getResultadosRondas();
        String fichaPrincipal; // La ficha del jugador en el panel izquierdo

        if (isMultiplayer) {
            fichaPrincipal = clienteRed.getMiFicha();
        } else {
            fichaPrincipal = lblNombreHumano.getText().contains("(X)") ? "X" : "O";
        }

        for (String resultado : resultados) {
            if (resultado.equals("Empate")) {
                puntuacionHumano.getChildren().add(crearIconoResultado(imagenGuion));
                puntuacionBot.getChildren().add(crearIconoResultado(imagenGuion));
            } else if (resultado.equals(fichaPrincipal)) {
                puntuacionHumano.getChildren().add(crearIconoResultado(imagenCheck));
                puntuacionBot.getChildren().add(crearIconoResultado(imagenCruz));
            } else {
                puntuacionHumano.getChildren().add(crearIconoResultado(imagenCruz));
                puntuacionBot.getChildren().add(crearIconoResultado(imagenCheck));
            }
        }

        int rondasPendientes = partida.getRondasTotales() - resultados.size();
        for (int i = 0; i < rondasPendientes; i++) {
            puntuacionHumano.getChildren().add(crearIconoResultado(imagenGuion));
            puntuacionBot.getChildren().add(crearIconoResultado(imagenGuion));
        }
    }
    
    private ImageView crearIconoResultado(Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitHeight(30);
        iv.setFitWidth(30);
        return iv;
    }
    
    private void actualizarEstado() {
        if (isMultiplayer) {
            boolean esMiTurno = clienteRed.getMiFicha().equals(partida.getJugadorActual());
            celdasTablero[0][0].getScene().setCursor(esMiTurno ? javafx.scene.Cursor.HAND : javafx.scene.Cursor.DEFAULT);
            
            if (partida.isJuegoTerminado()) {
                lblEstado.setText("¡Partida Finalizada! Ganador: " + partida.getGanadorFinal());
            } else if(partida.isRondaTerminada()) {
                 lblEstado.setText("Ronda " + partida.getRondaActual() + " terminada. Esperando al servidor...");
            }
            else {
                lblEstado.setText(esMiTurno ? "¡Es tu turno!" : "Turno del Oponente");
            }
        } else { // Modo un jugador
            if (partida.isJuegoTerminado()) {
                lblEstado.setText("¡Partida Finalizada! Ganador: " + partida.getGanadorFinal());
                cronometro.stop();
                Platform.runLater(this::mostrarDialogoFinMultijugador);
            } else if (partida.isRondaTerminada()) {
                lblEstado.setText("Ronda " + partida.getRondaActual() + " terminada. Siguiente...");
            } else {
                lblEstado.setText("Ronda " + (partida.getRondaActual()) + " | Turno de " + partida.getJugadorActual());
            }
        }
    }

    // --- Métodos de Finalización y Navegación ---

    // --- NUEVO MÉTODO PARA EL FIN DE PARTIDA MULTIJUGADOR ---
    private void mostrarDialogoFinMultijugador() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fin de la Partida");
        alert.setHeaderText("La partida ha finalizado. Resultado: " + partida.getGanadorFinal());
        alert.setContentText("Serás devuelto al menú principal.");
        
        ButtonType btnOk = new ButtonType("Volver al Menú");
        alert.getButtonTypes().setAll(btnOk);
        
        alert.showAndWait();
        salirAlMenuDesdeComponente(); // Reutilizamos el método para volver al menú
    }
    
     private void reiniciarPartida() {
        segundos = 0;
        lblCronometro.setText("00:00");

        // --- INICIO DE LA CORRECCIÓN ---

        // 1. Leer las preferencias guardadas para saber qué ficha eligió el usuario.
        Preferencias prefs;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("preferencias.dat"))) {
            prefs = (Preferencias) ois.readObject();
        } catch (Exception e) {
            System.out.println("No se pudo cargar preferencias para reiniciar, usando valores por defecto.");
            prefs = new Preferencias(); // Si falla, usa los valores por defecto (ficha X)
        }

        // 2. Iniciar la nueva partida usando la ficha de las preferencias.
        // Mantenemos el modo de juego (contraBot) de la partida anterior.
        this.initPartida(partida.isContraBot(), prefs.getFichaPreferida());
     }
    
    private void salirAlMenuDesdeComponente() {
        try {
            // Obtiene la ventana actual de forma segura desde un componente que sabes que existe
            Stage currentStage = (Stage) lblEstado.getScene().getWindow();
            currentStage.close();
            
            // Carga las preferencias para aplicar el tema correcto al menú principal
            Preferencias prefs;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("preferencias.dat"))) {
                prefs = (Preferencias) ois.readObject();
            } catch (Exception e) {
                prefs = new Preferencias();
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Main.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.setExitIconForTheme(prefs.getTema());
            
            Scene mainScene = new Scene(root);
            GameLab.aplicarTema(mainScene, prefs.getTema());
            
            Stage mainStage = new Stage();
            mainStage.setTitle("GameLab");
            mainStage.setScene(mainScene);
            mainStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handlePausa(MouseEvent event) {
        cronometro.pause();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/PausaMenu.fxml"));
            Parent root = loader.load();
            PausaMenuController controller = loader.getController();
            controller.initData(this.partida, (Stage) pauseIcon.getScene().getWindow());

            Stage pauseStage = new Stage(StageStyle.TRANSPARENT);
            pauseStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.setFill(null);
            pauseStage.setScene(scene);
            pauseStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
        cronometro.play();
    }
    
    private void mostrarAlertaError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}