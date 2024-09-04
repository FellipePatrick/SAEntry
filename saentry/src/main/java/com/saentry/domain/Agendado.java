package com.saentry.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "agendado")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Agendado {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private LocalDate data = LocalDate.now();;
    private LocalTime horaMarcada; 
    private LocalTime horaChegada = null;
    private String statusFila = "Geral"; 
}
