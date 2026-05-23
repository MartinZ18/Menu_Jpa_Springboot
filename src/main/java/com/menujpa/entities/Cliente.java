package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Cliente extends Persona {

    // usuario y contraseña del diagrama (Persona base tiene: nombre, cedula, telefono, correo)
    @Size(max = 100, message = "El usuario no puede superar 100 caracteres")
    @Column(name = "usuario", length = 100)
    private String usuario;

    @Size(max = 255, message = "La contraseña no puede superar 255 caracteres")
    @Column(name = "contrasenia", length = 255)
    private String contrasenia;

    @Override
    public String toString() {
        return getApellido() + ", " + getNombre();
    }
}
