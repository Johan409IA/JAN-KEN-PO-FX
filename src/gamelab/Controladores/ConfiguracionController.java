/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gamelab.Controladores;

import gamelab.Modelo.Preferencias;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import gamelab.GameLab;
import javafx.scene.Scene;


public class ConfiguracionController implements Initializable {

    // --- Componentes de la Vista ---
    @FXML private ComboBox<String> comboTema;
    @FXML private RadioButton radioFichaX;
    @FXML private RadioButton radioFichaO;
    @FXML private ToggleGroup fichaToggleGroup;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblStatus;
    
    
    private MainController mainController;
    //private MainController mainController;
    private Scene mainScene; // Variable para guardar la referencia a la escena principal

    private Preferencias preferenciasActuales;
    private final String RUTA_ARCHIVO = "preferencias.dat"; // Archivo donde se guardarán los datos
    
    // --- NUEVO MÉTODO SETTER ---
    /**
     * Establece la escena principal para que este controlador pueda modificarla.
     * @param scene La escena del menú principal.
     */
    public void setMainScene(Scene scene) {
        this.mainScene = scene;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // 1. Configurar las opciones disponibles en los controles
        comboTema.getItems().addAll(Preferencias.TEMA_CLARO, Preferencias.TEMA_OSCURO);
        
        // 2. Cargar las preferencias existentes
        cargarPreferencias();
        
        // 3. Mostrar las preferencias cargadas en la interfaz
        actualizarVista();
    }

    private void cargarPreferencias() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_ARCHIVO))) {
            preferenciasActuales = (Preferencias) ois.readObject();
            System.out.println("Preferencias cargadas exitosamente.");
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de preferencias no encontrado. Creando nuevas por defecto.");
            preferenciasActuales = new Preferencias();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al leer el archivo de preferencias. Usando valores por defecto.");
            e.printStackTrace();
            preferenciasActuales = new Preferencias();
        }
    }

    private void actualizarVista() {
        comboTema.setValue(preferenciasActuales.getTema());
        if (Preferencias.FICHA_X.equals(preferenciasActuales.getFichaPreferida())) {
            radioFichaX.setSelected(true);
        } else {
            radioFichaO.setSelected(true);
        }
    }
    
     // --- NUEVO MÉTODO ---
    /**
     * Permite que el controlador que abre esta ventana (MainController)
     * se identifique a sí mismo.
     * @param mainController La instancia del controlador principal.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleGuardar() {
        // 1. Recoger los nuevos valores de la vista
        preferenciasActuales.setTema(comboTema.getValue());
        
        RadioButton selectedRadio = (RadioButton) fichaToggleGroup.getSelectedToggle();
        if (selectedRadio.equals(radioFichaX)) {
            preferenciasActuales.setFichaPreferida(Preferencias.FICHA_X);
        } else {
            preferenciasActuales.setFichaPreferida(Preferencias.FICHA_O);
        }

        // 2. Guardar el objeto de preferencias en el archivo
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(preferenciasActuales);
            System.out.println("Preferencias guardadas exitosamente en " + RUTA_ARCHIVO);
            
            // 3. Aplicar tema si es necesario (ejemplo) y cerrar la ventana
            // --- INICIO DE LA MODIFICACIÓN ---
            
            if (mainScene != null) {
                GameLab.aplicarTema(mainScene, preferenciasActuales.getTema());
                System.out.println("Tema CSS aplicado directamente a la escena principal.");
            } else {
                System.err.println("Error: La referencia a mainScene es nula. No se pudo cambiar el tema.");
            }

            // 2. Notificar al MainController que actualice su ícono (esto se mantiene igual)
            if (mainController != null) {
                mainController.setExitIconForTheme(preferenciasActuales.getTema());
            }
            cerrarVentana();

        } catch (IOException e) {
            System.err.println("Error al guardar las preferencias.");
            e.printStackTrace();
            lblStatus.setText("Error al guardar.");
        }
    }
    
    // Este método es un ejemplo de cómo podrías aplicar el tema.
    // Necesitaría acceso a la escena principal para funcionar.
    private void aplicarTema(String tema) {
        System.out.println("Aplicando tema: " + tema);
        // Aquí iría la lógica para cambiar el CSS de la aplicación dinámicamente.
        // Por ejemplo:
        // Stage stage = (Stage) btnGuardar.getScene().getWindow();
        // stage.getOwner().getScene().getStylesheets().clear();
        // if(tema.equals(Preferencias.TEMA_OSCURO)) {
        //    stage.getOwner().getScene().getStylesheets().add(getClass().getResource("dark-theme.css").toExternalForm());
        // } else {
        //    stage.getOwner().getScene().getStylesheets().add(getClass().getResource("main.css").toExternalForm());
        // }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        // Obtenemos el Stage (la ventana) actual y la cerramos.
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
