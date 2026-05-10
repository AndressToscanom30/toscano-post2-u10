package com.toscano.tareas.e2e;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TareasPage {

    private final WebDriver driver;

    private final By btnNueva = By.id("btn-nueva");
    private final By listItems = By.cssSelector(".tarea-item");
    private final By tituloTarea = By.cssSelector(".tarea-item .tarea-titulo");
    private final By mensajeVacio = By.id("mensaje-vacio");

    public TareasPage(WebDriver driver) {
        this.driver = driver;
    }

    public int contarTareas() {
        return driver.findElements(listItems).size();
    }

    public boolean estaVacia() {
        return !driver.findElements(mensajeVacio).isEmpty();
    }

    public String tituloPagina() {
        return driver.getTitle();
    }

    public String primerTitulo() {
        return driver.findElement(tituloTarea).getText();
    }

    public NuevaTareaPage irANuevaTarea() {
        driver.findElement(btnNueva).click();
        return new NuevaTareaPage(driver);
    }
}
