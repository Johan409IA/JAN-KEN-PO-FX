

package gamelab.Modelo;

import java.io.Serializable;

/**
 * Entidad que representa las preferencias del usuario.
 * Implementa Serializable para poder guardarla y cargarla fácilmente desde un archivo.
 */
public class Preferencias implements Serializable {

    // Constantes para los temas, para evitar errores de tipeo.
    public static final String TEMA_CLARO = "Claro";
    public static final String TEMA_OSCURO = "Oscuro";
    
    // Constantes para las fichas.
    public static final String FICHA_X = "X";
    public static final String FICHA_O = "O";

    private String tema;
    private String fichaPreferida;
    // Puedes añadir más campos aquí en el futuro (ej: private boolean sonidoActivado;)

    /**
     * Constructor por defecto con valores predeterminados.
     * Se usará si no existe un archivo de configuración previo.
     */
    public Preferencias() {
        this.tema = TEMA_CLARO; // Tema por defecto
        this.fichaPreferida = FICHA_X; // Ficha por defecto
    }

    // --- Getters y Setters ---
    
    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getFichaPreferida() {
        return fichaPreferida;
    }

    public void setFichaPreferida(String fichaPreferida) {
        this.fichaPreferida = fichaPreferida;
    }
}
