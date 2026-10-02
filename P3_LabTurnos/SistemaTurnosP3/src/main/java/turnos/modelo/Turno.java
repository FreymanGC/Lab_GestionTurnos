package turnos.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

public class Turno {
    private final int numero;
    private EstadoTurno estado;
    private final LocalDateTime fechaHora;

    public Turno(int numero, EstadoTurno estado, LocalDateTime fechaHora) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de turno debe ser positivo");
        }
        this.numero = numero;
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha es obligatoria");
    }

    public int getNumero() { return numero; }
    public EstadoTurno getEstado() { return estado; }
    public LocalDateTime getFechaHora() { return fechaHora; }

    public void setEstado(EstadoTurno estado) {
        this.estado = Objects.requireNonNull(estado);
    }

    @Override
    public String toString() {
        return String.format("Turno %03d [%s] %s", numero, estado, fechaHora);
    }
}
