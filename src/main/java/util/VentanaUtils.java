package util;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

public class VentanaUtils {

    public static void deshabilitarMinimizar(JFrame frame) {
        try {
            frame.addWindowStateListener(new WindowStateListener() {
                @Override
                public void windowStateChanged(WindowEvent e) {
                    if (e.getNewState() == JFrame.ICONIFIED) {
                        frame.setExtendedState(JFrame.NORMAL);
                    }
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Error al deshabilitar minimizar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}