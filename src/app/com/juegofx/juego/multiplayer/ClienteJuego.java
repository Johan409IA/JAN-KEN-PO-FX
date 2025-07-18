package app.com.juegofx.juego.multiplayer;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class ClienteJuego {
    private ObjectOutputStream out;
    private Consumer<GameState> updateListener;

    public ClienteJuego(String host, int puerto) throws IOException {
        Socket socket = new Socket(host, puerto);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        new Thread(() -> escucharServidor(socket)).start();
    }

    private void escucharServidor(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            while (true) {
                GameState estado = (GameState) in.readObject();
                if (updateListener != null) {
                    Platform.runLater(() -> updateListener.accept(estado));
                }
            }
        } catch (Exception e) {
            Platform.runLater(() -> {
               Alert alert = new Alert(Alert.AlertType.ERROR, "Se perdió la conexión con el servidor.");
               alert.showAndWait();
               Platform.exit();
            });
        }
    }

    public void enviarEleccion(Eleccion eleccion) throws IOException {
        out.writeObject(eleccion);
    }
    
    public void setUpdateListener(Consumer<GameState> listener) {
        this.updateListener = listener;
    }
}