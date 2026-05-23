package com.menujpa.entities;
import jakarta.persistence.*;
import lombok.*;

@Entity @DiscriminatorValue("Postre") @Getter @Setter @NoArgsConstructor
public class Postre extends Alimento {}
