//package org.example.service;
//
//import org.example.model.T_Passage;
//import org.example.model.Train;
//import org.example.repository.PassageRepository;
//import org.example.repository.TrainRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class PassageService {
//
//
//    private final PassageRepository passageRepository;
//
//
//    public PassageService(PassageRepository passageRepository) {
//        this.passageRepository = passageRepository;
//
//
//    }
//
//    public Iterable<T_Passage> list() {
//        return passageRepository.findAll();
//    }
//
//
//    public T_Passage save(T_Passage passage) {
//        return passageRepository.save(passage);
//    }
//
//
//    public void save(List<T_Passage> passages) {
//
//        passageRepository.saveAll(passages);
//    }
//
//    public T_Passage findById(Long id) {
//        return passageRepository.findById(id).orElse(null);
//    }
//}
