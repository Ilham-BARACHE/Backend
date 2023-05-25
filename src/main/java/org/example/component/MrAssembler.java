//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.example.component;

import org.example.controller.TypeMrController;
import org.example.model.Mr;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class MrAssembler implements RepresentationModelAssembler<Mr, EntityModel<Mr>> {
    public MrAssembler() {
    }

    public EntityModel<Mr> toModel(Mr entity) {
        return EntityModel.of(entity, new Link[]{WebMvcLinkBuilder.linkTo(((TypeMrController)WebMvcLinkBuilder.methodOn(TypeMrController.class, new Object[0])).getMrById(entity.getId())).withSelfRel(), WebMvcLinkBuilder.linkTo(((TypeMrController)WebMvcLinkBuilder.methodOn(TypeMrController.class, new Object[0])).getAllMr()).withRel("MR")});
    }
}
