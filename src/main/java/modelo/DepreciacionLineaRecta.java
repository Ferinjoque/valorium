package modelo;

public class DepreciacionLineaRecta extends Depreciacion {

    public DepreciacionLineaRecta(double costoInicial, int vidaUtil) {
        super(costoInicial, vidaUtil);
    }

    @Override
    public double calcularDepreciacion() {
        return costoInicial / vidaUtil;
    }
}