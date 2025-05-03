package modelo;

import java.util.Objects;

public class Sede {
    private int id;
    private String nombre;
    private String direccion;

    public Sede(int id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sede sede = (Sede) o;
        return Objects.equals(nombre, sede.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }
}