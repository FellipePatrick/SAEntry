package com.saentry.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.saentry.domain.BaseDados;
import com.saentry.repository.BaseDadosRepository;



@Service
public class BaseDadosService {
    BaseDadosRepository baseDadosRepository;

    BaseDadosService(BaseDadosRepository baseDadosRepository){
        this.baseDadosRepository = baseDadosRepository;
    }

    public void deleteBaseDados(Long id){
        baseDadosRepository.deleteById(id);
    }

    public List<BaseDados> findAll(){
        return baseDadosRepository.findAll();
    }

    public BaseDados findById(Long id){
        return baseDadosRepository.findById(id).get();
    }

    public BaseDados saveBaseDados(BaseDados baseDados){
        return baseDadosRepository.save(baseDados);
    }
    
}
