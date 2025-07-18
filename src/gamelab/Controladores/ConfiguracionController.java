package gamelab.Controladores;

import gamelab.Modelo.Preferencias;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ConfiguracionController implements Initializable {

    // --- Componentes de la Vista ---
    @FXML private ComboBox<String> comboTema;
    @FXML private RadioButton radioFichaX;
    @FXML private RadioButton radioFichaO;
    @FXML private ToggleGroup fichaToggleGroup;
    @FXML private Label lblStatus;

    // --- Variables de Lógica ---
    private Preferencias preferenciasActuales;
    private MainController mainController; // Referencia para notificar cambios
    private final String RUTA_ARCHIVO = "preferencias.dat";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        comboTema.getItems().addAll(Preferencias.TEMA_CLARO, Preferencias.TEMA_OSCURO);
        cargarPreferencias();
        actualizarVista();
    }
    
    /**
     * Permite que el controlador principal se identifique para poder notificarle los cambios.
     * @param mainController La instancia del MainController.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void cargarPreferencias() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RUTA_ARCHIVO))) {
            preferenciasActuales = (Preferencias) ois.readObject();
        } catch (Exception e) {
            System.out.println("Archivo de preferencias no encontrado o corrupto. Creando nuevas por defecto.");
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

    @FXML
    private void handleGuardar(ActionEvent event) {
        // 1. Recoger los nuevos valores de la vista
        preferenciasActuales.setTema(comboTema.getValue());
        RadioButton selectedRadio = (RadioButton) fichaToggleGroup.getSelectedToggle();
        preferenciasActuales.setFichaPreferida(selectedRadio.getText().contains("X") ? Preferencias.FICHA_X : Preferencias.FICHA_O);

        // 2. Guardar el objeto de preferencias en el archivo
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_ARCHIVO))) {
            oos.writeObject(preferenciasActuales);
            System.out.println("Preferencias guardadas exitosamente.");
            
            // 3. Notificar al MainController para que actualice su propia vista
            if (mainController != null) {
                mainController.actualizarVistaDesdePreferencias(preferenciasActuales);
            }

            cerrarVentana(event);

        } catch (IOException e) {
            e.printStackTrace();
            lblStatus.setText("Error al guardar.");
        }
    }

    @FXML
    private void handleCancelar(ActionEvent event) {
        cerrarVentana(event);
    }

    private void cerrarVentana(ActionEvent event) {
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.close();
    }
}