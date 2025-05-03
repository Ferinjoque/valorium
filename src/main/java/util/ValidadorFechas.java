package util;

import com.toedter.calendar.JDateChooser;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class ValidadorFechas {

    public static String validarFecha(String fechaAdquisicion) {
        String[] partesFecha = fechaAdquisicion.split("-");
        if (partesFecha.length != 3) {
            return "Fecha en formato incorrecto. Use DD-MM-AAAA.";
        }

        try {
            int dia = Integer.parseInt(partesFecha[0]);
            int mes = Integer.parseInt(partesFecha[1]);
            int anio = Integer.parseInt(partesFecha[2]);

            LocalDate fechaIngresada = LocalDate.of(anio, mes, dia);
            LocalDate fechaActual = LocalDate.now();

            if (anio < 1997 || fechaIngresada.isAfter(fechaActual)) {
                return "La fecha debe ser entre 1997 y la fecha actual.";
            }

            if (!esDiaValido(dia, mes, anio)) {
                return "El día ingresado no es válido.";
            }

            return null;

        } catch (Exception e) {
            return "Fecha en formato incorrecto. Use DD-MM-AAAA.";
        }
    }

    public static boolean esDiaValido(int dia, int mes, int anio) {
        int[] diasPorMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (mes == 2 && esAnioBisiesto(anio)) {
            diasPorMes[1] = 29;
        }
        return dia > 0 && dia <= diasPorMes[mes - 1];
    }

    public static boolean esAnioBisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
    }

    public static String validarFechaJDateChooser(JDateChooser dateChooser) {
        Date fecha = dateChooser.getDate();
        if (fecha == null) {
            return "La fecha de adquisición no puede estar vacía.";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fechaStr = dateFormat.format(fecha);

        return validarFecha(fechaStr);
    }
}