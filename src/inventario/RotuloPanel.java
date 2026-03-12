package inventario;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDate;

/**
 * Panel de Rótulo de Producción — versión mejorada con estilo de ficha técnica,
 * tamaño aproximado carta y logo de empresa.
 */
public class RotuloPanel extends JPanel implements Printable {

    private JTextField tfProducto, tfLote, tfFecha, tfRealizadoPor, tfEstado;
    private JButton btnImprimir;
    private Image logo;

    public RotuloPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // === Cargar el logo (debe estar en /src/inventario/logo.png o mismo directorio de ejecución)
        try {
            logo = Toolkit.getDefaultToolkit().getImage(getClass().getResource("logo.png"));
        } catch (Exception e) {
            logo = null; // Si no se encuentra, no se detiene la app
        }

        // === Panel principal (ficha técnica) ===
        JPanel ficha = new JPanel(new GridBagLayout());
        ficha.setBackground(Color.WHITE);
        ficha.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                "FICHA DE PRODUCCIÓN TÉCNICA",
                0, 0, new Font("Arial", Font.BOLD, 16)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // === Campos ===
        tfProducto = new JTextField();
        tfLote = new JTextField();
        tfFecha = new JTextField(LocalDate.now().toString());
        tfRealizadoPor = new JTextField();
        tfEstado = new JTextField();

        // Filas
        addRow(ficha, gbc, 0, "Producto:", tfProducto);
        addRow(ficha, gbc, 1, "Lote:", tfLote);
        addRow(ficha, gbc, 2, "Fecha:", tfFecha);
        addRow(ficha, gbc, 3, "Realizado por:", tfRealizadoPor);
        addRow(ficha, gbc, 4, "Estado:", tfEstado);

        // === Botón imprimir ===
        btnImprimir = new JButton("🖨 Imprimir Ficha");
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnImprimir.setBackground(new Color(0, 123, 255));
        btnImprimir.setForeground(Color.WHITE);
        btnImprimir.setFocusPainted(false);
        btnImprimir.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnImprimir.addActionListener(e -> imprimirRotulo());

        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.add(btnImprimir);

        // === Encabezado con logo ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("EcoBrilla soluciones s.a.s", JLabel.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));

        if (logo != null) {
            JLabel lblLogo = new JLabel(new ImageIcon(logo.getScaledInstance(100, 100, Image.SCALE_SMOOTH)));
            header.add(lblLogo, BorderLayout.WEST);
        }

        header.add(lblTitulo, BorderLayout.CENTER);

        // === Ensamble final ===
        add(header, BorderLayout.NORTH);
        add(ficha, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    // Método auxiliar para agregar filas de campos
    private void addRow(JPanel panel, GridBagConstraints gbc, int y, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(label, JLabel.RIGHT), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(field, gbc);
        gbc.weightx = 0;
    }

    // === Impresión ===
    private void imprimirRotulo() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        g2d.setPaint(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Dibujar logo
        if (logo != null) {
            g2d.drawImage(logo, 40, 40, 80, 80, this);
        }

        // Título
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("FICHA DE PRODUCCIÓN TÉCNICA", 150, 60);

        // Campos
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = 120;
        g2d.drawString("Producto: " + tfProducto.getText(), 50, y);
        g2d.drawString("Lote: " + tfLote.getText(), 50, y + 20);
        g2d.drawString("Fecha: " + tfFecha.getText(), 50, y + 40);
        g2d.drawString("Realizado por: " + tfRealizadoPor.getText(), 50, y + 60);
        g2d.drawString("Estado: " + tfEstado.getText(), 50, y + 80);

        // Marco general
        g2d.drawRect(30, 30, (int) pf.getImageableWidth() - 60, (int) pf.getImageableHeight() - 60);

        return PAGE_EXISTS;
    }

    // Integración en pestañas
    public static void agregarAPestañas(JTabbedPane pestañas) {
        RotuloPanel rotulo = new RotuloPanel();
        pestañas.addTab("Rótulo", rotulo);
    }

    // Test independiente
 
}

