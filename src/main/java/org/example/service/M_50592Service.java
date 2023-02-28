package org.example.service;

import org.example.model.Environnement;
import org.example.model.M_50592;
import org.example.model.Sam;
import org.example.repository.M_50592Repository;
import org.example.repository.SamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class M_50592Service {

    private final M_50592Repository m50592Repository;

    public M_50592Service(M_50592Repository m50592Repository) {
        this.m50592Repository = m50592Repository;
    }

    public Iterable<M_50592> list() {
        return m50592Repository.findAll();
    }

    public M_50592 save(M_50592 m50592) {
        return m50592Repository.save(m50592);
    }


    public void save(List<M_50592> m_50592s) {
        for (M_50592 m50592 : m_50592s) {
            Environnement env = m50592.getEnvironnement();
            String[] villes = env.extraireVilles();
            if (villes != null) {
                env.setVilleDepart(villes[0]);
                env.setVilleArrivee(villes[1]);
            }
        }
        m50592Repository.saveAll(m_50592s);
    }
}
