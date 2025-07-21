package gamelab.Controladores;

import gamelab.Red.ClienteJuego;
import gamelab.Red.ServidorJuego;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class MultijugadorMenuController {
    
    @FXML private Label lblStatus;
    private Stage mainStage;
    
    // Setter para recibir el Stage principal
    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    private void handleCrearPartida(ActionEvent event) {
        lblStatus.setText("Iniciando servidor... Esperando oponente...");
        
        new Thread(() -> {
            try {
                // Esta llamada ahora es válida.
                ServidorJuego servidor = new ServidorJuego(12345);
                servidor.esperarConexiones(); 
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    mostrarError("No se pudo iniciar el servidor. El puerto podría estar en uso.");
                    lblStatus.setText("Error al iniciar servidor.");
                });
            }
        }).start();

        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> conectarA("127.0.0.1"));
        }).start();
    }

    @FXML
    private void handleUnirsePartida(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("Unirse a Partida");
        dialog.setHeaderText("Introduce la dirección IP del anfitrión:");
        dialog.setContentText("IP:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::conectarA);
    }
    
    private void conectarA(String ip) {
        try {
            ClienteJuego cliente = new ClienteJuego(ip, 12345);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Juego.fxml"));
            Parent root = loader.load();
            
            JuegoController juegoController = loader.getController();
            juegoController.initPartidaMultijugador(cliente);
            
            Stage gameStage = new Stage();
            gameStage.setTitle("Tic-Tac-Toe Multijugador");
            gameStage.setScene(new Scene(root));
            
            // --- INICIO DE LA CORRECCIÓN: Cerrar ventanas anteriores ---
            Stage menuMultiplayerStage = (Stage) lblStatus.getScene().getWindow();
            menuMultiplayerStage.close();
            if (mainStage != null) {
                mainStage.close();
            }
            // --- FIN DE LA CORRECCIÓN ---
            
            gameStage.show();
            
        } catch (Exception ex) {
            mostrarError("No se pudo conectar al servidor en " + ip + ".");
            lblStatus.setText("Error de conexión.");
            ex.printStackTrace();
        }
    }
    
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Red");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}