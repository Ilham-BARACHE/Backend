package org.example.controller;

import org.example.model.M_50592;
import org.example.model.Sam;
import org.example.service.M_50592Service;
import org.example.service.SamService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/50592")
public class M_50592Controller {



    private M_50592Service m50592Service;

    public M_50592Controller(M_50592Service m50592Service) {
        this.m50592Service = m50592Service;
    }

    @GetMapping("/list")
    public Iterable<M_50592> list() {
        return m50592Service.list();
    }
}
