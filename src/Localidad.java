import java.util.LinkedList;

public class Localidad {

    private String nombre;
    private String provincia;
    private LinkedList<Prediccion> predicciones;

    public Localidad(String nombre, String provincia, LinkedList<Prediccion> predicciones) {
        this.nombre = nombre;
        this.provincia = provincia;
        this.predicciones = predicciones;
    }

    public String getNombre() {
        return nombre;
    }

    public String getProvincia() {
        return provincia;
    }

    public LinkedList<Prediccion> getPredicciones() {
        return predicciones;
    }
}
