package app.com.juegofx.juego;

import Controladores.GameController;
import Controladores.MultiplayerController;
import app.com.juegofx.juego.Model.GameState;
import app.com.juegofx.juego.multiplayer.ClienteJuego;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static BorderPane rootLayout; // <-- El panel raíz que nunca cambia
    private static boolean isDarkTheme = false;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Jan Ken Po");
        primaryStage.setResizable(false);

        initRootLayout();
        switchToMenuScreen();
    }

    // Inicializa el BorderPane principal
    public void initRootLayout() {
        rootLayout = new BorderPane();
        Scene scene = new Scene(rootLayout, 800, 600);
        primaryStage.setScene(scene);
        applyTheme(); // Aplica el tema por defecto
        
        // --- CAMBIO CLAVE AQUÍ ---
        // Cambiar 'primaryStageTheme' a 'primaryStage'
        primaryStage.show();
    }

    public static void switchToMultiplayerScreen(ClienteJuego cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/MultiplayerScreen.fxml"));
            Parent multiplayerRoot = loader.load();

            // Inyectar el cliente en el controlador
            MultiplayerController controller = loader.getController();
            controller.setCliente(cliente);

            // Colocar la vista multijugador en el centro de nuestro BorderPane raíz
            rootLayout.setCenter(multiplayerRoot);

            // Re-aplicar el tema al BorderPane raíz. Esto asegurará que la nueva vista
            // herede los estilos correctos (tema por defecto o tema oscuro).
            applyTheme(); // <-- ¡CORRECTO! Sin parámetros.

            primaryStage.setTitle("Jan Ken Po - Multijugador");
            // No creamos una nueva escena, seguimos usando la principal.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- LÓGICA DE TEMAS SIMPLIFICADA ---
    private static void applyTheme() {
        if (rootLayout == null) {
            return;
        }
        rootLayout.getStylesheets().clear();

        URL baseCssUrl = MainApp.class.getResource("/estilos/menu-estilos.css");
        if (baseCssUrl != null) {
            rootLayout.getStylesheets().add(baseCssUrl.toExternalForm());
        }

        if (isDarkTheme) {
            URL darkCssUrl = MainApp.class.getResource("/estilos/dark-theme.css");
            if (darkCssUrl != null) {
                rootLayout.getStylesheets().add(darkCssUrl.toExternalForm());
            }
        }
    }

    public static void setTheme(boolean useDarkTheme) {
        isDarkTheme = useDarkTheme;
        applyTheme(); // Vuelve a aplicar los estilos al panel raíz
    }

    // --- LÓGICA DE NAVEGACIÓN SIMPLIFICADA ---
    private static void setView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Node view = loader.load();
            rootLayout.setCenter(view); // Coloca la vista cargada en el centro del BorderPane
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void switchToMenuScreen() {
        setView("/Vista/MenuInicio.fxml");
    }

    public static void switchToSettingsScreen() {
        setView("/Vista/SettingsScreen.fxml");
    }

    public static void switchToGameScreen(GameState loadedState) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/GameScreen.fxml"));
            Parent gameRoot = loader.load();

            GameController controller = loader.getController();
            if (loadedState != null) {
                controller.loadGame(loadedState);
            } else {
                controller.startNewGame();
            }

            // La pantalla de juego tiene su propio estilo y reemplaza todo el centro
            rootLayout.setCenter(gameRoot);

            // Aplicar el estilo de juego directamente al panel del juego
            URL gameCssUrl = MainApp.class.getResource("/estilos/game-estilos.css");
            if (gameCssUrl != null) {
                gameRoot.getStylesheets().add(gameCssUrl.toExternalForm());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static boolean isDarkThemeActive() {
        return isDarkTheme;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
