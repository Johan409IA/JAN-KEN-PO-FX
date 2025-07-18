package app.com.juegofx.juego.multiplayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorJuego {
    private ManejadorCliente jugador1, jugador2;
    private int rondaActual = 1;
    private int puntaje1 = 0, puntaje2 = 0;
    private Eleccion eleccion1, eleccion2;

    // --- NUEVAS CONSTANTES ---
    private final int MAX_ROUNDS = 5;
    private final int WIN_CONDITION = 3;

    public ServidorJuego(int puerto) throws IOException {
        // ... (El constructor no cambia)
        ServerSocket serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor iniciado...");
        Socket s1 = serverSocket.accept();
        this.jugador1 = new ManejadorCliente(s1, this);
        new Thread(jugador1).start();
        System.out.println("Jugador 1 conectado.");
        Socket s2 = serverSocket.accept();
        this.jugador2 = new ManejadorCliente(s2, this);
        new Thread(jugador2).start();
        System.out.println("Jugador 2 conectado. Empezando partida!");
        broadcastEstadoInicial();
    }

    public synchronized void recibirEleccion(ManejadorCliente jugador, Eleccion eleccion) {
        if (jugador == jugador1) eleccion1 = eleccion;
        else if (jugador == jugador2) eleccion2 = eleccion;

        if (eleccion1 != null && eleccion2 != null) {
            procesarRonda();
        }
    }

    private void procesarRonda() {
        Resultado resultado1 = determinarResultado(eleccion1, eleccion2);
        if (resultado1 == Resultado.GANASTE) puntaje1++;
        else if (resultado1 == Resultado.PERDISTE) puntaje2++;

        GameState estadoPara1 = new GameState(rondaActual, puntaje1, puntaje2);
        estadoPara1.setRondaTerminada(true, eleccion2, resultado1);
        
        GameState estadoPara2 = new GameState(rondaActual, puntaje2, puntaje1);
        estadoPara2.setRondaTerminada(true, eleccion1, determinarResultado(eleccion2, eleccion1));

        // --- LÓGICA DE FIN DE JUEGO ---
        if (puntaje1 >= WIN_CONDITION || puntaje2 >= WIN_CONDITION || rondaActual >= MAX_ROUNDS) {
            String msg1, msg2;
            if (puntaje1 > puntaje2) {
                msg1 = "¡FELICIDADES, HAS GANADO EL JUEGO!";
                msg2 = "Has perdido el juego. ¡Mejor suerte la próxima!";
            } else if (puntaje2 > puntaje1) {
                msg1 = "Has perdido el juego. ¡Mejor suerte la próxima!";
                msg2 = "¡FELICIDADES, HAS GANADO EL JUEGO!";
            } else {
                msg1 = "¡El juego ha terminado en empate!";
                msg2 = "¡El juego ha terminado en empate!";
            }
            estadoPara1.setGameOver(true, msg1);
            estadoPara2.setGameOver(true, msg2);
        }

        jugador1.enviarEstado(estadoPara1);
        jugador2.enviarEstado(estadoPara2);
        
        rondaActual++;
        eleccion1 = null;
        eleccion2 = null;
    }

    private void broadcastEstadoInicial() {
        jugador1.enviarEstado(new GameState(rondaActual, puntaje1, puntaje2));
        jugador2.enviarEstado(new GameState(rondaActual, puntaje2, puntaje1));
    }
    
    private Resultado determinarResultado(Eleccion p1, Eleccion p2) {
        if (p1 == p2) return Resultado.EMPATE;
        if ((p1 == Eleccion.PIEDRA && p2 == Eleccion.TIJERA) ||
            (p1 == Eleccion.PAPEL && p2 == Eleccion.PIEDRA) ||
            (p1 == Eleccion.TIJERA && p2 == Eleccion.PAPEL)) {
            return Resultado.GANASTE;
        }
        return Resultado.PERDISTE;
    }
}

// Hilo para manejar a cada cliente
class ManejadorCliente implements Runnable {
    private Socket socket;
    private ServidorJuego servidor;
    private ObjectOutputStream out;

    public ManejadorCliente(Socket s, ServidorJuego serv) throws IOException {
        this.socket = s;
        this.servidor = serv;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while(true) {
                Eleccion eleccion = (Eleccion) in.readObject();
                servidor.recibirEleccion(this, eleccion);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cliente desconectado: " + e.getMessage());
        }
    }

    public void enviarEstado(GameState estado) {
        try {
            out.writeObject(estado);
            out.reset();
        } catch (IOException e) { System.out.println("Error enviando estado al cliente."); }
    }
}