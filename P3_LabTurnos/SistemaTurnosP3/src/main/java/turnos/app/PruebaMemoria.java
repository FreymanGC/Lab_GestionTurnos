package turnos.app;

import turnos.persistencia.MemoriaTurnoRepository;
import turnos.persistencia.TurnoRepository;
import turnos.servicio.TurnoService;

public class PruebaMemoria {
    public static void main(String[] args) {
        TurnoRepository repositorio = new MemoriaTurnoRepository();
        TurnoService servicio = new TurnoService(repositorio);

        servicio.crearTurno();
        servicio.crearTurno();
        servicio.crearTurno();
        servicio.llamarSiguiente();
        servicio.atender(1);

        System.out.println("== Turnos (repositorio en memoria) ==");
        servicio.listarTodos().forEach(System.out::println);
        System.out.println("Conteo por estado: " + servicio.contarPorEstado());

        try {
            servicio.registrar(1);
        } catch (IllegalStateException e) {
            System.out.println("\nExperimento 2 -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage() + " (regla en TurnoService.registrar)");
        }

        try {
            servicio.atender(3);
        } catch (IllegalStateException e) {
            System.out.println("Experimento 3 -> " + e.getClass().getSimpleName()
                    + ": " + e.getMessage() + " (regla en TurnoService.atender)");
        }
    }
}
