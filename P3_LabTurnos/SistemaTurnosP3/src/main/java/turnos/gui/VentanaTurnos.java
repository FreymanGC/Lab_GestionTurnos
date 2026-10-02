package turnos.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import turnos.controlador.TurnoController;
import turnos.modelo.Turno;
import turnos.persistencia.PersistenciaException;

public final class VentanaTurnos extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient TurnoController controller;

    private final JLabel lblMensaje = new JLabel("Bienvenido", SwingConstants.CENTER);
    private final JLabel lblResumen = new JLabel(" ", SwingConstants.CENTER);
    private final DefaultTableModel modeloTabla =
            new DefaultTableModel(new Object[]{"Número", "Estado", "Fecha y hora"}, 0) {
                @Override
                public boolean isCellEditable(int fila, int columna) {
                    return false;
                }
            };
    private final JTable tablaTurnos = new JTable(modeloTabla);
    private final DefaultListModel<String> modeloPendientes = new DefaultListModel<>();

    public VentanaTurnos(TurnoController controller) {
        super("Sistema de Gestión de Turnos");
        this.controller = controller;
        construirInterfaz();
        refrescar();
    }

    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JButton btnSolicitar = new JButton("Solicitar turno");
        JButton btnLlamar = new JButton("Llamar siguiente");
        JButton btnAtender = new JButton("Atender turno");
        JButton btnHistorial = new JButton("Ver historial");
        JButton btnRefrescar = new JButton("Refrescar");

        btnSolicitar.addActionListener(e -> btnSolicitarActionPerformed());
        btnLlamar.addActionListener(e -> btnLlamarActionPerformed());
        btnAtender.addActionListener(e -> btnAtenderActionPerformed());
        btnHistorial.addActionListener(e -> btnHistorialActionPerformed());
        btnRefrescar.addActionListener(e -> refrescar());

        JPanel panelBotones = new JPanel(new FlowLayout());
        panelBotones.add(btnSolicitar);
        panelBotones.add(btnLlamar);
        panelBotones.add(btnAtender);
        panelBotones.add(btnHistorial);
        panelBotones.add(btnRefrescar);

        lblMensaje.setFont(lblMensaje.getFont().deriveFont(Font.BOLD, 20f));
        JPanel panelSuperior = new JPanel(new GridLayout(3, 1));
        panelSuperior.add(lblMensaje);
        panelSuperior.add(lblResumen);
        panelSuperior.add(panelBotones);

        tablaTurnos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTabla = new JScrollPane(tablaTurnos);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Todos los turnos"));

        JList<String> listaPendientes = new JList<>(modeloPendientes);
        JScrollPane scrollPendientes = new JScrollPane(listaPendientes);
        scrollPendientes.setBorder(BorderFactory.createTitledBorder("Pendientes (en orden)"));

        JPanel centro = new JPanel(new GridLayout(1, 2, 8, 8));
        centro.add(scrollTabla);
        centro.add(scrollPendientes);

        add(panelSuperior, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
    }

    private void btnSolicitarActionPerformed() {
        try {
            lblMensaje.setText(controller.solicitarTurno());
            refrescar();
        } catch (PersistenciaException e) {
            mostrarError("No se pudo guardar el turno: " + e.getMessage());
        }
    }

    private void btnLlamarActionPerformed() {
        try {
            lblMensaje.setText(controller.llamarSiguiente());
            refrescar();
        } catch (PersistenciaException e) {
            mostrarError("No se pudo actualizar el turno: " + e.getMessage());
        }
    }

    private void btnAtenderActionPerformed() {
        Integer numero = numeroSeleccionadoOPedido();
        if (numero == null) {
            return;
        }
        try {
            lblMensaje.setText(controller.atender(numero));
            refrescar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            mostrarError("Regla de negocio: " + e.getMessage());
        } catch (PersistenciaException e) {
            mostrarError("No se pudo actualizar el turno: " + e.getMessage());
        }
    }

    private void btnHistorialActionPerformed() {
        try {
            List<String> lineas = controller.leerHistorial();
            String texto = lineas.isEmpty() ? "(historial vacío)" : String.join("\n", lineas);
            javax.swing.JTextArea area = new javax.swing.JTextArea(texto, 15, 50);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Historial",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (PersistenciaException e) {
            mostrarError("No se pudo leer el historial: " + e.getMessage());
        }
    }

    private Integer numeroSeleccionadoOPedido() {
        int fila = tablaTurnos.getSelectedRow();
        if (fila >= 0) {
            return Integer.parseInt(modeloTabla.getValueAt(fila, 0).toString());
        }
        String entrada = JOptionPane.showInputDialog(this, "Número de turno a atender:");
        if (entrada == null || entrada.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            mostrarError("Debe digitar un número entero válido.");
            return null;
        }
    }

    private void refrescar() {
        try {
            modeloTabla.setRowCount(0);
            for (Turno t : controller.listarTodos()) {
                modeloTabla.addRow(new Object[]{
                    String.format("%03d", t.getNumero()), t.getEstado(), t.getFechaHora()});
            }
            modeloPendientes.clear();
            for (Turno t : controller.listarPendientes()) {
                modeloPendientes.addElement(t.toString());
            }
            lblResumen.setText(controller.resumenPorEstado());
        } catch (PersistenciaException e) {
            mostrarError("No se pudieron cargar los turnos: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
