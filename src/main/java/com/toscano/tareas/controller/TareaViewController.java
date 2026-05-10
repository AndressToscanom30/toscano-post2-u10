package com.toscano.tareas.controller;

import com.toscano.tareas.entity.Tarea;
import com.toscano.tareas.service.TareaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tareas")
public class TareaViewController {

    private final TareaService service;
    private final com.toscano.tareas.repository.TareaRepository repo;

    public TareaViewController(TareaService service,
                               com.toscano.tareas.repository.TareaRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tareas", repo.findAll());
        return "tareas";
    }

    @GetMapping("/nueva")
    public String formularioNueva(Model model) {
        model.addAttribute("tarea", new Tarea());
        return "nueva-tarea";
    }

    @PostMapping("/nueva")
    public String crear(@ModelAttribute Tarea tarea) {
        service.crear(tarea);
        return "redirect:/tareas";
    }
}
