package com.toscano.tareas.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TareasE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    private String baseUrl() {
        return "http://localhost:" + port + "/tareas";
    }

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage",
                "--disable-gpu", "--window-size=1280,800");

        driver = new ChromeDriver(opts);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(baseUrl());
    }

    @Test
    @DisplayName("La página /tareas carga correctamente con título 'Tareas'")
    void paginaTareas_cargaCorrectamente() {
        wait.until(ExpectedConditions.titleContains("Tareas"));

        TareasPage page = new TareasPage(driver);

        assertThat(page.tituloPagina()).contains("Tareas");
    }

    @Test
    @DisplayName("Al hacer clic en 'Nueva tarea' se navega al formulario de creación")
    void clickNuevaTarea_abreFormulario() {
        TareasPage page = new TareasPage(driver);

        NuevaTareaPage formulario = page.irANuevaTarea();

        wait.until(ExpectedConditions.urlContains("/tareas/nueva"));
        assertThat(formulario.formularioVisible()).isTrue();
    }

    @Test
    @DisplayName("Crear una tarea desde el formulario la añade a la lista")
    void crearTarea_apareceEnLaLista() {
        TareasPage page = new TareasPage(driver);
        int conteoInicial = page.contarTareas();

        page.irANuevaTarea()
                .escribirTitulo("Tarea E2E")
                .escribirDescripcion("Creada por Selenium")
                .guardar();

        wait.until(ExpectedConditions.urlMatches(".*/tareas/?$"));
        TareasPage despues = new TareasPage(driver);

        assertThat(despues.contarTareas()).isEqualTo(conteoInicial + 1);
        assertThat(driver.getPageSource()).contains("Tarea E2E");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
