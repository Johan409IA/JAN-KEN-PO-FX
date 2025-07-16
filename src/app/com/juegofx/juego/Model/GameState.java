package app.com.juegofx.juego.Model;

import app.com.juegofx.juego.Model.Resultado;
import java.io.Serializable;
import java.util.List;

// La interfaz Serializable es crucial para que Java pueda escribir este objeto en un archivo.
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L; // Identificador de versión para la serialización

    private final int playerScore;
    private final int botScore;
    private final int currentRound;
    private final List<Resultado> roundResults;

    public GameState(int playerScore, int botScore, int currentRound, List<Resultado> roundResults) {
        this.playerScore = playerScore;
        this.botScore = botScore;
        this.currentRound = currentRound;
        this.roundResults = roundResults;
    }

    // Getters para que el GameLogic pueda leer los datos
    public int getPlayerScore() { return playerScore; }
    public int getBotScore() { return botScore; }
    public int getCurrentRound() { return currentRound; }
    public List<Resultado> getRoundResults() { return roundResults; }
}