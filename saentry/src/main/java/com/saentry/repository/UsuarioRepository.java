package com.saentry.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.saentry.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByCredenciais(String credenciais);

}