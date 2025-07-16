package app.com.juegofx.juego.Model;

public enum Eleccion {
    PIEDRA,
    PAPEL,
    TIJERA;

    // Puedes añadir un método para obtener el nombre legible si lo necesitas
    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}