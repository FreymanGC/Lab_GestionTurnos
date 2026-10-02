package turnos.persistencia;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import turnos.modelo.EstadoTurno;
import turnos.modelo.Turno;

public class ArchivoTurnoRepository implements TurnoRepository {
    private static final String SEPARADOR = ";";
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Path archivo;

    public ArchivoTurnoRepository(Path archivo) {
        this.archivo = archivo;
        crearCarpetaSiNoExiste();
    }

    @Override
    public void guardar(Turno turno) {
        String linea = aLinea(turno) + System.lineSeparator();
        try {
            Files.writeString(archivo, linea, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo guardar el turno " + turno.getNumero(), e);
        }
    }

    @Override
    public void actualizar(Turno turnoActualizado) {
        List<Turno> turnos = listar();
        List<String> lineas = new ArrayList<>();
        boolean encontrado = false;

        for (Turno t : turnos) {
            if (t.getNumero() == turnoActualizado.getNumero()) {
                lineas.add(aLinea(turnoActualizado));
                encontrado = true;
            } else {
                lineas.add(aLinea(t));
            }
        }
        if (!encontrado) {
            throw new IllegalArgumentException("No existe el turno " + turnoActualizado.getNumero());
        }
        try {
            Files.write(archivo, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo actualizar el archivo " + archivo, e);
        }
    }

    @Override
    public List<Turno> listar() {
        if (!Files.exists(archivo)) {
            return List.of();
        }
        List<Turno> turnos = new ArrayList<>();

        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            int numeroLinea = 0;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                if (linea.isBlank()) {
                    continue;
                }
                try {
                    turnos.add(desdeLinea(linea));
                } catch (IllegalArgumentException | DateTimeParseException e) {
                    System.err.println("Línea " + numeroLinea + " ignorada (formato inválido): " + linea);
                }
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo " + archivo, e);
        }
        return turnos;
    }

    @Override
    public Optional<Turno> buscarPorNumero(int numero) {
        return listar().stream()
                .filter(t -> t.getNumero() == numero)
                .findFirst();
    }

    private String aLinea(Turno t) {
        return String.format("%03d%s%s%s%s",
                t.getNumero(), SEPARADOR,
                t.getEstado().name(), SEPARADOR,
                t.getFechaHora().format(FORMATO_FECHA));
    }

    private Turno desdeLinea(String linea) {
        String[] partes = linea.split(SEPARADOR);
        if (partes.length != 3) {
            throw new IllegalArgumentException("Se esperaban 3 campos y hay " + partes.length);
        }
        int numero = Integer.parseInt(partes[0].trim());
        EstadoTurno estado = EstadoTurno.valueOf(partes[1].trim());
        LocalDateTime fecha = LocalDateTime.parse(partes[2].trim(), FORMATO_FECHA);
        return new Turno(numero, estado, fecha);
    }

    private void crearCarpetaSiNoExiste() {
        try {
            Path carpeta = archivo.getParent();
            if (carpeta != null) {
                Files.createDirectories(carpeta);
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo crear la carpeta de datos", e);
        }
    }
}
