package Controladores;

import app.com.juegofx.juego.MainApp;

import app.com.juegofx.juego.Model.GameState;
import app.com.juegofx.juego.multiplayer.ClienteJuego;
import app.com.juegofx.juego.multiplayer.ServidorJuego;
import app.com.juegofx.juego.service.GameSaveService;
import java.io.IOException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;

import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

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
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Crear Partida", "Crear Partida", "Unirse a Partida");
        dialog.setTitle("Modo Multijugador");
        dialog.setHeaderText("¿Quieres ser el anfitrión o unirte a una partida existente?");
        dialog.setContentText("Elige una opción:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if (result.get().equals("Crear Partida")) {
                crearPartida();
            } else {
                unirseAPartida();
            }
        }
    }
    
    private void crearPartida() {
        // Iniciar el servidor en un hilo separado
        new Thread(() -> {
            try {
                new ServidorJuego(54321); // El servidor se queda escuchando
            } catch (IOException e) {
                Platform.runLater(() -> showError("Error de Servidor", "No se pudo iniciar el servidor. El puerto podría estar en uso."));
            }
        }).start();

        // Conectar este cliente al servidor local
        try {
            ClienteJuego cliente = new ClienteJuego("127.0.0.1", 54321);
            MainApp.switchToMultiplayerScreen(cliente);
        } catch (IOException e) {
            showError("Error de Conexión", "No se pudo conectar al servidor local.");
        }
    }

    private void unirseAPartida() {
        TextInputDialog dialog = new TextInputDialog("127.0.0.1");
        dialog.setTitle("Unirse a Partida");
        dialog.setHeaderText("Introduce la dirección IP del anfitrión:");
        dialog.setContentText("IP:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(ip -> {
            try {
                ClienteJuego cliente = new ClienteJuego(ip, 54321);
                MainApp.switchToMultiplayerScreen(cliente);
            } catch (IOException e) {
                showError("Error de Conexión", "No se pudo conectar al servidor en " + ip + ".");
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
}