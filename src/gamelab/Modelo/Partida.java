package gamelab.Modelo;

import java.io.Serializable;
import java.util.Random;

public class Partida implements Serializable {

    private static final long serialVersionUID = 2L; // Incrementamos la versión

    private final String[][] tablero;
    private String jugadorActual;
    private boolean juegoTerminado; // Se refiere al fin de la partida completa (5 rondas)
    private boolean rondaTerminada;
    
    private final int[] puntaje; // puntaje[0] para X, puntaje[1] para O
    private int rondaActual;
    private final int rondasTotales = 5;

    private final boolean contraBot;
    private final String jugadorHumano;
    private final String jugadorBot;

    // Constructor para una partida nueva
    public Partida(boolean contraBot, String fichaHumano) {
        this.tablero = new String[3][3];
        this.puntaje = new int[]{0, 0};
        this.rondaActual = 1;
        this.contraBot = contraBot;
        this.jugadorHumano = fichaHumano;
        this.jugadorBot = fichaHumano.equals("X") ? "O" : "X";
        iniciarNuevaRonda();
    }

    // --- Getters para que el Controlador lea el estado ---
    public String[][] getTablero() { return tablero; }
    public String getJugadorActual() { return jugadorActual; }
    public boolean isJuegoTerminado() { return juegoTerminado; }
    public boolean isRondaTerminada() { return rondaTerminada; }
    public int[] getPuntaje() { return puntaje; }
    public int getRondaActual() { return rondaActual; }
    public int getRondasTotales() { return rondasTotales; }
    public boolean isContraBot() { return contraBot; }
    public String getJugadorBot() { return jugadorBot; }

    // --- Lógica del Juego ---
    
    public final void iniciarNuevaRonda() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = "";
            }
        }
        this.jugadorActual = "X"; // X siempre empieza la ronda
        this.rondaTerminada = false;
    }
    
    public void avanzarSiguienteRonda() {
        if (rondaActual < rondasTotales) {
            rondaActual++;
            iniciarNuevaRonda();
        } else {
            juegoTerminado = true;
        }
    }

    public boolean realizarMovimiento(int fila, int col) {
        if (rondaTerminada || !tablero[fila][col].isEmpty()) {
            return false;
        }
        tablero[fila][col] = jugadorActual;
        verificarEstadoRonda();
        if (!rondaTerminada) {
            cambiarTurno();
        }
        return true;
    }
    
    // El movimiento del bot, ¡la nueva lógica!
    public int[] realizarMovimientoBot() {
        if (rondaTerminada || !jugadorActual.equals(jugadorBot)) {
            return null; // No es el turno del bot o la ronda terminó
        }
        
        // Lógica de IA muy simple: elige una casilla vacía al azar.
        Random rand = new Random();
        int fila, col;
        do {
            fila = rand.nextInt(3);
            col = rand.nextInt(3);
        } while (!tablero[fila][col].isEmpty());
        
        realizarMovimiento(fila, col);
        return new int[]{fila, col}; // Devuelve las coordenadas para que el controlador actualice la vista
    }

    private void cambiarTurno() {
        jugadorActual = (jugadorActual.equals("X")) ? "O" : "X";
    }

    private void verificarEstadoRonda() {
        // Verificar ganador
        for (int i = 0; i < 3; i++) {
            if (checkLine(tablero[i][0], tablero[i][1], tablero[i][2])) return;
            if (checkLine(tablero[0][i], tablero[1][i], tablero[2][i])) return;
        }
        if (checkLine(tablero[0][0], tablero[1][1], tablero[2][2])) return;
        if (checkLine(tablero[0][2], tablero[1][1], tablero[2][0])) return;

        // Verificar empate
        boolean tableroLleno = true;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tablero[i][j].isEmpty()) {
                    tableroLleno = false;
                    break;
                }
            }
        }
        if (tableroLleno) {
            this.rondaTerminada = true;
            // No se suma puntaje en empate
        }
    }

    private boolean checkLine(String s1, String s2, String s3) {
        if (!s1.isEmpty() && s1.equals(s2) && s2.equals(s3)) {
            this.rondaTerminada = true;
            // Actualizar puntaje
            if (s1.equals("X")) {
                puntaje[0]++;
            } else {
                puntaje[1]++;
            }
            // Verificar si el puntaje alcanza el límite para ganar la partida
            if (puntaje[0] > rondasTotales / 2 || puntaje[1] > rondasTotales / 2) {
                juegoTerminado = true;
            }
            return true;
        }
        return false;
    }
}