package inventario;

import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * VentanaFacturaGUI actualizada para:
 * - mostrar datos del Despacho recibido
 * - mantener exactamente la experiencia de impresión previa
 * - CORREGIDO: No generar nueva remisión al visualizar
 */
public class VentanaFacturaGUI extends JDialog {
    private final Despacho despacho;
    private JPanel panelContenido;

    public VentanaFacturaGUI(Window owner, Despacho despacho) {
        super(owner, "Comprobante de Despacho - ECOBRILLA SOLUCIONES S.A.S", ModalityType.APPLICATION_MODAL);
        this.despacho = despacho;
        setSize(700, 900);
        setLocationRelativeTo(owner);

        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // === ENCABEZADO ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logo = new ImageIcon(getClass().getResource("/inventario/logo.png"));
            Image scaled = logo.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            logoLabel.setText("ECOBRILLA");
            logoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        }

        JTextArea infoEmpresa = new JTextArea(
                       "ECOBRILLA SOLUCIONES S.A.S\n" +
                        "NIT: 901 972 853-4\n" +
                        "Tel: (602) 3154920190\n" +
                        "Buenaventura – Colombia\n" +
                        "ecobrillasolucionessas@gmail.com"
        );
        infoEmpresa.setEditable(false);
        infoEmpresa.setOpaque(false);
        infoEmpresa.setFont(new Font("SansSerif", Font.PLAIN, 12));
        infoEmpresa.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(logoLabel, BorderLayout.WEST);
        header.add(infoEmpresa, BorderLayout.CENTER);
        panelContenido.add(header);

        // === TÍTULO Y FECHA ===
        JLabel titulo = new JLabel("COMPROBANTE DE DESPACHO");
titulo.setFont(new Font("Arial", Font.BOLD, 16));
titulo.setAlignmentX(Component.CENTER_ALIGNMENT); // ← en vez de setHorizontalAlignment
titulo.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
panelContenido.add(titulo);


        // CORREGIDO: Usar el número de remisión existente sin generar nuevo
        JLabel lblRemision = new JLabel("REMISIÓN N°: " + despacho.getNumeroRemision());
        lblRemision.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRemision.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblRemision);

        JLabel lblFecha = new JLabel("impreso: " + (despacho.getFechaHora() != null 
            ? despacho.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            : "Sin fecha"));
        lblFecha.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblFecha);

        JLabel lblFechaEntrega = new JLabel("Fecha de Entrega: " + (despacho.getFechaEntrega() != null 
            ? despacho.getFechaEntrega().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            : "No especificada"));
        lblFechaEntrega.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenido.add(lblFechaEntrega);

        panelContenido.add(Box.createRigidArea(new Dimension(0, 15)));

        // === DATOS DEL CLIENTE ===
        JPanel clientePanel = new JPanel(new GridLayout(0, 2, 10, 5));
        clientePanel.setBackground(Color.WHITE);
        clientePanel.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        clientePanel.add(new JLabel("Cliente:"));
        clientePanel.add(new JLabel(nullToEmpty(despacho.getClienteNombre())));
        clientePanel.add(new JLabel("NIT:"));
        clientePanel.add(new JLabel(nullToEmpty(despacho.getClienteNIT())));
        clientePanel.add(new JLabel("Teléfono:"));
        clientePanel.add(new JLabel(nullToEmpty(despacho.getClienteTelefono())));
        clientePanel.add(new JLabel("Dirección:"));
        clientePanel.add(new JLabel(nullToEmpty(despacho.getClienteDireccion())));
        clientePanel.add(new JLabel("Ciudad:"));
        clientePanel.add(new JLabel(nullToEmpty(despacho.getClienteCiudad())));
        panelContenido.add(clientePanel);

        panelContenido.add(Box.createRigidArea(new Dimension(0, 15)));

        // === TABLA DE PRODUCTOS ===
        JPanel tablaPanel = new JPanel(new BorderLayout());
        tablaPanel.setBackground(Color.WHITE);
        tablaPanel.setBorder(BorderFactory.createTitledBorder("Productos Despachados"));

        DefaultTableModel modelo = new DefaultTableModel(new String[]{"Ítem", "Descripción", "Cantidad"}, 0);
        int i = 1;
        for (Despacho.Item it : despacho.getItems()) {
            modelo.addRow(new Object[]{i++, it.getNombreProducto(), it.getCantidad()});
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        tabla.setFillsViewportHeight(true);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        tablaPanel.add(scroll, BorderLayout.CENTER);
        panelContenido.add(tablaPanel);

        panelContenido.add(Box.createRigidArea(new Dimension(0, 15)));

        // === OBSERVACIONES Y FIRMAS ===
        JTextArea observaciones = new JTextArea("Observaciones: " + nullToEmpty(despacho.getNotas()));
        observaciones.setWrapStyleWord(true);
        observaciones.setLineWrap(true);
        observaciones.setEditable(false);
        observaciones.setFont(new Font("SansSerif", Font.PLAIN, 12));
        observaciones.setBorder(BorderFactory.createTitledBorder("Observaciones"));
        panelContenido.add(observaciones);

        JPanel firmas = new JPanel(new GridLayout(1, 2, 40, 0));
        firmas.setBackground(Color.WHITE);
        firmas.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        firmas.add(new JLabel("Entregado por: ____________________"));
        firmas.add(new JLabel("Recibido por: ____________________"));
        panelContenido.add(firmas);

        add(new JScrollPane(panelContenido), BorderLayout.CENTER);

        // === BOTONES ===
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnImprimir = new JButton("Imprimir");
        JButton btnCerrar = new JButton("Cerrar");
        bottom.add(btnImprimir);
        bottom.add(btnCerrar);
        add(bottom, BorderLayout.SOUTH);

        // === ACCIONES ===
        btnCerrar.addActionListener(e -> dispose());
        btnImprimir.addActionListener(e -> imprimirPanel(panelContenido));
    }

    private void imprimirPanel(JPanel panel) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Factura " + (despacho != null ? despacho.getNumeroRemision() : ""));

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            double scaleX = pageFormat.getImageableWidth() / panel.getWidth();
            double scaleY = pageFormat.getImageableHeight() / panel.getHeight();
            double scale = Math.min(scaleX, scaleY);
            if (scale < 1.0) g2d.scale(scale, scale);
            panel.printAll(g2d);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
            }
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
