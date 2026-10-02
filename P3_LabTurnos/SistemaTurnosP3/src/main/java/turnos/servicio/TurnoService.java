package turnos.servicio;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import turnos.modelo.EstadoTurno;
import turnos.modelo.Turno;
import turnos.persistencia.TurnoRepository;

public class TurnoService {
    private final TurnoRepository repositorio;

    public TurnoService(TurnoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Turno crearTurno() {
        int siguiente = repositorio.listar().stream()
                .mapToInt(Turno::getNumero)
                .max()
                .orElse(0) + 1;
        return registrar(siguiente);
    }

    public Turno registrar(int numero) {
        if (repositorio.buscarPorNumero(numero).isPresent()) {
            throw new IllegalStateException("Ya existe el turno " + numero);
        }
        Turno nuevo = new Turno(numero, EstadoTurno.PENDIENTE,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        repositorio.guardar(nuevo);
        return nuevo;
    }

    public Optional<Turno> llamarSiguiente() {
        Optional<Turno> siguiente = listarPendientes().stream().findFirst();
        siguiente.ifPresent(t -> cambiarEstado(t, EstadoTurno.LLAMADO));
        return siguiente;
    }

    public Turno atender(int numero) {
        Turno turno = repositorio.buscarPorNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("No existe el turno " + numero));
        if (turno.getEstado() != EstadoTurno.LLAMADO) {
            throw new IllegalStateException("El turno " + numero + " no ha sido llamado");
        }
        cambiarEstado(turno, EstadoTurno.ATENDIDO);
        return turno;
    }

    public List<Turno> listarPendientes() {
        return repositorio.listar().stream()
                .filter(t -> t.getEstado() == EstadoTurno.PENDIENTE)
                .sorted(Comparator.comparing(Turno::getFechaHora)
                        .thenComparing(Turno::getNumero))
                .toList();
    }

    public List<Turno> listarTodos() {
        return repositorio.listar();
    }

    public Optional<Turno> buscar(int numero) {
        return repositorio.buscarPorNumero(numero);
    }

    public Map<EstadoTurno, Long> contarPorEstado() {
        Map<EstadoTurno, Long> conteo = repositorio.listar().stream()
                .collect(Collectors.groupingBy(Turno::getEstado,
                        () -> new EnumMap<>(EstadoTurno.class),
                        Collectors.counting()));
        for (EstadoTurno estado : EstadoTurno.values()) {
            conteo.putIfAbsent(estado, 0L);
        }
        return conteo;
    }

    private void cambiarEstado(Turno turno, EstadoTurno nuevo) {
        turno.setEstado(nuevo);
        repositorio.actualizar(turno);
    }
}
