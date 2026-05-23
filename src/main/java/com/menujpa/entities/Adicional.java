package com.menujpa.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity @DiscriminatorValue("Adicional") @Getter @Setter @NoArgsConstructor
public class Adicional extends Alimento {}
