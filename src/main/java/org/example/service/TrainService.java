package org.example.service;

import org.example.model.Sam;
import org.example.model.Train;
import org.example.repository.SamRepository;
import org.example.repository.TrainRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainService {

    private final TrainRepository trainRepository;
    private final SamRepository samRepository;

    public TrainService(TrainRepository trainRepository, SamRepository samRepository) {
        this.trainRepository = trainRepository;
        this.samRepository = samRepository;
    }

    public Iterable<Train> list() {
        return trainRepository.findAll();
    }


    public Train save(Train train) {
        return trainRepository.save(train);
    }


    public void save(List<Train> trains) {

        trainRepository.saveAll(trains);
    }







}
