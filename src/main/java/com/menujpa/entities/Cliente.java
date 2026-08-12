package com.menujpa.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter @Setter
@NoArgsConstructor
public class Cliente extends Persona {

    @Override
    public String toString() {
        return getApellido() + ", " + getNombre();
    }
}
