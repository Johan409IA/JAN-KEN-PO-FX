package Controladores;

import app.com.juegofx.juego.MainApp;
import app.com.juegofx.juego.service.GameSaveService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class SettingsController {

    @FXML private ToggleButton darkThemeToggle;
    @FXML private ListView<String> savedGamesListView;
    @FXML private Button renameButton;
    @FXML private Button deleteButton;

    @FXML
    public void initialize() {
        boolean isDarkTheme = MainApp.isDarkThemeActive();
        darkThemeToggle.setSelected(isDarkTheme);
        updateToggleButtonText(); // <-- Usar un método para actualizar el texto

        loadSavedGames();
        
        renameButton.disableProperty().bind(savedGamesListView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(savedGamesListView.getSelectionModel().selectedItemProperty().isNull());
    }

    private void loadSavedGames() {
        savedGamesListView.setItems(FXCollections.observableArrayList(GameSaveService.getSavedGameNames()));
    }

    // --- CAMBIO CLAVE: Lógica de cambio de tema ---
    @FXML
    void handleThemeToggle(ActionEvent event) {
        boolean isSelected = darkThemeToggle.isSelected();
        MainApp.setTheme(isSelected); // Llama al método centralizado
        updateToggleButtonText(); // Actualiza el texto del botón
    }

    // --- NUEVO: Método para actualizar el texto del botón ---
    private void updateToggleButtonText() {
        if (darkThemeToggle.isSelected()) {
            darkThemeToggle.setText("Activado");
        } else {
            darkThemeToggle.setText("Desactivado");
        }
    }

    @FXML
    void handleRenameGame(ActionEvent event) {
        String selectedGame = savedGamesListView.getSelectionModel().getSelectedItem();
        if (selectedGame == null) return;

        TextInputDialog dialog = new TextInputDialog(selectedGame.replace(".sav", ""));
        dialog.setTitle("Renombrar Partida");
        dialog.setHeaderText("Renombrar '" + selectedGame + "'");
        dialog.setContentText("Introduce el nuevo nombre:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (GameSaveService.renameGame(selectedGame, newName)) {
                loadSavedGames();
            } else {
                showError("No se pudo renombrar la partida.");
            }
        });
    }

    @FXML
    void handleDeleteGame(ActionEvent event) {
        String selectedGame = savedGamesListView.getSelectionModel().getSelectedItem();
        if (selectedGame == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Eliminar Partida");
        confirmation.setHeaderText("¿Estás seguro de que quieres eliminar '" + selectedGame + "'?");
        confirmation.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (GameSaveService.deleteGame(selectedGame)) {
                loadSavedGames();
            } else {
                showError("No se pudo eliminar la partida.");
            }
        }
    }

    @FXML
    void handleBackButton(ActionEvent event) {
        MainApp.switchToMenuScreen();
    }

    private void showError(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }
}