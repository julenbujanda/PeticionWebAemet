public class Prediccion {

    private String fecha;
    private int tempMaxima;
    private int tempMinima;
    private int estadoCielo;


    public Prediccion(String fecha, int tempMaxima, int tempMinima, int estadoCielo) {
        this.fecha = fecha;
        this.tempMaxima = tempMaxima;
        this.tempMinima = tempMinima;
        this.estadoCielo = estadoCielo;
    }

    public String getFecha() {
        return fecha;
    }

    public int getTempMaxima() {
        return tempMaxima;
    }

    public int getTempMinima() {
        return tempMinima;
    }

    public int getEstadoCielo() {
        return estadoCielo;
    }

}
