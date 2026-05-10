package com.toscano.tareas.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class NuevaTareaPage {

    private final WebDriver driver;

    private final By inputTitulo = By.id("input-titulo");
    private final By inputDescripcion = By.id("input-descripcion");
    private final By btnGuardar = By.id("btn-guardar");
    private final By formNueva = By.id("form-nueva-tarea");

    public NuevaTareaPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean formularioVisible() {
        return driver.findElement(formNueva).isDisplayed();
    }

    public NuevaTareaPage escribirTitulo(String titulo) {
        driver.findElement(inputTitulo).sendKeys(titulo);
        return this;
    }

    public NuevaTareaPage escribirDescripcion(String descripcion) {
        driver.findElement(inputDescripcion).sendKeys(descripcion);
        return this;
    }

    public TareasPage guardar() {
        driver.findElement(btnGuardar).click();
        return new TareasPage(driver);
    }
}
