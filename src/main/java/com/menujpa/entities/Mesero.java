package com.menujpa.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mesero")
@Getter @Setter
public class Mesero extends Empleado {

    @Override
    public String toString() {
        return getApellido() + ", " + getNombre();
    }
}
