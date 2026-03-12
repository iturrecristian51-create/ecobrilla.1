/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class HistorialProductosPanel extends JPanel implements Printable {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnImprimir;

    public HistorialProductosPanel() {
        setLayout(new BorderLayout());

        // Modelo de tabla
        model = new DefaultTableModel(new String[]{"Producto", "Cantidad", "Fecha Producción"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Botón imprimir
        btnImprimir = new JButton("Imprimir Historial");
        btnImprimir.addActionListener(e -> imprimirHistorial());

        add(scrollPane, BorderLayout.CENTER);
        add(btnImprimir, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void cargarDatos() {
        try (Connection conn = ConexionSQLite.conectar(); 
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT * FROM productos_terminados ORDER BY fecha_produccion DESC");
            while (rs.next()) {
                String nombre = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                String fecha = rs.getString("fecha_produccion");
                model.addRow(new Object[]{nombre, cantidad, fecha});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando historial: " + e.getMessage());
        }
    }

    private void imprimirHistorial() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if(ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {
        if(pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        g.drawString("=== HISTORIAL DE PRODUCTOS TERMINADOS ===", 100, 50);

        int y = 80;
        for(int row = 0; row < model.getRowCount(); row++) {
            g.drawString(
                model.getValueAt(row, 0) + " | " +
                model.getValueAt(row, 1) + " | " +
                model.getValueAt(row, 2), 
                50, y
            );
            y += 20;
        }
        return PAGE_EXISTS;
    }

   

    // Test independiente
  
}
