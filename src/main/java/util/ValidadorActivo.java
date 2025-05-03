package util;

import com.toedter.calendar.JDateChooser;
import javax.swing.JComboBox;

public class ValidadorActivo {

    public static String validarNombre(String nombre) {
        if (nombre == null || nombre.length() < 3) {
            return "El nombre debe tener al menos 3 caracteres.";
        }
        return null;
    }

    public static String validarCategoria(JComboBox<String> comboCategoria) {
        String categoria = (String) comboCategoria.getSelectedItem();
        if (categoria == null || categoria.isEmpty()) {
            return "Seleccione una categoría para el activo.";
        }
        return null;
    }

    public static String validarCostoInicial(String costoInicialStr) {
        try {
            double costoInicial = Double.parseDouble(costoInicialStr);
            if (costoInicial <= 0) {
                return "El costo inicial debe ser mayor a 0.";
            }
            if (costoInicial > 100_000_000) {
                return "El costo inicial no puede ser mayor a S/ 100,000,000.";
            }
        } catch (NumberFormatException e) {
            return "Formato incorrecto para el costo inicial. Debe ser un número.";
        }
        return null;
    }

    public static String validarFechaAdquisicion(JDateChooser dateChooser) {
        return ValidadorFechas.validarFechaJDateChooser(dateChooser);
    }
}