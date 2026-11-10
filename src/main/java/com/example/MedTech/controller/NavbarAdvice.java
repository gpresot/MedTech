package com.example.MedTech.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Informa à navbar quem está vendo a página:
 * - logado: visitantes só veem "Início" e "Entrar";
 * - ehPaciente: paciente só vê "Início" e "Meu perfil";
 * - perfilUrl: cada tipo de usuário vai para o seu perfil.
 */
@ControllerAdvice
public class NavbarAdvice {

    @ModelAttribute("logado")
    public boolean logado(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @ModelAttribute("ehPaciente")
    public boolean ehPaciente(Authentication authentication) {
        return logado(authentication) && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_PACIENTE"));
    }

    @ModelAttribute("perfilUrl")
    public String perfilUrl(Authentication authentication) {
        if (!logado(authentication)) {
            return "/login";
        }
        return ehPaciente(authentication) ? "/perfil/paciente" : "/perfil/doutor";
    }
}
