package app.com.juegofx.juego.multiplayer;

import java.io.Serializable;

public enum Resultado implements Serializable {
    GANASTE("¡Ganaste la ronda!"),
    PERDISTE("Perdiste la ronda..."),
    EMPATE("¡Es un empate!");

    private final String mensaje;
    Resultado(String msg) { this.mensaje = msg; }
    public String getMensaje() { return mensaje; }
}