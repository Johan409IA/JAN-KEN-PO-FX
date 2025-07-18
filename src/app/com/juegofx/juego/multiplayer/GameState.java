package app.com.juegofx.juego.multiplayer;

import java.io.Serializable;

public class GameState implements Serializable {
    private static final long serialVersionUID = 2L; // Incrementamos versión por los nuevos campos
    private int rondaActual;
    private int miPuntuacion;
    private int puntuacionOponente;
    private boolean rondaTerminada = false;
    private Eleccion eleccionOponente;
    private Resultado resultadoRonda;

    // --- NUEVOS CAMPOS ---
    private boolean isGameOver = false;
    private String finalMessage = "";

    public GameState(int ronda, int miPuntuacion, int puntuacionOponente) {
        this.rondaActual = ronda;
        this.miPuntuacion = miPuntuacion;
        this.puntuacionOponente = puntuacionOponente;
    }

    // Getters
    public int getRondaActual() { return rondaActual; }
    public int getMiPuntuacion() { return miPuntuacion; }
    public int getPuntuacionOponente() { return puntuacionOponente; }
    public boolean isRondaTerminada() { return rondaTerminada; }
    public Eleccion getEleccionOponente() { return eleccionOponente; }
    public Resultado getResultadoRonda() { return resultadoRonda; }
    public boolean isGameOver() { return isGameOver; }
    public String getFinalMessage() { return finalMessage; }
    
    // Setters usados por el servidor
    public void setRondaTerminada(boolean terminada, Eleccion oponente, Resultado resultado) {
        this.rondaTerminada = terminada;
        this.eleccionOponente = oponente;
        this.resultadoRonda = resultado;
    }

    public void setGameOver(boolean gameOver, String message) {
        this.isGameOver = gameOver;
        this.finalMessage = message;
    }
}