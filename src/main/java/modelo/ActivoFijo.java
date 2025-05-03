package modelo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ActivoFijo extends Bien implements Depreciable {

    private int numero;
    private String codigoPatrimonial;
    private String fechaAdquisicion;
    private String fechaInicioDepreciacion;
    private int vidaUtil;
    private double depreciacionMensual;
    private String responsableRegistro;
    private String motivoBaja;
    private String tipoBaja;
    private Sede sede;

    public ActivoFijo(String nombre, String categoria, double costoInicial, String fechaAdquisicion, String responsableRegistro, Sede sede) {
        super(nombre, categoria, costoInicial);
        this.fechaAdquisicion = fechaAdquisicion;
        this.vidaUtil = determinarVidaUtil(categoria);
        actualizarEstado();
        this.depreciacionMensual = calcularDepreciacionMensual();
        this.responsableRegistro = responsableRegistro;
        this.sede = sede;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fechaAdq = LocalDate.parse(fechaAdquisicion, formatter);
        this.fechaInicioDepreciacion = fechaAdq.plusMonths(1).format(formatter);
        this.codigoPatrimonial = "";
    }

    public Sede getSede() {
        return sede;
    }

    public int getNumero() {
        return this.numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public void setId(int id) {
    }

    public String getCodigoPatrimonial() {
        return codigoPatrimonial;
    }

    public void setCodigoPatrimonial(String codigoPatrimonial) {
        this.codigoPatrimonial = codigoPatrimonial;
    }

    public String getFechaAdquisicion() {
        return fechaAdquisicion;
    }

    public int getVidaUtil() {
        return vidaUtil;
    }

    public void setFechaAdquisicion(String fechaAdquisicion) {
        this.fechaAdquisicion = fechaAdquisicion;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fechaAdq = LocalDate.parse(fechaAdquisicion, formatter);
        this.fechaInicioDepreciacion = fechaAdq.plusMonths(1).format(formatter);
    }

    public void setVidaUtil(String nuevaCategoria) {
        this.vidaUtil = determinarVidaUtil(nuevaCategoria);
    }

    @Override
    public double calcularDepreciacionMensual() {
        if (categoria.equals("Terreno")) {
            return 0.0;
        }
        DepreciacionLineaRecta depreciacion = new DepreciacionLineaRecta(costoInicial, vidaUtil);
        return depreciacion.calcularDepreciacion() / 12;
    }

    public double calcularDepreciacionAcumulada() {
        if (vidaUtil == 0) {
            return 0.0;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fechaInicioDepreciacion = LocalDate.parse(this.fechaInicioDepreciacion, formatter);
        LocalDate fechaActual = LocalDate.now();

        long diasTranscurridos = ChronoUnit.DAYS.between(fechaInicioDepreciacion, fechaActual);

        if (diasTranscurridos < 30) {
            return 0.0;
        }

        long mesesTranscurridos = diasTranscurridos / 30;

        long mesesVidaUtil = (long) vidaUtil * 12;
        if (mesesTranscurridos > mesesVidaUtil) {
            mesesTranscurridos = mesesVidaUtil;
        }

        return mesesTranscurridos * (costoInicial / (double) mesesVidaUtil);
    }

    @Override
    public double calcularValorResidual() {
        if (vidaUtil == 0) {
            return costoInicial;
        }

        double depreciacionAcumulada = calcularDepreciacionAcumulada();
        return Math.max(0, costoInicial - depreciacionAcumulada);
    }

    public static int determinarVidaUtil(String categoria) {
        switch (categoria) {
            case "Equipo de Computo":
                return 4;
            case "Mobiliario":
                return 10;
            case "Equipo de Laboratorio":
                return 5;
            case "Vehiculo":
                return 5;
            case "Terreno":
                return 0;
            default:
                return 1;
        }
    }

    @Override
    public void actualizarEstado() {
        if ("Baja".equalsIgnoreCase(this.estado) || "Baja por venta".equalsIgnoreCase(this.estado)) {
            return;
        }

        if ("Terreno".equalsIgnoreCase(categoria) || vidaUtil == 0) {
            this.estado = "Alta";
            return;
        }

        LocalDate fechaAdq = LocalDate.parse(fechaAdquisicion, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        LocalDate fechaFinVidaUtil = fechaAdq.plusYears(vidaUtil);
        LocalDate fechaActual = LocalDate.now();

        if (fechaActual.isAfter(fechaFinVidaUtil)) {
            this.estado = "Depreciado";
        } else {
            this.estado = "Alta";
        }
    }

    public void darDeBaja(String motivo, String tipoBaja) {
        this.motivoBaja = motivo;
        this.tipoBaja = tipoBaja;
        this.estado = "Baja";
    }
}