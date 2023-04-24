package org.example.component;

import org.example.controller.TypeMrController;
import org.example.controller.UtilisateurController;
import org.example.model.Mr;
import org.example.model.Utilisateur;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurAssembler implements RepresentationModelAssembler<Utilisateur , EntityModel<Utilisateur>> {


public  UtilisateurAssembler(){}

    public EntityModel<Utilisateur> toModel(Utilisateur entity) {
        return EntityModel.of(entity, new Link[]{WebMvcLinkBuilder.linkTo(((UtilisateurController)WebMvcLinkBuilder.methodOn(UtilisateurController.class, new Object[0])).getAllUser()).withRel("USER")});
    }
}
