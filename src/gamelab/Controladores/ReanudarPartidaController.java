package gamelab.Controladores;

import gamelab.Modelo.Partida;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class ReanudarPartidaController implements Initializable {

    @FXML private ListView<String> listaPartidas;
    private final String RUTA_SAVES = "saves/";
    
    private Stage mainStage; // Referencia a la ventana del menú principal

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarListaDePartidas();
    }
    
    private void cargarListaDePartidas() {
        File savesDir = new File(RUTA_SAVES);
        if (savesDir.exists() && savesDir.isDirectory()) {
            String[] files = savesDir.list((dir, name) -> name.toLowerCase().endsWith(".sav"));
            if (files != null) {
                Arrays.sort(files); // Ordenar para mostrar los más recientes al final
                ObservableList<String> items = FXCollections.observableArrayList(Arrays.asList(files));
                listaPartidas.setItems(items);
            }
        } else {
            listaPartidas.setPlaceholder(new Label("No hay partidas guardadas."));
        }
    }

      // --- NUEVO MÉTODO SETTER ---
    /**
     * Permite que el controlador que abre esta ventana (MainController)
     * le pase una referencia del Stage principal.
     * @param mainStage La ventana del menú principal.
     */
    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }
    
    

    @FXML
    private void handleCargar(ActionEvent event) {
        String archivoSeleccionado = listaPartidas.getSelectionModel().getSelectedItem();
        if (archivoSeleccionado == null) {
            mostrarAlerta("Error", "Por favor, selecciona una partida para cargar.");
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_SAVES + archivoSeleccionado))) {
            Partida partidaCargada = (Partida) ois.readObject();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Juego.fxml"));
            Parent root = loader.load();

            JuegoController juegoController = loader.getController();
            juegoController.initPartidaCargada(partidaCargada);

            Stage gameStage = new Stage();
            gameStage.setTitle("Tic-Tac-Toe (Reanudado)");
            gameStage.setScene(new Scene(root));
            
            // --- INICIO DE LA CORRECCIÓN ---
            
            // Cierra la ventana principal USANDO LA REFERENCIA DIRECTA
            if (mainStage != null) {
                mainStage.close();
            }
            
            // Cierra la ventana actual (la de Reanudar Partida)
            cerrarVentana(event);
            
            // --- FIN DE LA CORRECCIÓN ---
            
            gameStage.show();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "El archivo de guardado está corrupto o no se pudo leer.");
        }
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
        String archivoSeleccionado = listaPartidas.getSelectionModel().getSelectedItem();
        if (archivoSeleccionado == null) {
            mostrarAlerta("Error", "Por favor, selecciona una partida para eliminar.");
            return;
        }
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Estás seguro de que quieres eliminar esta partida guardada?");
        confirmacion.setContentText(archivoSeleccionado);
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            File fileToDelete = new File(RUTA_SAVES + archivoSeleccionado);
            if (fileToDelete.delete()) {
                System.out.println("Archivo eliminado: " + archivoSeleccionado);
                cargarListaDePartidas(); // Recargar la lista
            } else {
                mostrarAlerta("Error", "No se pudo eliminar el archivo.");
            }
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana(event);
    }
    
    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
    
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}