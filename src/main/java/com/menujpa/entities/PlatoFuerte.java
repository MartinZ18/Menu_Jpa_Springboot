package com.menujpa.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity @DiscriminatorValue("PlatoFuerte") @Getter @Setter @NoArgsConstructor
public class PlatoFuerte extends Alimento {}
