package org.example.controller;

import org.example.model.Sam;
import org.example.model.Train;
import org.example.service.SamService;
import org.example.service.TrainService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trains")

public class TrainController {

    private TrainService trainService;

    public TrainController(TrainService trainService) {
        this.trainService = trainService;
    }

    @GetMapping("/list")
    public Iterable<Train> list() {
        return trainService.list();
    }

}
