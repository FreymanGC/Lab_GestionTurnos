package turnos.persistencia;

import java.util.List;
import java.util.Optional;
import turnos.modelo.Turno;

public class OracleTurnoRepository implements TurnoRepository {
    @Override
    public void guardar(Turno turno) {
        throw new UnsupportedOperationException("Pendiente: implementar con JDBC");
    }

    @Override
    public void actualizar(Turno turno) {
        throw new UnsupportedOperationException("Pendiente: implementar con JDBC");
    }

    @Override
    public List<Turno> listar() {
        throw new UnsupportedOperationException("Pendiente: implementar con JDBC");
    }

    @Override
    public Optional<Turno> buscarPorNumero(int numero) {
        throw new UnsupportedOperationException("Pendiente: implementar con JDBC");
    }
}
