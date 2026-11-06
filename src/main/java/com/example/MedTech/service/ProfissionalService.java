package com.example.MedTech.service;

import com.example.MedTech.model.Profissional;
import com.example.MedTech.repository.ProfissionalRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;



@Service 
public class ProfissionalService {
    private final ProfissionalRepository profissionalRepository;

    public ProfissionalService(ProfissionalRepository profissionalRepository) {
        this.profissionalRepository = profissionalRepository;
    }

    public List<Profissional> listarTodos() {
        return profissionalRepository.findAll();
    }

    public Optional<Profissional> buscarPorId(Long id) {
        return profissionalRepository.findById(id);
    }
    
    public Profissional salvar(Profissional profissional) {
        return profissionalRepository.save(profissional);
    }
    public void excluir(Long id) {
        profissionalRepository.deleteById(id);
    }
}
