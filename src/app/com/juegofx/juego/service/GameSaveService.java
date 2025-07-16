package app.com.juegofx.juego.service;

import app.com.juegofx.juego.Model.GameState;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameSaveService {

    // Directorio de guardado dentro de la carpeta del usuario (ej. C:/Users/TuUsuario/JanKenPoSaves)
    private static final Path SAVE_DIRECTORY = Paths.get(System.getProperty("user.dir"), "JanKenPoSaves");
    private static final String SAVE_FILE_EXTENSION = ".sav";

    // Asegurarse de que el directorio de guardado exista
    static {
        try {
            if (!Files.exists(SAVE_DIRECTORY)) {
                Files.createDirectories(SAVE_DIRECTORY);
                System.out.println("Directorio de guardado creado en: " + SAVE_DIRECTORY.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error fatal: no se pudo crear el directorio de guardado.");
            e.printStackTrace();
        }
    }

    // --- NUEVO: Guardado automático ---
    public static void saveGameAutomatically(GameState state) {
        // Crear un nombre de archivo único basado en la fecha y hora
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String fileName = "partida_" + timestamp + SAVE_FILE_EXTENSION;
        Path savePath = SAVE_DIRECTORY.resolve(fileName);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(savePath.toFile()))) {
            oos.writeObject(state);
            // Mostrar una confirmación al usuario
            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Partida Guardada");
            confirmation.setHeaderText(null);
            confirmation.setContentText("¡La partida se ha guardado correctamente como '" + fileName + "'!");
            confirmation.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error al Guardar");
            error.setHeaderText("No se pudo guardar la partida.");
            error.setContentText("Ocurrió un error al intentar escribir el archivo de guardado.");
            error.showAndWait();
        }
    }
     // --- ¡NUEVO! Obtener nombres de partidas para la lista ---
    public static List<String> getSavedGameNames() {
        try {
            return Files.list(SAVE_DIRECTORY)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.endsWith(SAVE_FILE_EXTENSION))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    // --- ¡NUEVO! Renombrar una partida guardada ---
    public static boolean renameGame(String oldName, String newName) {
        if (newName == null || newName.isBlank() || !oldName.endsWith(SAVE_FILE_EXTENSION)) {
            return false;
        }
        
        // Asegurarse de que el nuevo nombre tenga la extensión correcta
        if (!newName.endsWith(SAVE_FILE_EXTENSION)) {
            newName += SAVE_FILE_EXTENSION;
        }
        
        Path oldPath = SAVE_DIRECTORY.resolve(oldName);
        Path newPath = SAVE_DIRECTORY.resolve(newName);

        try {
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- ¡NUEVO! Eliminar una partida guardada ---
    public static boolean deleteGame(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        Path filePath = SAVE_DIRECTORY.resolve(fileName);
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NUEVO: Cargar juego desde una lista ---
    public static GameState loadGameFromList() {
        List<String> savedGames = getSavedGameNames();

        if (savedGames.isEmpty()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Sin Partidas Guardadas");
            info.setHeaderText(null);
            info.setContentText("No se encontraron partidas guardadas para reanudar.");
            info.showAndWait();
            return null;
        }

        // Mostrar un diálogo con las partidas guardadas
        ChoiceDialog<String> dialog = new ChoiceDialog<>(savedGames.get(0), savedGames);
        dialog.setTitle("Reanudar Partida");
        dialog.setHeaderText("Selecciona una partida para reanudar");
        dialog.setContentText("Partidas guardadas:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            Path filePath = SAVE_DIRECTORY.resolve(result.get());
            return loadGameStateFromFile(filePath.toFile());
        }

        return null; // El usuario canceló
    }


    private static GameState loadGameStateFromFile(File file) {
        if (file != null && file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (GameState) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                // Aquí podrías mostrar un Alert de error al usuario
                return null;
            }
        }
        return null;
    }
}