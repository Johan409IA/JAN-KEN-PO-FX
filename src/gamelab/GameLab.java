
package gamelab;

import gamelab.Controladores.MainController;
import gamelab.Modelo.Preferencias;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
/**
 *
 * @author Johan Castillon
 */
public class GameLab extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent ruta = FXMLLoader.load(getClass().getResource("Main.fxml"));
        Preferencias prefs = cargarPreferencias();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
        Parent ruta = loader.load();
        MainController mainController = loader.getController(); // Obtenemos una instancia del controlador
        // 2. Establecer el ícono de salida correcto ANTES de mostrar la escena
        mainController.setExitIconForTheme(prefs.getTema());
        
        Scene escena = new Scene(ruta);
        escena.getStylesheets().add(getClass().getResource("main.css").toExternalForm()); // Agregamos hojas de estilo
        primaryStage.setScene(escena);
        primaryStage.setTitle("GameLab");// nombre de la ventana
        primaryStage.show();

        // --- INICIO DE LA MODIFICACIÓN ---
        // 1. Cargar las preferencias antes de mostrar nada
        
        
        

        
        
        // 2. Aplicar la hoja de estilos correcta BASADA en las preferencias
        aplicarTema(escena, prefs.getTema());

        // --- FIN DE LA MODIFICACIÓN ---
        primaryStage.setScene(escena);
        primaryStage.setTitle("GameLab");
        primaryStage.show();
        
        
        
        
        
    }

    /**
     * Carga las preferencias del usuario desde el archivo. Si no existe,
     * devuelve unas preferencias por defecto.
     *
     * @return El objeto Preferencias cargado o uno nuevo.
     */
    private Preferencias cargarPreferencias() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("preferencias.dat"))) {
            return (Preferencias) ois.readObject();
        } catch (Exception e) {
            System.out.println("No se pudo cargar el archivo de preferencias, usando valores por defecto.");
            return new Preferencias(); // Devuelve las preferencias por defecto
        }
    }

    /**
     * Aplica la hoja de estilos correspondiente a una escena.
     *
     * @param scene La escena a la que se aplicará el estilo.
     * @param tema El nombre del tema (ej. "Claro", "Oscuro").
     */
    public static void aplicarTema(Scene scene, String tema) {
        scene.getStylesheets().clear(); // Limpiamos cualquier estilo anterior
        if (Preferencias.TEMA_OSCURO.equals(tema)) {
            scene.getStylesheets().add(GameLab.class.getResource("dark-theme.css").toExternalForm());
            System.out.println("Tema Oscuro aplicado.");
        } else {
            scene.getStylesheets().add(GameLab.class.getResource("main.css").toExternalForm());
            System.out.println("Tema Claro (por defecto) aplicado.");
        }
    }
}
 
 
