package gamelab.Modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Partida implements Serializable {

     private static final long serialVersionUID = 6L; // Incrementamos versión por los cambios

    private final String[][] tablero;
    private String jugadorActual;
    private boolean juegoTerminado;
    private boolean rondaTerminada;
    
    // puntaje[0]=X, puntaje[1]=O, puntaje[2]=Empates
    private final int[] puntaje; // puntaje[0]=X, puntaje[1]=O, puntaje[2]=Empates
    private int rondaActual;
    private final int rondasTotales = 5;

    // --- NUEVO: El historial de rondas ahora es parte de la entidad ---
    private final List<String> resultadosRondas;

    private final boolean contraBot;
    private final String jugadorHumano;
    private final String jugadorBot;

    public Partida(boolean contraBot, String fichaHumano) {
        this.tablero = new String[3][3];
        this.puntaje = new int[]{0, 0, 0};
        this.rondaActual = 1;
        this.resultadosRondas = new ArrayList<>(); // Lista vacía al empezar
        this.contraBot = contraBot;
        this.jugadorHumano = fichaHumano;
        this.jugadorBot = fichaHumano.equals("X") ? "O" : "X";
        iniciarNuevaRonda(true);
    }

    // --- Getters ---
    public String[][] getTablero() { return tablero; }
    public String getJugadorActual() { return jugadorActual; }
    public boolean isJuegoTerminado() { return juegoTerminado; }
    public boolean isRondaTerminada() { return rondaTerminada; }
    public int[] getPuntaje() { return puntaje; }
    public int getRondaActual() { return rondaActual; }
    public int getRondasTotales() { return rondasTotales; }
    public List<String> getResultadosRondas() { return resultadosRondas; } // Getter para la lista
    public boolean isContraBot() { return contraBot; }
    public String getJugadorBot() { return jugadorBot; }
    public String getJugadorHumano() { return jugadorHumano; }
    public String getGanadorFinal() {
        if (puntaje[0] > puntaje[1]) return "X";
        if (puntaje[1] > puntaje[0]) return "O";
        return "Empate";
    }

    // --- Lógica del Juego ---
    
    public final void iniciarNuevaRonda(boolean esPrimeraRonda) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tablero[i][j] = "";
            }
        }
        if (esPrimeraRonda) {
            this.jugadorActual = (new Random().nextBoolean()) ? "X" : "O";
        } else {
            this.jugadorActual = this.jugadorActual.equals("X") ? "O" : "X";
        }
        this.rondaTerminada = false;
    }
    
    public void avanzarSiguienteRonda() {
        if (rondaActual < rondasTotales && !juegoTerminado) {
            rondaActual++;
            iniciarNuevaRonda(false);
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
    
    public int[] realizarMovimientoBot() {
        if (rondaTerminada || !jugadorActual.equals(jugadorBot)) return null;
        Random rand = new Random();
        int fila, col;
        if (estaTableroLleno()) return null;
        do {
            fila = rand.nextInt(3);
            col = rand.nextInt(3);
        } while (!tablero[fila][col].isEmpty());
        
        realizarMovimiento(fila, col);
        return new int[]{fila, col};
    }

    private void cambiarTurno() {
        jugadorActual = (jugadorActual.equals("X")) ? "O" : "X";
    }

    private void verificarEstadoRonda() {
        for (int i = 0; i < 3; i++) {
            if (checkLine(tablero[i][0], tablero[i][1], tablero[i][2])) return;
            if (checkLine(tablero[0][i], tablero[1][i], tablero[2][i])) return;
        }
        if (checkLine(tablero[0][0], tablero[1][1], tablero[2][2])) return;
        if (checkLine(tablero[0][2], tablero[1][1], tablero[2][0])) return;

        if (estaTableroLleno()) {
            this.rondaTerminada = true;
            puntaje[2]++;
            resultadosRondas.add("Empate"); // Guardar resultado
            verificarFinDePartidaPorPuntaje();
        }
    }

    private boolean checkLine(String s1, String s2, String s3) {
        if (!s1.isEmpty() && s1.equals(s2) && s2.equals(s3)) {
            this.rondaTerminada = true;
            if (s1.equals("X")) puntaje[0]++; else puntaje[1]++;
            resultadosRondas.add(s1); // Guardar resultado (el ganador de la ronda)
            verificarFinDePartidaPorPuntaje();
            return true;
        }
        return false;
    }
    
    private void verificarFinDePartidaPorPuntaje() {
        int rondasRestantes = rondasTotales - rondaActual;
        if (puntaje[0] > puntaje[1] + rondasRestantes || puntaje[1] > puntaje[0] + rondasRestantes) {
            juegoTerminado = true;
        }
    }

    private boolean estaTableroLleno() {
        for (String[] fila : tablero) {
            for (String celda : fila) {
                if (celda.isEmpty()) return false;
            }
        }
        return true;
    }
}