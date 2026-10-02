package turnos.persistencia;

import java.util.List;
import java.util.Optional;
import turnos.modelo.Turno;

public interface TurnoRepository {
    void guardar(Turno turno);

    void actualizar(Turno turno);

    List<Turno> listar();

    Optional<Turno> buscarPorNumero(int numero);
}
