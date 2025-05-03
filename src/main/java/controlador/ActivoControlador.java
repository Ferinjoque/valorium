package controlador;

import modelo.ActivoFijo;
import modelo.Sede;
import servicio.ActivoServicio;

import java.util.List;

public class ActivoControlador {

    private final ActivoServicio activoServicio;

    public ActivoControlador(ActivoServicio activoServicio) {
        this.activoServicio = activoServicio;
    }

    public List<ActivoFijo> obtenerInventario() {
        return activoServicio.cargarActivosDesdeBD();
    }
}