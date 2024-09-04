package com.saentry.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.saentry.domain.Agendado;


public interface AgendadoRepository extends JpaRepository<Agendado, Long> {

    List<Agendado> findByStatusFila(String statusFila);

    List<Optional<Agendado>> findByCpf(String cpf);

}
