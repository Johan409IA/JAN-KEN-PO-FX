package app.com.juegofx.juego;

import Controladores.GameController;
import app.com.juegofx.juego.Model.GameState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import javafx.application.Platform;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene menuScene; // Guardamos la escena para no recargar el FXML cada vez
    private static boolean isDarkTheme = false;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false);
        // Creamos la escena del menú una sola vez al inicio
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/MenuInicio.fxml"));
            Parent root = loader.load();
            menuScene = new Scene(root, 800, 600);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error fatal: No se pudo cargar el menú principal. La aplicación se cerrara.");
            Platform.exit();
            return;
        }
        switchToMenuScreen(); // La mostramos por primera vez
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static boolean isDarkThemeActive() {
        return isDarkTheme;
    }

    // --- Lógica de Temas (Centralizada y Correcta) ---
    private static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        URL baseCssUrl = MainApp.class.getResource("/estilos/menu-estilos.css");
        if (baseCssUrl != null) {
            scene.getStylesheets().add(baseCssUrl.toExternalForm());
        }

        if (isDarkTheme) {
            URL darkCssUrl = MainApp.class.getResource("/estilos/dark-theme.css");
            if (darkCssUrl != null) {
                scene.getStylesheets().add(darkCssUrl.toExternalForm());
            } else {
                System.err.println("Advertencia: No se pudo encontrar el archivo dark-theme.css");
            }
        }
    }

    public static void setTheme(boolean useDarkTheme) {
        isDarkTheme = useDarkTheme;
        // Aplica el tema a la escena actual que está visible
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
        // También actualiza el tema de la escena del menú si existe
        if (menuScene != null) {
            applyTheme(menuScene);
        }
    }

    // --- CAMBIO CLAVE AQUÍ ---
    public static void switchToMenuScreen() {
        // Asegurarnos de que la escena del menú exista
        if (menuScene == null) {
            System.err.println("Error: La escena del menú no fue inicializada.");
            return;
        }

        // Volver a aplicar el tema actual a la escena del menú ANTES de mostrarla
        applyTheme(menuScene);

        primaryStage.setTitle("Jan Ken Po - Menú Principal");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    public static void switchToSettingsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/SettingsScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            applyTheme(scene); // Aplicar tema actual a la nueva escena de configuración

            primaryStage.setTitle("Configuración");
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToGameScreen(GameState loadedState) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/GameScreen.fxml"));
            Parent root = loader.load();

            GameController controller = loader.getController();
            if (loadedState != null) {
                controller.loadGame(loadedState);
            } else {
                controller.startNewGame();
            }

            Scene scene = new Scene(root, 800, 600);
            URL gameCssUrl = MainApp.class.getResource("/estilos/game-estilos.css");
            if (gameCssUrl != null) {
                scene.getStylesheets().add(gameCssUrl.toExternalForm());
            }
            // Aplicar tema oscuro si está activo
            if (isDarkTheme) {
                URL darkCssUrl = MainApp.class.getResource("/estilos/dark-theme.css");
                if (darkCssUrl != null) {
                    scene.getStylesheets().add(darkCssUrl.toExternalForm());
                }
            }

            primaryStage.setTitle("Jan Ken Po - Partida vs Bot");
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
