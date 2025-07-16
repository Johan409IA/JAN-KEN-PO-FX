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
        // Cargar el estado actual del tema
        boolean isDarkTheme = MainApp.isDarkThemeActive();
        darkThemeToggle.setSelected(isDarkTheme);
        darkThemeToggle.setText(isDarkTheme ? "Activado" : "Desactivado");

        // Poblar la lista de partidas guardadas
        loadSavedGames();
        
        // Deshabilitar botones si no hay nada seleccionado
        renameButton.disableProperty().bind(savedGamesListView.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(savedGamesListView.getSelectionModel().selectedItemProperty().isNull());
    }

    private void loadSavedGames() {
        savedGamesListView.setItems(FXCollections.observableArrayList(GameSaveService.getSavedGameNames()));
    }

    @FXML
    void handleThemeToggle(ActionEvent event) {
        boolean isSelected = darkThemeToggle.isSelected();
        MainApp.setDarkTheme(isSelected);
        darkThemeToggle.setText(isSelected ? "Activado" : "Desactivado");
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
                loadSavedGames(); // Refrescar la lista
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
                loadSavedGames(); // Refrescar la lista
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