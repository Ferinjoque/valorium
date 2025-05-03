package util;

import javax.swing.*;
import java.util.regex.Pattern;

public class ValidadorVentas {

    public static boolean validarComprador(JTextField txtComprador) {
        if (txtComprador.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El campo 'Comprador' no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean validarRUCComprador(JTextField txtRUCComprador) {
        String ruc = txtRUCComprador.getText().trim();
        if (ruc.isEmpty() || !Pattern.matches("\\d{11}", ruc)) {
            JOptionPane.showMessageDialog(null, "El RUC del comprador debe tener 11 dígitos.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean validarDetallesVenta(JTextArea txtDetalleVenta) {
        if (txtDetalleVenta.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "El campo 'Detalles de la Venta' no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public static boolean validarPrecioVenta(JTextField txtPrecioVenta, double valorResidual) {
        String precioVentaStr = txtPrecioVenta.getText().trim();
        if (precioVentaStr.isEmpty()) {
            JOptionPane.showMessageDialog(null, "El campo 'Precio de Venta' no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            double precioVenta = Double.parseDouble(precioVentaStr);
            if (precioVenta <= 0) {
                JOptionPane.showMessageDialog(null, "El 'Precio de Venta' debe ser un valor positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            if (precioVenta >= valorResidual) {
                JOptionPane.showMessageDialog(null, "El 'Precio de Venta' debe ser menor al valor residual del activo.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "El 'Precio de Venta' debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public static boolean validarFormularioVenta(JTextField txtComprador, JTextField txtRUCComprador, JTextArea txtDetalleVenta, JTextField txtPrecioVenta, double valorResidual) {
        return validarComprador(txtComprador) &&
                validarRUCComprador(txtRUCComprador) &&
                validarDetallesVenta(txtDetalleVenta) &&
                validarPrecioVenta(txtPrecioVenta, valorResidual);
    }
}