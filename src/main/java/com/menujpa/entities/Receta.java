package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "receta")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Receta extends Base {

    @NotBlank(message = "El nombre de la receta es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombreReceta;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Size(max = 50, message = "El tiempo no puede superar 50 caracteres")
    @Column(name = "tiempo", length = 50)
    private String tiempo;

    @Size(max = 50, message = "La dificultad no puede superar 50 caracteres")
    @Column(name = "dificultad", length = 50)
    private String dificultad;

    @Size(max = 100, message = "El nombre del chef no puede superar 100 caracteres")
    @Column(name = "nombre_chef", length = 100)
    private String nombreChefTexto;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "receta_alimento",
        joinColumns = @JoinColumn(name = "idReceta"),
        inverseJoinColumns = @JoinColumn(name = "idAlimento")
    )
    private List<Alimento> alimentosSeleccionados = new ArrayList<>();

    @Override
    public String toString() {
        return nombreReceta;
    }
}
