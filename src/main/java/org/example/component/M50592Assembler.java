package org.example.component;

import org.example.controller.M_50592Controller;
import org.example.controller.TypeMrController;
import org.example.model.M_50592;
import org.example.model.Mr;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class M50592Assembler implements RepresentationModelAssembler<M_50592, EntityModel<M_50592>> {


    public  M50592Assembler(){

    }
    public EntityModel<M_50592> toModel(M_50592 entity) {
        return EntityModel.of(entity, new Link[]{WebMvcLinkBuilder.linkTo(((M_50592Controller)WebMvcLinkBuilder.methodOn(M_50592Controller.class, new Object[0])).get50592ById(entity.getId())).withSelfRel(), WebMvcLinkBuilder.linkTo(((M_50592Controller)WebMvcLinkBuilder.methodOn(M_50592Controller.class, new Object[0])).getAll50592()).withRel("50592")});
    }
}
