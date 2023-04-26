package org.example.repository;

import org.example.model.Sam;
import org.example.model.Train;
import org.example.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {




    default boolean exists(String email) {
        List<Utilisateur> utilisateurList = findAll();
        for (Utilisateur utilisateur : utilisateurList) {
            if (utilisateur.getLogin().equals(email)) {
                return true;
            }
        }
        return false;
    }

    //en excluant l'objet correspondant à id
    default boolean exists(Utilisateur utilisateur, Long id){
        List<Utilisateur> utilisateurList = findAll();
        for (Utilisateur utilisateur1 : utilisateurList){
            if (!Objects.equals(utilisateur1.getId(), id) && utilisateur1.equals(utilisateur)) return true;
        }
        return false;
    }
}
