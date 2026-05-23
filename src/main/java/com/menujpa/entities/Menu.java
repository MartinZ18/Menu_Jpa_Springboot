package com.menujpa.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Menu extends Base {

    @NotBlank(message = "El nombre del menú es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "idGerente")
    private Gerente gerente;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "menu_receta",
        joinColumns = @JoinColumn(name = "idMenu"),
        inverseJoinColumns = @JoinColumn(name = "idReceta")
    )
    private List<Receta> recetas = new ArrayList<>();

    @Override
    public String toString() {
        return nombre;
    }
}
