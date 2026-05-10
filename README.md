# Toscano — Post 2 · Unidad 10

## Pruebas E2E con Selenium, Postman y Newman

Aplicación Spring Boot de **gestión de tareas** (heredada del Post 1) que añade:

1. **Pruebas End-to-End con Selenium WebDriver** aplicando el patrón **Page Object Model**.
2. **Colección Postman** con 5 requests y *test scripts* que validan status, body y persistencia entre llamadas.
3. **Workflow de GitHub Actions** que arranca la aplicación y ejecuta la colección con **Newman** en CI.

---

## Tecnologías

| Tecnología          | Versión             |
|---------------------|---------------------|
| Java                | 17                  |
| Spring Boot         | 3.4.5               |
| Thymeleaf           | (starter Spring Boot) |
| Spring Boot Actuator| (starter Spring Boot) |
| Selenium WebDriver  | 4.18.1              |
| WebDriverManager    | 5.8.0               |
| Postman             | Desktop v10+        |
| Newman              | 6.x (vía npm)       |
| Node.js             | 18+                 |

---

## Estructura del Proyecto

```
U10-Post2/
├── pom.xml
├── postman/
│   ├── ColeccionToDo.json        ← Colección de 5 requests
│   ├── env-local.json            ← Entorno para ejecución local
│   └── env-ci.json               ← Entorno para CI (GitHub Actions)
├── .github/workflows/
│   └── api-tests.yml             ← Workflow Newman
├── capturas/
│   ├── selenium-tests.png        ← Evidencia 1
│   ├── postman-runner.png        ← Evidencia 2
│   └── github-actions.png        ← Evidencia 3
├── src/main/java/com/toscano/tareas/
│   ├── TareasApplication.java
│   ├── controller/
│   │   ├── TareaController.java       ← API REST /api/tareas
│   │   └── TareaViewController.java   ← Vista Thymeleaf /tareas
│   ├── entity/Tarea.java
│   ├── repository/TareaRepository.java
│   ├── service/TareaService.java
│   └── exception/GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.properties
│   └── templates/
│       ├── tareas.html               ← Vista lista (selectores #btn-nueva, .tarea-item)
│       └── nueva-tarea.html          ← Formulario (#input-titulo, #btn-guardar)
└── src/test/java/com/toscano/tareas/
    ├── e2e/                          ← Page Object Model + tests Selenium
    │   ├── TareasPage.java
    │   ├── NuevaTareaPage.java
    │   └── TareasE2ETest.java
    ├── controller/TareaControllerTest.java
    ├── repository/TareaRepositoryTest.java
    └── service/TareaServiceTest.java
```

---

## Endpoints

| Método | Ruta                              | Descripción                                |
|--------|-----------------------------------|--------------------------------------------|
| GET    | `/tareas`                         | Vista HTML (Thymeleaf) con la lista        |
| GET    | `/tareas/nueva`                   | Formulario HTML para crear una tarea       |
| POST   | `/tareas/nueva`                   | Crea una tarea desde el formulario         |
| GET    | `/api/tareas/{id}`                | Obtiene una tarea por id (JSON)            |
| POST   | `/api/tareas`                     | Crea una tarea (JSON)                      |
| PATCH  | `/api/tareas/{id}/completar`      | Marca la tarea como completada             |
| GET    | `/actuator/health`                | Health-check usado por el workflow CI      |

---

## Checkpoint 1 — Page Object Model con Selenium

Las clases en `src/test/java/com/toscano/tareas/e2e/` encapsulan los selectores como constantes `By` privadas:

```java
public class TareasPage {
    private final By btnNueva  = By.id("btn-nueva");
    private final By listItems = By.cssSelector(".tarea-item");
    // …
}
```

`TareasE2ETest` se ejecuta con Chrome en modo `--headless=new` y `WebDriverManager` resuelve automáticamente el binario de ChromeDriver:

```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class TareasE2ETest {
    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new", "--no-sandbox");
        driver = new ChromeDriver(opts);
        driver.get("http://localhost:8080/tareas");
    }
}
```

### Tests implementados (3 en verde)

| Test                                    | Comprueba                                                          |
|-----------------------------------------|--------------------------------------------------------------------|
| `paginaTareas_cargaCorrectamente`       | El título de la página contiene "Tareas".                          |
| `clickNuevaTarea_abreFormulario`        | El botón `#btn-nueva` navega al formulario y el form es visible.   |
| `crearTarea_apareceEnLaLista`           | Tras enviar el formulario, la nueva tarea aparece en `.tarea-item`.|

### Ejecutar Selenium localmente

> **Requisito:** Google Chrome estable instalado.

```powershell
# Solo los tests E2E de Selenium
mvn -B test -Dtest=TareasE2ETest

# Toda la suite (unitarios + E2E)
mvn clean test
```

📸 **Evidencia 1:** `capturas/selenium-tests.png` — consola del IDE/terminal con los tests E2E en verde.

---

## Checkpoint 2 — Colección Postman con Test Scripts

Carpeta `postman/`:

| Archivo                | Descripción                                              |
|------------------------|----------------------------------------------------------|
| `ColeccionToDo.json`   | Colección "API ToDoApp" con 5 requests en orden.         |
| `env-local.json`       | Entorno **ToDoApp-Local** (`baseUrl=http://localhost:8080`). |
| `env-ci.json`          | Entorno **ToDoApp-CI** equivalente, usado por el workflow.   |

### Requests en orden

1. **POST `/api/tareas`** → 201 Created · guarda `tareaId` en una *collection variable*.
2. **GET `/api/tareas/{{tareaId}}`** → 200 OK · valida que `completada == false`.
3. **PATCH `/api/tareas/{{tareaId}}/completar`** → 200 OK · valida que `completada == true`.
4. **GET `/api/tareas/{{tareaId}}`** → 200 OK · vuelve a verificar que la tarea quedó completada.
5. **GET `/api/tareas/99999`** → 404 Not Found · valida el manejo de errores.

### Test scripts (extracto)

```javascript
pm.test("Status 201 Created", () => {
    pm.response.to.have.status(201);
});

pm.test("Respuesta contiene id numérico", () => {
    const b = pm.response.json();
    pm.expect(b).to.have.property("id");
    pm.collectionVariables.set("tareaId", b.id);
});

pm.test("Tiempo de respuesta < 500ms", () => {
    pm.expect(pm.response.responseTime).to.be.below(500);
});
```

### Ejecutar la colección localmente

```powershell
# 1) Levantar la aplicación
mvn spring-boot:run

# 2) En otra terminal: ejecutar Newman
npm install -g newman
newman run postman/ColeccionToDo.json --environment postman/env-local.json
```

También se puede ejecutar desde el **Postman Runner** (botón *Runner* en Postman Desktop), seleccionando la colección **API ToDoApp** y el entorno **ToDoApp-Local**. Debe mostrar **0 failures** sobre las 5 peticiones.

📸 **Evidencia 2:** `capturas/postman-runner.png` — Postman Runner con 0 failures.

---

## Checkpoint 3 — Newman en GitHub Actions

Workflow: `.github/workflows/api-tests.yml`

Pasos:

1. `actions/checkout@v4`
2. `actions/setup-java@v4` (Java 17 Temurin con caché Maven)
3. `mvn -B package -DskipTests` — empaqueta el JAR.
4. Inicia la aplicación con `nohup java -jar target/*.jar &`.
5. Espera hasta 60 s a que `/actuator/health` devuelva `{"status":"UP"}`.
6. `npm install -g newman`.
7. `newman run postman/ColeccionToDo.json --environment postman/env-ci.json --reporters cli,junit`.
8. Sube `newman-results.xml` como artefacto del job.
9. Detiene el proceso de la aplicación al finalizar.

El workflow se dispara automáticamente con cada `push` y `pull_request`.

📸 **Evidencia 3:** `capturas/github-actions.png` — pestaña *Actions* del repositorio con el job **api-test** en verde.

---

## Cómo reproducir todo localmente

```powershell
# 1) Pruebas unitarias + E2E (requiere Chrome)
mvn clean test

# 2) Solo Newman contra la app levantada
mvn spring-boot:run                           # terminal A
newman run postman/ColeccionToDo.json `
       --environment postman/env-local.json   # terminal B
```

---

## Entregables

| Elemento     | Ubicación                                                      |
|--------------|----------------------------------------------------------------|
| Repositorio  | GitHub `apellido-post2-u10` (mín. 3 commits descriptivos)      |
| Colección    | `postman/ColeccionToDo.json`                                   |
| Entornos     | `postman/env-local.json`, `postman/env-ci.json`                |
| Workflow     | `.github/workflows/api-tests.yml`                              |
| Evidencia 1  | `capturas/selenium-tests.png`                                  |
| Evidencia 2  | `capturas/postman-runner.png`                                  |
| Evidencia 3  | `capturas/github-actions.png`                                  |

---

## Autor

**Andrés Toscano** — Ingeniería de Sistemas, UDES 2026
