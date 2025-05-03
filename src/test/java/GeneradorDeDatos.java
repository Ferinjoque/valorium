import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GeneradorDeDatos {

    private static final String[] NOMBRES_ACTIVOS = {
            "Computadora Dell", "Escritorio de Madera", "Microscopio Nikon", "Auto Toyota", "Terreno Urbano",
            "Servidor HP", "Silla Ergonómica", "Laboratorio Químico", "Auto Hyundai", "Terreno Rústico",
            "Monitor Samsung", "Mesa de Reuniones", "Microscopio Olympus", "Camioneta Ford", "Parcela Agrícola"
    };

    private static final String[] CATEGORIAS = {
            "Equipo de Computo", "Mobiliario", "Equipo de Laboratorio", "Vehiculo", "Terreno"
    };

    private static final String[] ESTADOS = {"Alta", "Depreciado", "Baja", "Baja por venta"};

    private static final String[] MOTIVOS_BAJA = {"Obsoleto", "Daño irreparable", "Reemplazo", "Venta"};
    private static final String[] FORMAS_PAGO = {"Efectivo", "Transferencia", "Cheque"};
    private static final String[] NOMBRES_COMPRADORES = {"Empresa ABC", "Inversiones XYZ", "Morris Belmont"};

    private static final Random RANDOM = new Random();

    public static void main(String[] args) {
        StringBuilder sqlScripts = new StringBuilder();

        sqlScripts.append(generarActivoInserts(100));
        sqlScripts.append(generarMovimientoInserts(50));
        sqlScripts.append(generarVentaInserts(30));
        sqlScripts.append(generarRevaluacionInserts(20));
        sqlScripts.append(generarNotificacionInserts(5));

        try {
            String filePath = "src/test/java/datos_generados.sql";
            File file = new File(filePath);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(sqlScripts.toString());
                System.out.println("Archivo '" + file.getAbsolutePath() + "' generado.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String generarActivoInserts(int count) {
        StringBuilder inserts = new StringBuilder("-- Activos\n");
        for (int i = 1; i <= count; i++) {
            String codigoPatrimonial = "2024-" + String.format("%04d", i);
            String nombre = NOMBRES_ACTIVOS[RANDOM.nextInt(NOMBRES_ACTIVOS.length)];
            String categoria = CATEGORIAS[RANDOM.nextInt(CATEGORIAS.length)];
            String estado = ESTADOS[RANDOM.nextInt(ESTADOS.length)];
            String fechaAdquisicion = generarRandomDate();
            int vidaUtil = determinarVidaUtil(categoria);
            double costoInicial = Math.round((RANDOM.nextDouble() * 10000 + 1000) * 100.0) / 100.0;
            double depreciacionMensual = calcularDepreciacionMensual(costoInicial, vidaUtil);
            double depreciacionAcumulada = calcularDepreciacionAcumulada(depreciacionMensual, fechaAdquisicion, vidaUtil);
            double valorResidual = Math.max(0, costoInicial - depreciacionAcumulada);
            int idSede = RANDOM.nextInt(4) + 1;

            inserts.append(String.format(
                    "INSERT INTO activos (id_sede, codigo_patrimonial, nombre, categoria, estado, fecha_adquisicion, vida_util, costo_inicial, depreciacion_mensual, depreciacion_acumulada, valor_residual) " +
                            "VALUES (%d, '%s', '%s', '%s', '%s', '%s', %d, %.2f, %.2f, %.2f, %.2f);%n",
                    idSede, codigoPatrimonial, nombre, categoria, estado, fechaAdquisicion, vidaUtil, costoInicial, depreciacionMensual, depreciacionAcumulada, valorResidual
            ));
        }
        return inserts.toString();
    }

    private static String generarMovimientoInserts(int count) {
        StringBuilder inserts = new StringBuilder("-- Movimientos\n");
        for (int i = 1; i <= count; i++) {
            int idActivo = RANDOM.nextInt(100) + 1;
            String tipoMovimiento = "Baja";
            String fechaHora = generarRandomDateTime();
            String motivoBaja = MOTIVOS_BAJA[RANDOM.nextInt(MOTIVOS_BAJA.length)];

            inserts.append(String.format(
                    "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, motivo_baja, tipo_baja) " +
                            "VALUES (%d, '%s', '%s', '%s', '%s');%n",
                    idActivo, tipoMovimiento, fechaHora, motivoBaja, tipoMovimiento
            ));
        }
        return inserts.toString();
    }

    private static String generarVentaInserts(int count) {
        StringBuilder inserts = new StringBuilder("-- Ventas\n");
        for (int i = 1; i <= count; i++) {
            int idActivo = RANDOM.nextInt(100) + 1;
            String comprador = NOMBRES_COMPRADORES[RANDOM.nextInt(NOMBRES_COMPRADORES.length)];
            String rucComprador = String.valueOf(10000000000L + RANDOM.nextInt(999999999));
            String fechaVenta = generarRandomDateTime();
            double precioVenta = Math.round((RANDOM.nextDouble() * 5000 + 1000) * 100.0) / 100.0;
            String formaPago = FORMAS_PAGO[RANDOM.nextInt(FORMAS_PAGO.length)];
            String detalleVenta = "Venta realizada satisfactoriamente.";

            inserts.append(String.format(
                    "INSERT INTO ventas (id_activo, comprador, ruc_comprador, fecha_venta, precio_venta, forma_pago, detalle_venta) " +
                            "VALUES (%d, '%s', '%s', '%s', %.2f, '%s', '%s');%n",
                    idActivo, comprador, rucComprador, fechaVenta, precioVenta, formaPago, detalleVenta
            ));
        }
        return inserts.toString();
    }

    private static String generarRevaluacionInserts(int count) {
        StringBuilder inserts = new StringBuilder("-- Revaluaciones\n");
        for (int i = 1; i <= count; i++) {
            int idMovimiento = RANDOM.nextInt(50) + 1;
            double valorAnterior = Math.round((RANDOM.nextDouble() * 10000 + 5000) * 100.0) / 100.0;
            double valorRevaluado = Math.round((valorAnterior * (1 + RANDOM.nextDouble() * 0.2)) * 100.0) / 100.0;
            double depreciacionAnterior = Math.round((valorAnterior / 10) * 100.0) / 100.0;
            double nuevaDepreciacion = Math.round((valorRevaluado / 10) * 100.0) / 100.0;
            double impactoTotal = Math.round((valorRevaluado - valorAnterior) * 100.0) / 100.0;

            inserts.append(String.format(
                    "INSERT INTO revaluaciones (id_movimiento, valor_anterior, valor_revaluado, depreciacion_anterior, nueva_depreciacion, impacto_total) " +
                            "VALUES (%d, %.2f, %.2f, %.2f, %.2f, %.2f);%n",
                    idMovimiento, valorAnterior, valorRevaluado, depreciacionAnterior, nuevaDepreciacion, impactoTotal
            ));
        }
        return inserts.toString();
    }

    private static String generarNotificacionInserts(int count) {
        StringBuilder inserts = new StringBuilder("-- Notificaciones\n");
        for (int i = 1; i <= count; i++) {
            int idActivo = RANDOM.nextInt(100) + 1;
            String mensaje = "El activo se depreciará en menos de 6 meses.";
            String estado = "Pendiente";
            String fechaCreacion = generarRandomDateTime();

            inserts.append(String.format(
                    "INSERT INTO notificaciones (id_activo, mensaje, estado, fecha_creacion) " +
                            "VALUES (%d, '%s', '%s', '%s');%n",
                    idActivo, mensaje, estado, fechaCreacion
            ));
        }
        return inserts.toString();
    }

    private static String generarRandomDate() {
        int year = 2000 + RANDOM.nextInt(25);
        int month = RANDOM.nextInt(12) + 1;
        int day = RANDOM.nextInt(28) + 1;
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private static String generarRandomDateTime() {
        return generarRandomDate() + " " + String.format("%02d:%02d:%02d",
                RANDOM.nextInt(24), RANDOM.nextInt(60), RANDOM.nextInt(60));
    }
    private static int determinarVidaUtil(String categoria) {
        return switch (categoria) {
            case "Equipo de Computo" -> 4;
            case "Mobiliario" -> 10;
            case "Equipo de Laboratorio" -> 5;
            case "Vehiculo" -> 5;
            case "Terreno" -> 0;
            default -> 1;
        };
    }

    private static double calcularDepreciacionMensual(double costoInicial, int vidaUtil) {
        if (vidaUtil == 0) return 0;
        return Math.round((costoInicial / (vidaUtil * 12)) * 100.0) / 100.0;
    }

    private static double calcularDepreciacionAcumulada(double depreciacionMensual, String fechaAdquisicion, int vidaUtil) {
        if (vidaUtil == 0) return 0;

        int mesesTranscurridos = calcularMesesTranscurridos(fechaAdquisicion);
        int mesesVidaUtil = vidaUtil * 12;

        if (mesesTranscurridos > mesesVidaUtil) mesesTranscurridos = mesesVidaUtil;

        return Math.round((depreciacionMensual * mesesTranscurridos) * 100.0) / 100.0;
    }

    private static int calcularMesesTranscurridos(String fechaAdquisicion) {
        String[] parts = fechaAdquisicion.split("-");
        int year = Integer.parseInt(parts[2]);
        int month = Integer.parseInt(parts[1]);

        int currentYear = 2024;
        int currentMonth = 12;

        return (currentYear - year) * 12 + (currentMonth - month);
    }
}