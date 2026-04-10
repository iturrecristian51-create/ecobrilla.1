/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import java.awt.*;
import java.awt.print.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class HistorialProductosPanel extends JPanel implements Printable {

    private JTable table;
    private DefaultTableModel model;
    private JButton btnImprimir;
    private JButton btnRefrescar;
    private JComboBox<String> comboAccion;

    public HistorialProductosPanel() {
        setLayout(new BorderLayout());

        // ✅ CORRECCIÓN: Columnas correctas para historial REAL de movimientos
        model = new DefaultTableModel(
            new String[]{"Producto", "Acción", "Cantidad", "Lote/Remisión", "Fecha", "Observación"}, 0) {
            @Override 
            public boolean isCellEditable(int row, int col) {
                return false;  // Historial es de solo lectura
            }
        };
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel de controles
        JPanel panelControles = new JPanel();
        
        // Combobox para filtrar por acción
        JLabel lblAccion = new JLabel("Filtrar por acción:");
        comboAccion = new JComboBox<>(new String[]{"Todas", "Producción", "Despacho", "Ajuste"});
        comboAccion.addActionListener(e -> cargarDatos());
        
        btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        
        // Botón imprimir
        btnImprimir = new JButton("Imprimir Historial");
        btnImprimir.addActionListener(e -> imprimirHistorial());

        panelControles.add(lblAccion);
        panelControles.add(comboAccion);
        panelControles.add(btnRefrescar);
        panelControles.add(btnImprimir);

        add(scrollPane, BorderLayout.CENTER);
        add(panelControles, BorderLayout.SOUTH);

        cargarDatos();
    }

    // ✅ CORRECCIÓN: Cargar datos del HISTORIAL REAL, no de stock actual
    private void cargarDatos() {
        model.setRowCount(0);  // Limpiar tabla
        
        String filtroAccion = (String) comboAccion.getSelectedItem();
        String sql = "SELECT fecha, nombre_producto, accion, cantidad, lote_asociado, observacion " +
                     "FROM historial_productos ";
        
        // Agregar filtro si no es "Todas"
        if (!filtroAccion.equals("Todas")) {
            sql += "WHERE accion = '" + filtroAccion + "' ";
        }
        
        sql += "ORDER BY fecha DESC LIMIT 500";  // Últimos 500 movimientos
        
        try (Connection conn = ConexionSQLite.conectar(); 
             Statement stmt = conn.createStatement()) {

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String fecha = rs.getString("fecha");
                String producto = rs.getString("nombre_producto");
                String accion = rs.getString("accion");
                int cantidad = rs.getInt("cantidad");
                String lote = rs.getString("lote_asociado");
                String observacion = rs.getString("observacion");
                
                model.addRow(new Object[]{producto, accion, cantidad, lote, fecha, observacion});
            }
            
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay movimientos registrados.");
            } else {
                System.out.println("✅ Historial cargado: " + model.getRowCount() + " movimientos");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error cargando historial: " + e.getMessage());
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

        g.drawString("=== HISTORIAL DE MOVIMIENTOS DE PRODUCTOS ===", 50, 30);
        g.drawString("Generado: " + new java.util.Date(), 50, 50);

        int y = 80;
        g.drawString("Producto | Acción | Cantidad | Lote/Remisión | Fecha | Observación", 30, y);
        y += 20;
        g.drawString("─".repeat(130), 30, y);
        y += 20;
        
        for(int row = 0; row < Math.min(model.getRowCount(), 30); row++) {  // Máx 30 por página
            String linea = 
                model.getValueAt(row, 0) + " | " +
                model.getValueAt(row, 1) + " | " +
                model.getValueAt(row, 2) + " | " +
                model.getValueAt(row, 3) + " | " +
                model.getValueAt(row, 4) + " | " +
                model.getValueAt(row, 5);
            g.drawString(linea, 30, y);
            y += 15;
        }
        return PAGE_EXISTS;
    }
}
