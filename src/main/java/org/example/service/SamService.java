package org.example.service;

import org.example.model.Sam;
import org.example.repository.SamRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SamService {

    private final SamRepository samRepository ;

    public SamService(SamRepository samRepository){
        this.samRepository = samRepository;
    }

    public Iterable<Sam> list() {
        return samRepository.findAll();
    }

    public Sam save(Sam sam) {
        return samRepository.save(sam);
    }

    public void save(List<Sam> sams) {
        samRepository.saveAll(sams);
    }
}
