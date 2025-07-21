package gamelab.Red;

import gamelab.Modelo.Partida;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorJuego {
    private Partida partida;
    private final List<ManejadorCliente> clientes = new ArrayList<>();
    private final ServerSocket serverSocket;

    /**
     * CORRECCIÓN: El constructor ahora acepta un 'int' para el puerto.
     * @param puerto El puerto en el que el servidor escuchará.
     * @throws IOException Si el puerto ya está en uso.
     */
    public ServidorJuego(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
        System.out.println("Servidor iniciado en puerto " + puerto + ". Esperando jugadores...");
    }

    /**
     * CORRECCIÓN: Este método ahora funcionará porque el constructor de ManejadorCliente será correcto.
     * Espera a que se conecten dos jugadores y luego inicia la partida.
     */
    public void esperarConexiones() throws IOException {
        // Inicia una partida nueva cuando el servidor está listo para aceptar conexiones.
        // Se asume que en multijugador no se juega contra el bot.
        this.partida = new Partida(false, "X"); 

        while (clientes.size() < 2) {
            Socket socketCliente = serverSocket.accept();
            String ficha = (clientes.isEmpty()) ? "X" : "O";
            
            // Esta llamada ahora coincidirá con el constructor corregido de ManejadorCliente.
            ManejadorCliente manejador = new ManejadorCliente(socketCliente, this, ficha);
            clientes.add(manejador);
            new Thread(manejador).start();
            System.out.println("Jugador conectado como: " + ficha);
        }
        System.out.println("¡Ambos jugadores conectados! Empezando la partida.");
        broadcastPartida();
    }

    public synchronized void procesarMovimiento(int fila, int col, String ficha) {
        if (partida.realizarMovimiento(fila, col)) {
            System.out.println("Movimiento válido de " + ficha + " en (" + fila + "," + col + ")");
            broadcastPartida();
        } else {
            System.out.println("Movimiento inválido de " + ficha + " en (" + fila + "," + col + ")");
        }
    }

    private void broadcastPartida() {
        System.out.println("Enviando estado de la partida a los clientes.");
        for (ManejadorCliente mc : clientes) {
            mc.enviarPartida(partida);
        }
    }
}