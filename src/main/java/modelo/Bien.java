package modelo;

abstract class Bien {

    protected String nombre;
    protected String categoria;
    protected double costoInicial;
    protected String estado;

    public Bien(String nombre, String categoria, double costoInicial) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.costoInicial = costoInicial;
        this.estado = "Alta";
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getCostoInicial() {
        return costoInicial;
    }

    public String getEstado() {
        return estado;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setCostoInicial(double costoInicial) {
        this.costoInicial = costoInicial;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public abstract double calcularValorResidual();
}