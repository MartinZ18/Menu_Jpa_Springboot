package com.menujpa.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "despensa")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Despensa extends Base {

    // Composición con Ingrediente: diamante lleno en el diagrama
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinTable(
        name = "despensa_ingrediente",
        joinColumns = @JoinColumn(name = "idDespensa"),
        inverseJoinColumns = @JoinColumn(name = "idIngrediente")
    )
    private List<Ingrediente> ingredientes = new ArrayList<>();

    // El Gerente administra la Despensa (1..* en diagrama: "Administra")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idGerente")
    private Gerente gerente;

    @Override
    public String toString() {
        return "Despensa #" + getId();
    }
}
