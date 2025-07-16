package app.com.juegofx.juego.Model;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LogicaJuego {

    private final Random random = new Random();
    private int playerScore;
    private int botScore;
    private int currentRound;
    private final int MAX_ROUNDS = 5;
    private final int WIN_CONDITION = 3;

    private List<Resultado> roundResults;

    public LogicaJuego() {
        resetGame();
    }

    public void resetGame() {
        playerScore = 0;
        botScore = 0;
        currentRound = 0;
        roundResults = new ArrayList<>();
    }
    
    // --- NUEVO: Cargar estado desde un objeto GameState ---
    public void loadState(GameState state) {
        this.playerScore = state.getPlayerScore();
        this.botScore = state.getBotScore();
        this.currentRound = state.getCurrentRound();
        this.roundResults = new ArrayList<>(state.getRoundResults());
    }

    // --- NUEVO: Exportar estado a un objeto GameState ---
    public GameState saveState() {
        return new GameState(playerScore, botScore, currentRound, roundResults);
    }

    public Eleccion getBotChoice() {
        return Eleccion.values()[random.nextInt(Eleccion.values().length)];
    }

    public void playRound(Eleccion playerChoice, Eleccion botChoice) {
        currentRound++;
        Resultado result;

        if (playerChoice == botChoice) {
            result = Resultado.EMPATE;
        } else {
            switch (playerChoice) {
                case PIEDRA: result = (botChoice == Eleccion.TIJERA) ? Resultado.GANASTE : Resultado.PERDISTE; break;
                case PAPEL:  result = (botChoice == Eleccion.PIEDRA) ? Resultado.GANASTE : Resultado.PERDISTE; break;
                case TIJERA: result = (botChoice == Eleccion.PAPEL) ? Resultado.GANASTE : Resultado.PERDISTE; break;
                default:     result = Resultado.EMPATE;
            }
        }

        if (result == Resultado.GANASTE) playerScore++;
        if (result == Resultado.PERDISTE) botScore++;

        roundResults.add(result);
    }

    public boolean isGameOver() {
        return currentRound >= MAX_ROUNDS || playerScore >= WIN_CONDITION || botScore >= WIN_CONDITION;
    }
    
    // El resto de los métodos getters y de lógica permanecen igual...
    public String getRoundResultText(Eleccion playerChoice, Eleccion botChoice) {
        Resultado lastResult = roundResults.get(roundResults.size() - 1);
        switch (lastResult) {
            case GANASTE: return "¡Ganas la ronda!";
            case PERDISTE: return "El Bot gana la ronda.";
            case EMPATE: return "¡Ronda de Empate!";
            default: return "";
        }
    }

    public String getGameWinnerMessage() {
        if (!isGameOver()) return "El juego aún no ha terminado.";
        if (playerScore > botScore) return "¡Felicidades, has ganado el juego!";
        if (botScore > playerScore) return "¡El Bot ha ganado el juego!";
        return "¡El juego ha terminado en empate!";
    }

    public int getPlayerScore() { return playerScore; }
    public int getBotScore() { return botScore; }
    public List<Resultado> getRoundResults() { return roundResults; }
    public int getCurrentRound() { return currentRound; }
}