package org.example.service;

import org.example.model.Mr;
import org.example.model.Sam;
import org.example.repository.MrRepository;
import org.example.repository.SamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MrService {


    private final MrRepository mrRepository ;

    public MrService(MrRepository mrRepository){
        this.mrRepository = mrRepository;
    }

    public Iterable<Mr> list() {
        return mrRepository.findAll();
    }

    public Mr save(Mr mr) {
        return mrRepository.save(mr);
    }

    public void save(List<Mr> mrs) {
        mrRepository.saveAll(mrs);
    }
}
