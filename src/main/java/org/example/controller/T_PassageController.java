//package org.example.controller;
//
//import org.example.model.T_Passage;
//import org.example.model.Train;
//import org.example.service.PassageService;
//import org.example.service.TrainService;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/passages")
//public class T_PassageController {
//
//    private PassageService passageService
//            ;
//
//    public T_PassageController(PassageService passageService) {
//        this.passageService = passageService;
//    }
//
//    @GetMapping("/list")
//    public Iterable<T_Passage> list() {
//        return passageService.list();
//    }
//
//}
