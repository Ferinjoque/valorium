package modelo;

public abstract class Depreciacion {

    protected double costoInicial;
    protected int vidaUtil;

    public Depreciacion(double costoInicial, int vidaUtil) {
        this.costoInicial = costoInicial;
        this.vidaUtil = vidaUtil;
    }

    public abstract double calcularDepreciacion();

}