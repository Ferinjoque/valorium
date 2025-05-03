package util;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class ImpactoTotalCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (value != null) {
            try {
                String porcentajeStr = value.toString().replace("%", "");
                double porcentaje = Double.parseDouble(porcentajeStr);

                if (porcentaje >= 50) {
                    cell.setBackground(new Color(144, 238, 144));
                } else if (porcentaje >= 20) {
                    cell.setBackground(new Color(173, 255, 47));
                } else if (porcentaje >= 10) {
                    cell.setBackground(new Color(255, 255, 153));
                } else if (porcentaje >= 0) {
                    cell.setBackground(new Color(255, 204, 153));
                }

                cell.setForeground(Color.DARK_GRAY);
            } catch (NumberFormatException e) {
                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);
            }
        } else {
            cell.setBackground(Color.WHITE);
            cell.setForeground(Color.BLACK);
        }

        return cell;
    }
}