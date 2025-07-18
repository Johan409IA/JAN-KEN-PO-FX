package gamelab.Controladores;

import gamelab.GameLab;
import gamelab.Modelo.Preferencias;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable {

    // --- Componentes FXML Vinculados ---
    @FXML private Button btnRaya;
    @FXML private Button btnOpc;
    @FXML private Button btnRnd;
    @FXML private Button btnMjd;
    @FXML private ImageView exitImageView;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        System.out.println("Controlador del Menú Principal inicializado.");
    }

    // --- MANEJADORES DE EVENTOS ---

    @FXML
    private void handleJugar(ActionEvent event) {
        try {
            Preferencias prefs;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("preferencias.dat"))) {
                prefs = (Preferencias) ois.readObject();
            } catch (Exception e) {
                prefs = new Preferencias();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Juego.fxml"));
            Parent root = loader.load();
            
            JuegoController juegoController = loader.getController();
            juegoController.initPartida(true, prefs.getFichaPreferida());
            
            Stage gameStage = new Stage();
            gameStage.setTitle("Tic-Tac-Toe vs Bot");
            gameStage.setScene(new Scene(root));
            
            ((Stage) btnRaya.getScene().getWindow()).close();
            gameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpciones(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Configuracion.fxml"));
            Parent root = loader.load();
            
            ConfiguracionController configController = loader.getController();
            // Le pasamos la referencia de este controlador para que nos pueda notificar
            configController.setMainController(this);
            
            // Creamos y configuramos la nueva ventana (Stage)
            Stage configuracionStage = new Stage();
            configuracionStage.setTitle("Configuración");
            configuracionStage.initModality(Modality.APPLICATION_MODAL);
            configuracionStage.initOwner((Stage) btnOpc.getScene().getWindow());
            configuracionStage.setScene(new Scene(root));
            
            // Mostramos la ventana y esperamos a que se cierre
            configuracionStage.showAndWait();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar una alerta de error si falla la carga
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la ventana de configuración.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleReanudar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/ReanudarPartida.fxml"));
            Parent root = loader.load();
            
            ReanudarPartidaController reanudarController = loader.getController();
            Stage currentStage = (Stage) btnRnd.getScene().getWindow();
            reanudarController.setMainStage(currentStage);
            
            Stage stage = new Stage();
            stage.setTitle("Reanudar Partida");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSalir(MouseEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Salida");
        alert.setHeaderText("Vas a salir de GameLab");
        alert.setContentText("¿Estás seguro de que quieres cerrar el juego?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    // --- MÉTODOS PÚBLICOS ---

    public void setExitIconForTheme(String theme) {
        String imagePath = Preferencias.TEMA_OSCURO.equals(theme) ? "/Imagen/salir-oscuro.png" : "/Imagen/salir.png";
        try {
            Image icon = new Image(getClass().getResource(imagePath).toExternalForm());
            exitImageView.setImage(icon);
        } catch (Exception e) {
            System.err.println("Error: No se pudo encontrar el ícono de salida en la ruta: " + imagePath);
        }
    }
    
    public void actualizarVistaDesdePreferencias(Preferencias prefs) {
        if (prefs == null) return;
        
        Scene currentScene = btnOpc.getScene(); 
        if (currentScene != null) {
            GameLab.aplicarTema(currentScene, prefs.getTema());
        }
        
        setExitIconForTheme(prefs.getTema());
    }
}