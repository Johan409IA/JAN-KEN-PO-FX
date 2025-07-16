package Controladores;

import app.com.juegofx.juego.MainApp;

import app.com.juegofx.juego.Model.GameState;
import app.com.juegofx.juego.service.GameSaveService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;

import java.util.Optional;

public class MenuController {

    @FXML
    void handleIniciarPartida(ActionEvent event) {
        System.out.println("Botón 'Iniciar Partida' presionado. Navegando a la pantalla de juego...");
        MainApp.switchToGameScreen(null); // Pasa null para una nueva partida
    }

    @FXML
    void handleReanudarPartida(ActionEvent event) {
        System.out.println("Botón 'Reanudar Partida' presionado.");
        GameState loadedState = GameSaveService.loadGameFromList();
        if (loadedState != null) {
            MainApp.switchToGameScreen(loadedState);
        }
    }

    @FXML
    void handleConfiguracion(ActionEvent event) {
        System.out.println("Botón 'Configuración' presionado. Navegando a la pantalla de configuración...");
        MainApp.switchToSettingsScreen();
    }

    @FXML
    void handleExit(MouseEvent event) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Salir del Juego");
        confirmation.setHeaderText("¿Estás seguro de que quieres salir?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }
    
    @FXML
    void handleMultijugador(ActionEvent event) {
        System.out.println("Botón 'Multijugador' presionado. (Funcionalidad pendiente)");
    }
}