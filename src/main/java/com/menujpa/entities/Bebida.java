package com.menujpa.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity @DiscriminatorValue("Bebida") @Getter @Setter @NoArgsConstructor
public class Bebida extends Alimento {}
