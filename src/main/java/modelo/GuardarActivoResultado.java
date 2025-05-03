package modelo;

public class GuardarActivoResultado {
    private int id;
    private String codigoPatrimonial;

    public GuardarActivoResultado(int id, String codigoPatrimonial) {
        this.id = id;
        this.codigoPatrimonial = codigoPatrimonial;
    }

    public int getId() {
        return id;
    }

    public String getCodigoPatrimonial() {
        return codigoPatrimonial;
    }
}