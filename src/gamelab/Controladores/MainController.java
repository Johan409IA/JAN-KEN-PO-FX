package gamelab.Controladores;

import gamelab.Modelo.Preferencias;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;

/**
 * Controlador para la vista del menú principal (MainMenu.fxml). Gestiona las
 * acciones del usuario como iniciar partida, ir a configuración o salir.
 */
public class MainController {

    // --- Vinculación de Componentes FXML ---
    @FXML
    private Button btnRaya;
    @FXML
    private Button btnOpc;
    @FXML
    private Button btnRnd; // El botón "REANUDAR"
    @FXML
    private Button btnMjd;
    @FXML
    private ImageView exitImageView;

    // Este método se vincula con el onAction="#handleSalir" del botón en el FXML.
    @FXML
    private void handleSalir(MouseEvent event) {
        // Buena práctica: Pedir confirmación antes de cerrar la aplicación.
        // Esto previene cierres accidentales.

        // Creamos una alerta de tipo CONFIRMATION
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Salida");
        alert.setHeaderText("Estás a punto de salir de la aplicación.");
        alert.setContentText("¿Estás seguro de que quieres cerrar el juego?");

        // Mostramos la alerta y esperamos la respuesta del usuario.
        // showAndWait() pausa la ejecución hasta que el usuario cierra la alerta.
        Optional<ButtonType> result = alert.showAndWait();

        // Verificamos si el usuario presionó el botón "Aceptar" (OK)
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("Cerrando la aplicacion...");
            // Platform.exit() es la forma correcta y segura de cerrar una aplicación JavaFX.
            // Cierra todos los hilos de JavaFX limpiamente.
            Platform.exit();
        } else {
            // El usuario canceló la acción, no hacemos nada.
            System.out.println("Salida cancelada.");
        }
    }

    /**
     * Este método permite a otras clases (como GameLab.java) establecer la
     * imagen del ícono de salida según el tema.
     *
     * @param theme El tema actual ("Claro" o "Oscuro").
     */
    public void setExitIconForTheme(String theme) {
        String imagePath;
        if (Preferencias.TEMA_OSCURO.equals(theme)) {
            // Usamos la imagen que se ve bien en el fondo oscuro
            imagePath = "/Imagen/salir-oscuro.png";
        } else {
            // Usamos la imagen que se ve bien en el fondo claro
            imagePath = "/Imagen/salir.png";
        }

        try {
            // La forma de cargar la imagen es correcta, solo la ruta estaba mal.
            Image icon = new Image(getClass().getResource(imagePath).toExternalForm());
            exitImageView.setImage(icon);
            System.out.println("Ícono de salida cargado desde: " + imagePath); // Mensaje de depuración útil
        } catch (Exception e) {
            System.err.println("Error FATAL: No se pudo encontrar el ícono de salida en la ruta: " + imagePath);
            // Imprime la traza para ver más detalles del error si persiste
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpciones(ActionEvent event) {
        try {
            // 1. Cargar el archivo FXML de la ventana de configuración
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Configuracion.fxml"));
            Parent root = loader.load();

            // 1. Obtener el controlador de la ventana de configuración
            ConfiguracionController configController = loader.getController();

            // 1. Pasar la referencia de ESTE controlador (se mantiene)
            configController.setMainController(this);

            // 2. Obtener la escena actual y pasarla al controlador de configuración
            Scene currentScene = btnOpc.getScene();
            configController.setMainScene(currentScene);

            // 2. Crear un nuevo Stage (una nueva ventana)
            Stage configuracionStage = new Stage();
            configuracionStage.setTitle("Configuración");
            configuracionStage.setScene(new Scene(root));

            // 3. Configurar la modalidad (opcional pero recomendado)
            // Modality.APPLICATION_MODAL bloquea la interacción con la ventana principal
            // hasta que esta ventana de configuración se cierre.
            configuracionStage.initModality(Modality.APPLICATION_MODAL);

            // Opcional: establecer el "dueño" de la ventana
            Stage mainStage = (Stage) btnOpc.getScene().getWindow();
            configuracionStage.initOwner(mainStage);

            // 4. Mostrar la ventana y esperar a que el usuario la cierre
            configuracionStage.showAndWait();

            // Una vez que la ventana se cierra, podrías querer recargar algo en el menú principal.
            System.out.println("Ventana de configuración cerrada.");

        } catch (IOException e) {
            e.printStackTrace();
            // Mostrar una alerta de error al usuario
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo abrir la ventana de configuración.");
            alert.setContentText("Ocurrió un error al cargar la vista: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // En gamelab.Controladores.MainController.java
    @FXML
    private void handleJugar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/Juego.fxml"));
            Parent root = loader.load();

            JuegoController juegoController = loader.getController();
            // ¡Ahora le decimos que inicie una partida contra un bot y que el humano usa X!
            juegoController.initPartida(true, "X");

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
    private void handleReanudar(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gamelab/ReanudarPartida.fxml"));
            Parent root = loader.load();

            // --- INICIO DE LA MODIFICACIÓN ---
            // 1. Obtener el controlador de la ventana de reanudar
            ReanudarPartidaController reanudarController = loader.getController();

            // 2. Obtener la ventana actual (el menú principal) y pasarla al controlador
            Stage currentStage = (Stage) btnRnd.getScene().getWindow();
            reanudarController.setMainStage(currentStage);

            // --- FIN DE LA MODIFICACIÓN ---
            Stage stage = new Stage();
            stage.setTitle("Reanudar Partida");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(currentStage); // Dejamos esto, es buena práctica aunque no lo usemos para cerrar
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
