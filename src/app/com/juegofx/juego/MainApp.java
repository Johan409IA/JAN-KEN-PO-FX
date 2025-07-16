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
// Holaa
public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene menuScene; // Guardar la escena del menú para aplicar temas
    private static boolean isDarkTheme = false;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setResizable(false);
        switchToMenuScreen();
    }

    public static Stage getPrimaryStage() { return primaryStage; }
    
    public static boolean isDarkThemeActive() { return isDarkTheme; }

    // --- ¡NUEVO! Método para cambiar el tema ---
    public static void setDarkTheme(boolean active) {
        isDarkTheme = active;
        if (menuScene != null) {
            menuScene.getStylesheets().clear(); // Limpiar estilos antiguos
            menuScene.getStylesheets().add(MainApp.class.getResource("/estilos/menu-estilos.css").toExternalForm());
            if (isDarkTheme) {
                menuScene.getStylesheets().add(MainApp.class.getResource("/estilos/dark-theme.css").toExternalForm());
            }
        }
    }

    public static void switchToMenuScreen() {
        try {
            if (menuScene == null) { // Crear la escena solo la primera vez
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/MenuInicio.fxml"));
                Parent root = loader.load();
                menuScene = new Scene(root, 800, 600);
                setDarkTheme(isDarkTheme); // Aplicar tema inicial
            }
            primaryStage.setTitle("Jan Ken Po - Menú Principal");
            primaryStage.setScene(menuScene);
            primaryStage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    // --- ¡NUEVO! Método para cambiar a la pantalla de configuración ---
    public static void switchToSettingsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/Vista/SettingsScreen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 800, 600);
            
            // Aplicar el tema actual a la pantalla de configuración
            scene.getStylesheets().add(MainApp.class.getResource("/estilos/menu-estilos.css").toExternalForm());
            if (isDarkTheme) {
                scene.getStylesheets().add(MainApp.class.getResource("/estilos/dark-theme.css").toExternalForm());
            }

            primaryStage.setTitle("Configuración");
            primaryStage.setScene(scene);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static void switchToGameScreen(GameState loadedState) { /* ... (sin cambios) ... */ }
    public static void main(String[] args) { launch(args); }
}