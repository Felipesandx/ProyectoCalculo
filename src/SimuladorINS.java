import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class SimuladorINS extends JFrame {

    // Controles de entrada
    private JSpinner spinTiempo;
    private JSpinner spinLongitudTunel;
    private JSpinner spinErrorAccel;
    private JSpinner spinErrorGyro;

    // Componentes de salida / métricas
    private JLabel lblTheta;
    private JLabel lblVelocidad;
    private JLabel lblPosicion;
    private JLabel lblEstadoTunel;
    private JLabel lblVprom;
    private JLabel lblSensibilidadAccel;
    private JLabel lblSensibilidadGyro;

    // Panel gráfico
    private PanelGraficas panelGraficas;

    public SimuladorINS() {
        super("Simulador de Navegación Inercial (INS) - Cálculo II / Ing. en Sistemas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initUI();
        calcularYActualizar();
    }

    private void initUI() {
        // --- PANEL SUPERIOR: ENCABEZADO ---
        JPanel panelNorte = new JPanel(new GridLayout(2, 1, 0, 3));
        panelNorte.setBackground(new Color(24, 32, 45));
        panelNorte.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel titulo = new JLabel("SISTEMA DE NAVEGACIÓN INERCIAL (INS) - ESTIMACIÓN DE TRAYECTORIA");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel("Simulación de Pérdida de GPS en Túnel | Doble Integración, Valor Promedio y Sensibilidad");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitulo.setForeground(new Color(175, 190, 210));

        panelNorte.add(titulo);
        panelNorte.add(subtitulo);
        add(panelNorte, BorderLayout.NORTH);

        // --- PANEL LATERAL IZQUIERDO: CONTROLES Y RESULTADOS ---
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setPreferredSize(new Dimension(350, 0));
        panelIzquierdo.setBorder(new EmptyBorder(10, 12, 10, 6));

        // 1. Grupo de Parámetros de Entrada
        JPanel panelInputs = new JPanel(new GridLayout(4, 2, 8, 8));
        panelInputs.setBorder(crearBordeSeccion("Parámetros de Simulación"));

        panelInputs.add(new JLabel("Tiempo t (s):"));
        spinTiempo = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 30.0, 0.5));
        panelInputs.add(spinTiempo);

        panelInputs.add(new JLabel("Long. Túnel (m):"));
        spinLongitudTunel = new JSpinner(new SpinnerNumberModel(150.0, 10.0, 1000.0, 10.0));
        panelInputs.add(spinLongitudTunel);

        panelInputs.add(new JLabel("Error Acel. da (m/s²):"));
        spinErrorAccel = new JSpinner(new SpinnerNumberModel(2.0, 0.0, 10.0, 0.1));
        panelInputs.add(spinErrorAccel);

        panelInputs.add(new JLabel("Error Girosc. dθ (rad):"));
        spinErrorGyro = new JSpinner(new SpinnerNumberModel(0.1, 0.0, 1.0, 0.01));
        panelInputs.add(spinErrorGyro);

        panelIzquierdo.add(panelInputs);
        panelIzquierdo.add(Box.createVerticalStrut(10));

        // Botón de cálculo
        JButton btnCalcular = new JButton("Recalcular Simulación");
        btnCalcular.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCalcular.setBackground(new Color(30, 110, 210));
        btnCalcular.setForeground(Color.ORANGE);
        btnCalcular.setFocusPainted(false);
        btnCalcular.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCalcular.addActionListener(e -> calcularYActualizar());
        panelIzquierdo.add(btnCalcular);
        panelIzquierdo.add(Box.createVerticalStrut(12));

        // 2. Grupo de Resultados Analíticos
        JPanel panelResultados = new JPanel(new GridLayout(5, 1, 6, 6));
        panelResultados.setBorder(crearBordeSeccion("Resultados Cinemáticos"));

        lblTheta = new JLabel("θ(t): -");
        lblVelocidad = new JLabel("v(t): -");
        lblPosicion = new JLabel("s(t): -");
        lblEstadoTunel = new JLabel("Estado: -");
        lblVprom = new JLabel("Vprom [0, t]: -");

        panelResultados.add(lblTheta);
        panelResultados.add(lblVelocidad);
        panelResultados.add(lblPosicion);
        panelResultados.add(lblEstadoTunel);
        panelResultados.add(lblVprom);

        panelIzquierdo.add(panelResultados);
        panelIzquierdo.add(Box.createVerticalStrut(12));

        // 3. Grupo de Derivadas Parciales y Análisis de Error
        JPanel panelError = new JPanel(new GridLayout(2, 1, 6, 6));
        panelError.setBorder(crearBordeSeccion("Análisis de Derivadas Parciales"));

        lblSensibilidadAccel = new JLabel("∂E/∂εa: -");
        lblSensibilidadGyro = new JLabel("∂E/∂εθ: -");

        panelError.add(lblSensibilidadAccel);
        panelError.add(lblSensibilidadGyro);

        panelIzquierdo.add(panelError);
        panelIzquierdo.add(Box.createVerticalGlue());

        add(panelIzquierdo, BorderLayout.WEST);

        // --- PANEL CENTRAL: GRÁFICAS ---
        panelGraficas = new PanelGraficas();
        add(panelGraficas, BorderLayout.CENTER);
    }

    private TitledBorder crearBordeSeccion(String titulo) {
        TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 215)), titulo);
        border.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        border.setTitleColor(new Color(40, 50, 70));
        return border;
    }

    private void calcularYActualizar() {
        double t = ((Number) spinTiempo.getValue()).doubleValue();
        double lTunel = ((Number) spinLongitudTunel.getValue()).doubleValue();
        double da = ((Number) spinErrorAccel.getValue()).doubleValue();
        double dtheta = ((Number) spinErrorGyro.getValue()).doubleValue();

        // 1. Orientación: θ(t) = 0.01 * t^2
        double thetaRad = 0.01 * Math.pow(t, 2);
        double thetaDeg = thetaRad * (180.0 / Math.PI);

        // 2. Velocidad: v(t) = 5*t*e^(-0.1t) + 50*e^(-0.1t) - 35
        double expTerm = Math.exp(-0.1 * t);
        double v = 5.0 * t * expTerm + 50.0 * expTerm - 35.0;

        // 3. Posición: s(t) = -50*t*e^(-0.1t) - 1000*e^(-0.1t) - 35*t + 1000
        double s = -50.0 * t * expTerm - 1000.0 * expTerm - 35.0 * t + 1000.0;

        // 4. Valor Promedio: V_prom = (s(t) - s(0)) / t
        double vProm = s / t;

        // 5. Análisis de Derivadas Parciales
        double sensAccel = 0.5 * Math.pow(t, 2);
        double errorPosAccel = sensAccel * da;

        double sensGyro = 0.5 * vProm * Math.pow(t, 2);
        double errorPosGyro = sensGyro * dtheta;

        // 6. Evaluación de Salida del Túnel
        double porcentaje = Math.min(100.0, (s / lTunel) * 100.0);
        String estadoHtml;
        if (s >= lTunel) {
            estadoHtml = "<html><b>Estado:</b> <font color='#16a34a'><b>¡Túnel Completado! (GPS Recuperado)</b></font></html>";
        } else {
            double restante = lTunel - s;
            estadoHtml = String.format("<html><b>Estado:</b> <font color='#d97706'>En Túnel (Faltan %.1fm | %.0f%%)</font></html>", restante, porcentaje);
        }

        // Actualizar etiquetas
        lblTheta.setText(String.format("<html><b>Orientación θ:</b> %.4f rad (%.2f°)</html>", thetaRad, thetaDeg));
        lblVelocidad.setText(String.format("<html><b>Velocidad v:</b> %.3f m/s</html>", v));
        lblPosicion.setText(String.format("<html><b>Posición s:</b> %.3f m / %.0f m</html>", s, lTunel));
        lblEstadoTunel.setText(estadoHtml);
        lblVprom.setText(String.format("<html><b>Velocidad Media:</b> %.3f m/s</html>", vProm));

        lblSensibilidadAccel.setText(String.format("<html><b>∂E/∂εa:</b> %.2f s² &rarr; Desvío: <b>%.2f m</b></html>", sensAccel, errorPosAccel));
        lblSensibilidadGyro.setText(String.format("<html><b>∂E/∂εθ:</b> %.2f m/rad &rarr; Desvío: <b>%.2f m</b></html>", sensGyro, errorPosGyro));

        // Actualizar gráficas
        panelGraficas.actualizarDatos(t, vProm, da, lTunel);
    }

    // =========================================================================
    // PANEL GRÁFICO CON LEYENDAS EXTERNAS Y ÁREA DE DATOS 100% DESPEJADA
    // =========================================================================
    private static class PanelGraficas extends JPanel {
        private double tMax = 5.0;
        private double vProm = 13.375;
        private double errorAccel = 2.0;
        private double lTunel = 150.0;

        public PanelGraficas() {
            setBackground(new Color(250, 252, 255));
        }

        public void actualizarDatos(double tMax, double vProm, double errorAccel, double lTunel) {
            this.tMax = tMax;
            this.vProm = vProm;
            this.errorAccel = errorAccel;
            this.lTunel = lTunel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int panelW = getWidth();
            int panelH = getHeight();
            int chartH = (panelH - 60) / 2;

            // Gráfica 1: Velocidad
            dibujarGraficaVelocidad(g2, 10, 10, panelW - 20, chartH);

            // Gráfica 2: Posición, Túnel y Deriva
            dibujarGraficaPosicion(g2, 10, 25 + chartH, panelW - 20, chartH);
        }

        private void dibujarGraficaVelocidad(Graphics2D g2, int x, int y, int w, int h) {
            int padLeft = 60;
            int padRight = 30;
            int padTop = 42; // Espacio superior suficiente para título y leyenda
            int padBottom = 30;

            int plotX = x + padLeft;
            int plotY = y + padTop;
            int plotW = w - padLeft - padRight;
            int plotH = h - padTop - padBottom;

            double yMin = 0.0;
            double yMax = 20.0;

            // 1. Título y Leyenda fuera del recuadro
            g2.setColor(new Color(30, 40, 55));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("Perfil de Velocidad v(t) y Teorema del Valor Promedio", plotX, y + 24);

            String[] lineas = {
                    "v(t) Teórica",
                    String.format("Vprom = %.3f m/s", vProm)
            };
            Color[] colores = { new Color(20, 115, 215), new Color(225, 110, 20) };
            boolean[] punteadas = { false, true };
            dibujarLeyendaHorizontal(g2, plotX + plotW, y + 8, lineas, colores, punteadas);

            // 2. Gráfico y Cuadrícula
            dibujarMarcoYCuadricula(g2, plotX, plotY, plotW, plotH, "m/s", yMin, yMax, tMax);

            int numPuntos = 200;
            int[] px = new int[numPuntos];
            int[] py = new int[numPuntos];

            for (int i = 0; i < numPuntos; i++) {
                double t = (tMax * i) / (numPuntos - 1);
                double expTerm = Math.exp(-0.1 * t);
                double v = 5.0 * t * expTerm + 50.0 * expTerm - 35.0;

                px[i] = plotX + (int) ((t / tMax) * plotW);
                py[i] = plotY + plotH - (int) (((v - yMin) / (yMax - yMin)) * plotH);
            }

            Shape clipOriginal = g2.getClip();
            g2.clipRect(plotX, plotY, plotW, plotH);

            // Línea del valor promedio
            int yVpromPx = plotY + plotH - (int) (((vProm - yMin) / (yMax - yMin)) * plotH);
            g2.setColor(new Color(225, 110, 20));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 5.0f}, 0.0f));
            g2.drawLine(plotX, yVpromPx, plotX + plotW, yVpromPx);

            // Curva v(t)
            g2.setColor(new Color(20, 115, 215));
            g2.setStroke(new BasicStroke(2.4f));
            g2.drawPolyline(px, py, numPuntos);

            g2.setClip(clipOriginal);
        }

        private void dibujarGraficaPosicion(Graphics2D g2, int x, int y, int w, int h) {
            int padLeft = 60;
            int padRight = 30;
            int padTop = 42;
            int padBottom = 30;

            int plotX = x + padLeft;
            int plotY = y + padTop;
            int plotW = w - padLeft - padRight;
            int plotH = h - padTop - padBottom;

            // Escala adaptativa
            double expT = Math.exp(-0.1 * tMax);
            double sTeoricaMax = -50.0 * tMax * expT - 1000.0 * expT - 35.0 * tMax + 1000.0;
            double sErrorMax = sTeoricaMax + 0.5 * errorAccel * Math.pow(tMax, 2);
            double yMin = 0.0;
            double mayorDistancia = Math.max(lTunel, Math.max(sTeoricaMax, sErrorMax));
            double yMax = Math.max(100.0, Math.ceil((mayorDistancia * 1.15) / 20.0) * 20.0);

            // 1. Título y Leyenda fuera del recuadro
            g2.setColor(new Color(30, 40, 55));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString("Desplazamiento s(t) y Deriva Inercial Acumulada", plotX, y + 24);

            String[] lineas = {
                    "s(t) Teórica",
                    String.format("Túnel (%.0f m)", lTunel),
                    String.format("Con Deriva (da=%.1f)", errorAccel)
            };
            Color[] colores = { new Color(30, 150, 60), new Color(220, 38, 38), new Color(155, 40, 160) };
            boolean[] punteadas = { false, true, true };
            dibujarLeyendaHorizontal(g2, plotX + plotW, y + 8, lineas, colores, punteadas);

            // 2. Gráfico y Cuadrícula
            dibujarMarcoYCuadricula(g2, plotX, plotY, plotW, plotH, "metros", yMin, yMax, tMax);

            int numPuntos = 200;
            int[] px = new int[numPuntos];
            int[] pyTeorico = new int[numPuntos];
            int[] pyError = new int[numPuntos];

            for (int i = 0; i < numPuntos; i++) {
                double t = (tMax * i) / (numPuntos - 1);
                double expTerm = Math.exp(-0.1 * t);
                double s = -50.0 * t * expTerm - 1000.0 * expTerm - 35.0 * t + 1000.0;
                double sErr = s + 0.5 * errorAccel * Math.pow(t, 2);

                px[i] = plotX + (int) ((t / tMax) * plotW);
                pyTeorico[i] = plotY + plotH - (int) (((s - yMin) / (yMax - yMin)) * plotH);
                pyError[i] = plotY + plotH - (int) (((sErr - yMin) / (yMax - yMin)) * plotH);
            }

            Shape clipOriginal = g2.getClip();
            g2.clipRect(plotX, plotY, plotW, plotH);

            // Sombra del área de deriva inercial
            Polygon areaDeriva = new Polygon();
            for (int i = 0; i < numPuntos; i++) areaDeriva.addPoint(px[i], pyTeorico[i]);
            for (int i = numPuntos - 1; i >= 0; i--) areaDeriva.addPoint(px[i], pyError[i]);

            g2.setColor(new Color(155, 60, 180, 45));
            g2.fill(areaDeriva);

            // Línea horizontal límite del túnel (totalmente visible)
            int yTunelPx = plotY + plotH - (int) (((lTunel - yMin) / (yMax - yMin)) * plotH);
            g2.setColor(new Color(220, 38, 38));
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 4.0f}, 0.0f));
            g2.drawLine(plotX, yTunelPx, plotX + plotW, yTunelPx);

            // Etiqueta sutil directamente sobre la línea roja
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            g2.drawString(String.format("Límite Túnel (%.0f m)", lTunel), plotX + 8, yTunelPx - 4);

            // Curva Teórica s(t)
            g2.setColor(new Color(30, 150, 60));
            g2.setStroke(new BasicStroke(2.4f));
            g2.drawPolyline(px, pyTeorico, numPuntos);

            // Curva con Error (Deriva)
            g2.setColor(new Color(155, 40, 160));
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6.0f, 5.0f}, 0.0f));
            g2.drawPolyline(px, pyError, numPuntos);

            g2.setClip(clipOriginal);
        }

        private void dibujarMarcoYCuadricula(Graphics2D g2, int x, int y, int w, int h,
                                             String unidadY, double yMin, double yMax, double tMax) {
            g2.setColor(Color.WHITE);
            g2.fillRect(x, y, w, h);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            // Eje Y y cuadrícula horizontal (4 intervalos / 5 líneas)
            for (int i = 0; i <= 4; i++) {
                int gy = y + h - (h * i) / 4;
                double valY = yMin + ((yMax - yMin) * i) / 4.0;
                String strY = String.format("%.0f", valY);

                g2.setColor(new Color(230, 235, 243));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawLine(x, gy, x + w, gy);

                g2.setColor(new Color(90, 100, 115));
                int strW = fm.stringWidth(strY);
                g2.drawString(strY, x - strW - 8, gy + 4);

                g2.setColor(new Color(170, 180, 195));
                g2.drawLine(x - 4, gy, x, gy);
            }

            // Eje X y cuadrícula vertical
            for (int i = 0; i <= 5; i++) {
                int gx = x + (w * i) / 5;
                double valT = (tMax * i) / 5.0;
                String strT = String.format("%.1fs", valT);

                g2.setColor(new Color(230, 235, 243));
                g2.drawLine(gx, y, gx, y + h);

                g2.setColor(new Color(90, 100, 115));
                int strW = fm.stringWidth(strT);
                g2.drawString(strT, gx - (strW / 2), y + h + 18);

                g2.setColor(new Color(170, 180, 195));
                g2.drawLine(gx, y + h, gx, y + h + 4);
            }

            // Marco del gráfico
            g2.setColor(new Color(180, 190, 205));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRect(x, y, w, h);

            // Indicador de unidad del Eje Y arriba a la izquierda
            g2.setColor(new Color(110, 120, 135));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString("[" + unidadY + "]", x - 35, y - 6);
        }

        private void dibujarLeyendaHorizontal(Graphics2D g2, int rightX, int topY,
                                              String[] lineas, Color[] colores, boolean[] punteadas) {
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();

            int paddingH = 10;
            int itemSpacing = 14;
            int sampleW = 18;
            int sampleGap = 6;

            int totalW = paddingH * 2;
            int[] itemW = new int[lineas.length];
            for (int i = 0; i < lineas.length; i++) {
                itemW[i] = sampleW + sampleGap + fm.stringWidth(lineas[i]);
                totalW += itemW[i];
                if (i > 0) totalW += itemSpacing;
            }

            int boxH = 24;
            int boxX = rightX - totalW;
            int boxY = topY;

            // Caja con esquinas redondeadas
            g2.setColor(new Color(255, 255, 255, 240));
            g2.fillRoundRect(boxX, boxY, totalW, boxH, 6, 6);
            g2.setColor(new Color(205, 215, 225));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(boxX, boxY, totalW, boxH, 6, 6);

            // Muestras y textos
            int curX = boxX + paddingH;
            int textY = boxY + 16;
            int sampleY = boxY + 12;

            for (int i = 0; i < lineas.length; i++) {
                g2.setColor(colores[i]);
                if (punteadas[i]) {
                    g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f, 4.0f}, 0.0f));
                } else {
                    g2.setStroke(new BasicStroke(2.2f));
                }
                g2.drawLine(curX, sampleY, curX + sampleW, sampleY);

                g2.setColor(new Color(40, 45, 55));
                g2.drawString(lineas[i], curX + sampleW + sampleGap, textY);

                curX += itemW[i] + itemSpacing;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SimuladorINS().setVisible(true);
        });
    }
}