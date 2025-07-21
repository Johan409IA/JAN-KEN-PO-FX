package gamelab.Red;

import gamelab.Modelo.Partida;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class ClienteJuego {
    private ObjectOutputStream out;
    private String miFicha;
    private Consumer<Partida> updateListener;

    public ClienteJuego(String host, int puerto) throws IOException {
        Socket socket = new Socket(host, puerto);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        new Thread(() -> escucharServidor(socket)).start();
    }

    private void escucharServidor(Socket socket) {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            this.miFicha = (String) in.readObject();
            
            while (true) {
                Partida partidaActualizada = (Partida) in.readObject();
                if (updateListener != null) {
                    Platform.runLater(() -> updateListener.accept(partidaActualizada));
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

    public void enviarMovimiento(int fila, int col) throws IOException {
        out.writeObject(new int[]{fila, col});
    }
    
    public void setUpdateListener(Consumer<Partida> listener) { this.updateListener = listener; }
    public String getMiFicha() { return miFicha; }
}