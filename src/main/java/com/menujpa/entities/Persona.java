package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@MappedSuperclass
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Persona extends Base {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Size(max = 20, message = "La cédula no puede superar 20 caracteres")
    @Column(name = "cedula", length = 20)
    private String cedula;

    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres")
    @Column(name = "telefono", length = 20)
    private String telefono;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 100, message = "El correo no puede superar 100 caracteres")
    @Column(name = "correo", length = 100)
    private String correo;

    @Override
    public String toString() {
        return nombre + " " + apellido;
    }
}
