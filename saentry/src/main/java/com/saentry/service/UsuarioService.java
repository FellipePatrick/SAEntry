package com.saentry.service;

import org.springframework.stereotype.Service;

import com.saentry.domain.Usuario;
import com.saentry.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;
@Service
public class UsuarioService{ 
    UsuarioRepository usuarioRepository;
    
    UsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }
    
    public Usuario cadastreUser(Usuario usuario){
        return usuarioRepository.save(usuario);
    }

    public void deleteUser(Long id){
        usuarioRepository.deleteById(id);
    }

    public Optional<Usuario> findById(Long id){
        return usuarioRepository.findById(id);
    }

    public List<Usuario> findAll(){
        return usuarioRepository.findAll();
    }

    public void updateUser(Usuario usuario) {
        Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findById(usuario.getId());
    
        if (usuarioExistenteOpt.isPresent()) {
            Usuario usuarioExistente = usuarioExistenteOpt.get();
            usuarioExistente.setNome(usuario.getNome());
    
            if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
                usuarioExistente.setSenha(usuario.getSenha()); 
            }
    
            // Atualiza as credenciais se forem fornecidas e diferentes das atuais
            if (usuario.getCredenciais() != null && !usuario.getCredenciais().isEmpty() &&
                !usuario.getCredenciais().equals(usuarioExistente.getCredenciais())) {
    
                Optional<Usuario> usuarioExistenteCredenciais = usuarioRepository.findByCredenciais(usuario.getCredenciais());
    
                // Se as novas credenciais já estiverem cadastradas, lança uma exceção
                if (usuarioExistenteCredenciais.isPresent()) {
                    throw new RuntimeException("Credenciais já cadastradas.");
                } else {
                    usuarioExistente.setCredenciais(usuario.getCredenciais());
                }
            }
    
            // Atualiza o papel (role)
            usuarioExistente.setRole(usuario.getRole());
    
            // Salva as alterações no repositório
            usuarioRepository.save(usuarioExistente);
        } else {
            throw new RuntimeException("Usuário não encontrado para atualização.");
        }
    }
    

    public Optional<Usuario> findByCredenciais(String credenciais){
        return usuarioRepository.findByCredenciais(credenciais);
    }
}