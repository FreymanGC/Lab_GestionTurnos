package turnos.persistencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import turnos.modelo.Turno;

public class MemoriaTurnoRepository implements TurnoRepository {
    private final List<Turno> turnos = new ArrayList<>();

    @Override
    public void guardar(Turno turno) {
        turnos.add(turno);
    }

    @Override
    public void actualizar(Turno turno) {
        for (int i = 0; i < turnos.size(); i++) {
            if (turnos.get(i).getNumero() == turno.getNumero()) {
                turnos.set(i, turno);
                return;
            }
        }
        throw new IllegalArgumentException("No existe el turno " + turno.getNumero());
    }

    @Override
    public List<Turno> listar() {
        return List.copyOf(turnos);
    }

    @Override
    public Optional<Turno> buscarPorNumero(int numero) {
        return turnos.stream().filter(t -> t.getNumero() == numero).findFirst();
    }
}
