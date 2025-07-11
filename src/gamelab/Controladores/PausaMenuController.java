package gamelab.Controladores;

import gamelab.Modelo.Partida;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import gamelab.GameLab;
import gamelab.Modelo.Preferencias;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class PausaMenuController {

    private Partida partidaActual;
    private Stage gameStage;

    public void initData(Partida partida, Stage gameStage) {
        this.partidaActual = partida;
        this.gameStage = gameStage;
    }

    @FXML
    private void handleReanudar(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSalirAlMenu(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir");
        alert.setHeaderText("Vas a salir de la partida.");
        alert.setContentText("¿Deseas guardar el progreso antes de salir?");

        ButtonType btnSi = new ButtonType("Sí, guardar");
        ButtonType btnNo = new ButtonType("No, salir sin guardar");
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(btnSi, btnNo, btnCancelar);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == btnSi) {
                guardarPartida();
                cerrarTodo(event);
            } else if (result.get() == btnNo) {
                cerrarTodo(event);
            }
            // Si es Cancelar, no hace nada.
        }
    }

    private void guardarPartida() {
        if (partidaActual == null) {
            System.err.println("Error: No hay partida para guardar.");
            return;
        }

        File savesDir = new File("saves");
        if (!savesDir.exists()) {
            savesDir.mkdirs(); // Crea la carpeta "saves" si no existe
        }

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd 'a las' HH.mm.ss").format(new Date());
        String fileName = "saves/partida_" + timeStamp + ".sav";

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(partidaActual);
            mostrarInfo("Partida Guardada", "La partida se ha guardado exitosamente.");
        } catch (IOException e) {
            e.printStackTrace();
            mostrarError("Error al Guardar", "No se pudo guardar la partida.");
        }
    }

     private void cerrarTodo(ActionEvent event) {
        // 1. Cierra la ventana del juego actual
        if (gameStage != null) {
            gameStage.close();
        }
        
        // 2. Cierra la ventana de pausa actual
        Stage pauseStage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        pauseStage.close();

        // 3. Reabre el menú principal de forma segura
        try {
            // Cargar las preferencias para saber qué tema aplicar
            Preferencias prefs;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("preferencias.dat"))) {
                prefs = (Preferencias) ois.readObject();
            } catch (Exception e) {
                System.out.println("No se pudo cargar prefs en PausaMenu, usando valores por defecto.");
                prefs = new Preferencias();
            }

            // Cargar la vista del menú principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Main.fxml"));
            Parent root = loader.load();

            // Obtener el controlador para establecer el ícono de salir
            MainController mainController = loader.getController();
            mainController.setExitIconForTheme(prefs.getTema());
            
            // Crear la nueva escena
            Scene mainScene = new Scene(root);
            
            // ¡Paso clave! Aplicar el tema a la nueva escena usando nuestro método centralizado
            GameLab.aplicarTema(mainScene, prefs.getTema());

            // Crear el nuevo Stage y ASOCIAR la escena
            Stage mainStage = new Stage();
            mainStage.setTitle("GameLab");
            mainStage.setScene(mainScene); // Asociamos la escena ANTES de intentar usarla
            
            // Mostrar la nueva ventana del menú principal
            mainStage.show();

        } catch(Exception e) {
            e.printStackTrace();
            // Mostrar una alerta de error si falla la carga del menú
            mostrarError("Error Crítico", "No se pudo volver al menú principal.");
        }
    }
    
    private void mostrarError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    /**
     * Muestra una ventana de alerta de tipo INFORMATION.
     * @param titulo El título de la ventana de alerta.
     * @param contenido El mensaje principal a mostrar.
     */
    private void mostrarInfo(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
