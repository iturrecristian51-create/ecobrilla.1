package inventario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Panel de filtro reutilizable para búsquedas
 * MEJORAS FASE 2: Componente universal de filtrado
 */
public class FiltroPanel extends JPanel {
    private JTextField txtFiltro;
    private JButton btnLimpiar;
    private JLabel lblResultados;
    private Runnable onFiltroChange;
    private Timer timerBusqueda;
    
    public FiltroPanel(String placeholder) {
        initUI(placeholder);
    }
    
    public FiltroPanel(String placeholder, Runnable onFiltroChange) {
        this.onFiltroChange = onFiltroChange;
        initUI(placeholder);
    }
    
    private void initUI(String placeholder) {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        setBackground(Color.WHITE);
        
        // Icono de búsqueda
        JLabel lblBuscar = new JLabel("🔍");
        lblBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(lblBuscar);
        
        // Campo de texto
        txtFiltro = new JTextField(20);
        txtFiltro.putClientProperty("JTextField.placeholderText", placeholder);
        txtFiltro.setToolTipText("Escriba para filtrar...");
        txtFiltro.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        add(txtFiltro);
        
        // Botón limpiar
        btnLimpiar = new JButton("×");
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiar.setPreferredSize(new Dimension(30, 25));
        btnLimpiar.setToolTipText("Limpiar filtro");
        btnLimpiar.setEnabled(false);
        add(btnLimpiar);
        
        // Label para resultados
        lblResultados = new JLabel();
        lblResultados.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblResultados.setForeground(Color.GRAY);
        add(lblResultados);
        
        // Configurar timer para búsqueda en tiempo real
        timerBusqueda = new Timer(300, e -> {
            if (onFiltroChange != null) {
                onFiltroChange.run();
            }
        });
        timerBusqueda.setRepeats(false);
        
        setupListeners();
    }
    
    private void setupListeners() {
        // Evento de teclado para búsqueda en tiempo real
        txtFiltro.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                timerBusqueda.restart();
                btnLimpiar.setEnabled(!getTextoFiltro().isEmpty());
                actualizarContador();
            }
        });
        
        // Botón limpiar
        btnLimpiar.addActionListener(e -> {
            txtFiltro.setText("");
            btnLimpiar.setEnabled(false);
            if (onFiltroChange != null) {
                onFiltroChange.run();
            }
            actualizarContador();
            txtFiltro.requestFocus();
        });
    }
    
    /**
     * Obtiene el texto actual del filtro
     */
    public String getTextoFiltro() {
        return txtFiltro.getText().trim();
    }
    
    /**
     * Establece el texto del filtro
     */
    public void setTextoFiltro(String texto) {
        txtFiltro.setText(texto);
        btnLimpiar.setEnabled(!texto.isEmpty());
        actualizarContador();
    }
    
    /**
     * Limpia el filtro
     */
    public void limpiarFiltro() {
        setTextoFiltro("");
    }
    
    /**
     * Actualiza el contador de resultados
     */
    public void actualizarContador(int total, int filtrados) {
        if (getTextoFiltro().isEmpty()) {
            lblResultados.setText(total + " elementos");
        } else {
            lblResultados.setText(filtrados + " de " + total + " elementos");
        }
    }
    
    /**
     * Actualiza contador sin números específicos
     */
    public void actualizarContador() {
        if (getTextoFiltro().isEmpty()) {
            lblResultados.setText("");
        } else {
            lblResultados.setText("filtrado");
        }
    }
    
    /**
     * Establece el callback cuando cambia el filtro
     */
    public void setOnFiltroChange(Runnable callback) {
        this.onFiltroChange = callback;
    }
    
    /**
     * Obtiene el campo de texto para personalización adicional
     */
    public JTextField getCampoTexto() {
        return txtFiltro;
    }
    
    /**
     * Habilita/deshabilita el filtro
     */
    public void setHabilitado(boolean habilitado) {
        txtFiltro.setEnabled(habilitado);
        btnLimpiar.setEnabled(habilitado && !getTextoFiltro().isEmpty());
    }
}