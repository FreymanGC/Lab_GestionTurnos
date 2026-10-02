package turnos.app;

import java.nio.file.Path;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import turnos.controlador.TurnoController;
import turnos.gui.VentanaTurnos;
import turnos.persistencia.ArchivoTurnoRepository;
import turnos.persistencia.Historial;
import turnos.persistencia.PersistenciaException;
import turnos.persistencia.TurnoRepository;
import turnos.servicio.TurnoService;

public class AppGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Historial historial = new Historial(Path.of("data", "historial.txt"));

                TurnoRepository repositorio = new ArchivoTurnoRepository(Path.of("data", "turnos.txt"));

                TurnoService servicio = new TurnoService(repositorio);
                TurnoController controller = new TurnoController(servicio, historial);
                historial.registrar("Aplicación gráfica iniciada");

                new VentanaTurnos(controller).setVisible(true);
            } catch (PersistenciaException e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo iniciar el almacenamiento: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
