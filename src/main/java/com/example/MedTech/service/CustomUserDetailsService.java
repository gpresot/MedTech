package com.example.MedTech.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.MedTech.model.Usuario;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ProfissionalRepository profissionalRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository,
            ProfissionalRepository profissionalRepository) {
        this.usuarioRepository = usuarioRepository;
        this.profissionalRepository = profissionalRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado: " + email));

        // Sem coluna de tipo: quem tem cadastro de profissional é DOUTOR,
        // todos os demais usuários são PACIENTE.
        String papel = profissionalRepository.findByEmail(email).isPresent()
                ? "DOUTOR"
                : "PACIENTE";

        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenha())
                .roles(papel)
                .build();
    }
}
