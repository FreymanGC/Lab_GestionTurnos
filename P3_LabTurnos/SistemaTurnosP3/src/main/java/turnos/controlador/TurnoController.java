package turnos.controlador;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import turnos.modelo.EstadoTurno;
import turnos.modelo.Turno;
import turnos.persistencia.Historial;
import turnos.servicio.TurnoService;

public class TurnoController {
    private final TurnoService servicio;
    private final Historial historial;

    public TurnoController(TurnoService servicio, Historial historial) {
        this.servicio = servicio;
        this.historial = historial;
    }

    public String solicitarTurno() {
        Turno t = servicio.crearTurno();
        historial.registrar("Creado turno " + t.getNumero());
        return "Su turno es el " + String.format("%03d", t.getNumero());
    }

    public String llamarSiguiente() {
        Optional<Turno> llamado = servicio.llamarSiguiente();
        llamado.ifPresent(t -> historial.registrar("Llamado turno " + t.getNumero()));
        return llamado
                .map(t -> String.format("Llamando al turno %03d", t.getNumero()))
                .orElse("No hay turnos pendientes");
    }

    public String atender(int numero) {
        Turno t = servicio.atender(numero);
        historial.registrar("Atendido turno " + t.getNumero());
        return String.format("Turno %03d atendido", t.getNumero());
    }

    public List<Turno> listarTodos() {
        return servicio.listarTodos();
    }

    public List<Turno> listarPendientes() {
        return servicio.listarPendientes();
    }

    public String resumenPorEstado() {
        Map<EstadoTurno, Long> conteo = servicio.contarPorEstado();
        return conteo.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("   |   "));
    }

    public List<String> leerHistorial() {
        return historial.leer();
    }
}
