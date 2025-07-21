package gamelab.Red;

import gamelab.Modelo.Partida;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ManejadorCliente implements Runnable {
    private final Socket socket;
    private final ServidorJuego servidor;
    private final String ficha;
    private ObjectOutputStream out;

    /**
     * CORRECCIÓN: El constructor ahora acepta los 3 argumentos correctamente.
     * @param socket El socket del cliente conectado.
     * @param servidor La referencia al servidor principal.
     * @param ficha La ficha asignada a este cliente ("X" o "O").
     */
    public ManejadorCliente(Socket socket, ServidorJuego servidor, String ficha) {
        this.socket = socket;
        this.servidor = servidor;
        this.ficha = ficha;
    }

    @Override
    public void run() {
        try {
            // Inicializamos los flujos de datos aquí, dentro del hilo.
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            
            // Lo primero que hacemos es enviar al cliente la ficha que le ha sido asignada.
            out.writeObject(ficha); 
            out.flush();

            // Bucle infinito para escuchar los movimientos del cliente.
            while (true) {
                int[] movimiento = (int[]) in.readObject();
                servidor.procesarMovimiento(movimiento[0], movimiento[1], this.ficha);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Jugador " + ficha + " desconectado.");
            // Aquí podrías añadir lógica para manejar la desconexión de un jugador.
        }
    }
    
    public void enviarPartida(Partida partida) {
        try {
            if (out != null) {
                out.writeObject(partida);
                out.reset(); // Importante para que se envíe el estado actualizado del objeto.
            }
        } catch (IOException e) {
            System.out.println("Error al enviar partida a " + ficha);
        }
    }
}