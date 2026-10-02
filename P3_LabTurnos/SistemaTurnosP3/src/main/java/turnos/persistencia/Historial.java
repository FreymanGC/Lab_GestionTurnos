package turnos.persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Historial {
    private final Path archivo;

    public Historial(Path archivo) {
        this.archivo = archivo;
    }

    public void registrar(String evento) {
        String linea = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                + " - " + evento + System.lineSeparator();
        try {
            Path carpeta = archivo.getParent();
            if (carpeta != null && !Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }
            Files.writeString(archivo, linea, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo escribir en " + archivo, e);
        }
    }

    public List<String> leer() {
        if (!Files.exists(archivo)) {
            return List.of();
        }
        try {
            return Files.readAllLines(archivo, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer " + archivo, e);
        }
    }
}
