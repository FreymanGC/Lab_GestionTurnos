# SistemaTurnos — Persistencia con archivos y patrón Repository

EIF206 – Programación III · Java 21 · NetBeans (proyecto Maven)

## 1. Cómo abrir y ejecutar en NetBeans

1. Descomprimir `SistemaTurnos.zip`.
2. NetBeans → **File → Open Project** → seleccionar la carpeta `SistemaTurnos` (tiene el ícono de Maven).
3. Clic derecho al proyecto → **Clean and Build** (la primera vez descarga los plugins de Maven; requiere internet).
4. Ejecutar:

| Qué | Cómo | Para qué |
|---|---|---|
| `turnos.app.Main` | **Run Project (F6)** | Ejercicio guiado en consola: guardar, listar, buscar con `Optional`, actualizar |
| `turnos.app.AppGUI` | Clic derecho al archivo → **Run File (Shift+F6)** | Laboratorio aplicado: ventana Swing |
| `turnos.app.PruebaMemoria` | Clic derecho → **Run File** | Reto final: mismo servicio con repositorio en memoria |

La carpeta `data/` se crea **desde el programa** en la raíz del proyecto (NetBeans ejecuta desde ahí). Las rutas son relativas: `Path.of("data", "turnos.txt")`.

**Prueba de persistencia:** ejecute `Main` dos veces. En la segunda, los turnos 001–003 siguen ahí (001 como `ATENDIDO`) y los nuevos se numeran 004, 005, 006.

## 2. Estructura

```
SistemaTurnos/
├── pom.xml
├── README.md
├── data/                      ← lo crea el programa
│   ├── turnos.txt
│   └── historial.txt
└── src/main/java/turnos/
    ├── modelo/        EstadoTurno, Turno
    ├── persistencia/  TurnoRepository (interfaz), ArchivoTurnoRepository,
    │                  MemoriaTurnoRepository, OracleTurnoRepository (esqueleto),
    │                  Historial, PersistenciaException
    ├── servicio/      TurnoService
    ├── controlador/   TurnoController
    ├── gui/           VentanaTurnos
    └── app/           Main, AppGUI, PruebaMemoria
```

> Se usó Maven (la guía permite Ant o Maven), por eso el código va en `src/main/java/`. Los paquetes son exactamente los de la guía.

## 3. Explicación del diseño

Flujo:

```
VentanaTurnos → TurnoController → TurnoService → TurnoRepository (interfaz) → ArchivoTurnoRepository → data/turnos.txt
```

| Paquete | Responsabilidad | ¿Conoce archivos? |
|---|---|---|
| `modelo` | Qué es un turno; valida sus datos (número positivo, estado y fecha obligatorios) | No |
| `persistencia` | Guarda y recupera turnos; único que conoce el formato `001;PENDIENTE;2026-09-25T10:15:00` | **Sí, es el único** |
| `servicio` | Reglas de negocio: numeración consecutiva, sin duplicados, solo se atiende un turno LLAMADO | No |
| `controlador` | Traduce acciones del usuario a llamadas del servicio y registra el historial | No |
| `gui` | Muestra datos y botones; no usa `Files`, `Path` ni el repositorio | No |
| `app` | Arma las piezas. **Único lugar** donde se elige la implementación del repositorio | Solo elige la ruta |

**Decisiones clave**

- `TurnoService` depende de la **interfaz** `TurnoRepository`, no de `ArchivoTurnoRepository` (inyección por constructor).
- `buscarPorNumero` devuelve `Optional<Turno>`: nunca `null`.
- `guardar` agrega con `APPEND`; `actualizar` lee todo, reemplaza la línea y reescribe el archivo (en texto no se puede editar una línea en su lugar).
- `listar` usa `BufferedReader` en **try-with-resources**.
- Errores en tres niveles: archivo inexistente → lista vacía; línea inválida → se informa y se ignora; `IOException` → se envuelve en `PersistenciaException` con la causa. No hay `catch` vacíos.
- Consulta con Streams + `Collectors.groupingBy`: `TurnoService.contarPorEstado()` (se muestra en consola y en la barra superior de la ventana).

## 4. Checklist del laboratorio (sección 9)

- [x] Crear el directorio `data` desde el programa → `ArchivoTurnoRepository.crearCarpetaSiNoExiste()` y `Historial.registrar()`
- [x] Persistir los turnos en un archivo → `data/turnos.txt`
- [x] Recuperarlos al iniciar → `VentanaTurnos` llama `refrescar()` en el constructor
- [x] Consultar los turnos pendientes → `listarPendientes()` (lista derecha de la ventana)
- [x] Cambiar el estado → botones *Llamar siguiente* y *Atender turno*
- [x] Ventanas sin código de persistencia
- [x] Interfaz Repository → `TurnoRepository`
- [x] `Optional` en búsquedas → `buscarPorNumero`, `buscar`, `llamarSiguiente`
- [x] Streams con `Collectors.groupingBy` → `contarPorEstado()`
- [x] Errores de lectura/escritura sin `catch` vacíos

## 5. Evidencias (sección 13)

- [x] `Turno` y el dominio funcionando → `modelo/`
- [x] `TurnoRepository` → `persistencia/TurnoRepository.java`
- [x] `ArchivoTurnoRepository` → `persistencia/ArchivoTurnoRepository.java`
- [x] Servicio que usa el Repository → `servicio/TurnoService.java`
- [x] Archivo de datos generado → `data/turnos.txt`, `data/historial.txt`
- [x] Prueba de guardar, listar, buscar y actualizar → `app/Main.java`
- [x] README con la explicación del diseño → este archivo

## 6. Respuestas a las preguntas de la guía

### Preguntas para reflexionar (sección 2)

1. **¿Qué pasa con los turnos al cerrar el programa?** Sin persistencia se pierden: vivían en un `ArrayList` en la memoria RAM, que se libera al terminar el proceso.
2. **¿Es lo mismo guardar un objeto que guardar su estado?** No. Se guarda el **estado** (número, estado, fecha) en un formato de texto; al leerlo se construye un objeto `Turno` nuevo con esos datos.
3. **¿Quién debe conocer el formato del archivo?** Solo el repositorio (`ArchivoTurnoRepository`).

### Preguntas de diseño (sección 5)

1. **Si un dato contiene `;`**, `split` produce más campos y la línea queda corrupta. En este sistema no ocurre porque los campos son `int`, un `enum` y una fecha ISO; si se agrega texto libre (ej. nombre del cliente) hay que validarlo o escapar el separador.
2. **Agregar un estado** no rompe archivos viejos; **eliminar uno** sí: `valueOf` falla en líneas antiguas (y `listar` las ignoraría con aviso).
3. **Si cambia `Turno`** (ej. campo `cliente`), cambia el formato: hay que decidir cómo leer archivos anteriores (por ejemplo aceptar 3 o 4 campos).
4. **¿Quién conoce el formato?** Solo el repositorio. Con Oracle el formato desaparece y nadie más lo nota.

### Experimentos sugeridos (sección 7)

1. Línea inválida `abc;PENDIENTE` → se imprime `Línea N ignorada (formato inválido): abc;PENDIENTE` y el resto de turnos se carga normal.
2. `servicio.registrar(1)` con el turno 1 existente → `IllegalStateException: Ya existe el turno 1`. Lo impide la regla en `TurnoService.registrar`.
3. `servicio.atender(3)` sin llamarlo → `IllegalStateException: El turno 3 no ha sido llamado` (regla en `TurnoService.atender`).

Los experimentos 2 y 3 se ejecutan automáticamente en `PruebaMemoria`.

### Reto final (sección 10)

Para pasar a Oracle solo se completa `OracleTurnoRepository` (ya está el esqueleto con los SQL) y se cambia **una línea** en `Main`/`AppGUI`:

```java
TurnoRepository repositorio = new OracleTurnoRepository(/* datos de conexión */);
```

`PruebaMemoria` demuestra que `TurnoService` funciona igual con otra implementación (`MemoriaTurnoRepository`), así que la arquitectura está lista para JDBC.

### Autoevaluación (sección 14)

1. **¿Por qué no conviene que `TurnoService` use `Files`?** Porque mezclaría reglas de negocio con detalles de almacenamiento: cambiar a Oracle obligaría a reescribir la lógica, y no se podría probar el servicio sin archivos.
2. **¿Qué ventaja ofrece Repository?** Separa el *qué* (contrato) del *cómo* (implementación). Se puede cambiar archivo → memoria → Oracle sin tocar servicio, controlador ni ventanas.
3. **¿Qué cambiaría para usar Oracle?** Crear `OracleTurnoRepository` con JDBC (`INSERT`, `UPDATE`, `SELECT`) y cambiar la línea que crea el repositorio en `Main`/`AppGUI`. Nada más.
4. **¿Dónde vive la regla que impide un turno duplicado?** En el **servicio** (`TurnoService.registrar`), no en el repositorio ni en la ventana.

## 7. Archivos frente a base de datos

| Aspecto | Archivo de texto | Base de datos (Oracle) |
|---|---|---|
| Actualizar un registro | Reescribir todo el archivo | `UPDATE` de una fila |
| Buscar | Leer todo y filtrar | Índices y `WHERE` |
| Acceso simultáneo | Riesgo de corromper datos | Transacciones y bloqueos |
| Integridad | La valida nuestro código | Llaves primarias, restricciones |
| Consultas complejas | Programarlas a mano | SQL |
| Cambios de estructura | Formato frágil (`;`, campos nuevos) | `ALTER TABLE` controlado |
