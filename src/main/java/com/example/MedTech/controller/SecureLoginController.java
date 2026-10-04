package com.example.MedTech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SecureLoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("cpf") String cpf,
            @RequestParam("rg") String rg,
            @RequestParam("endereco") String endereco,
            @RequestParam("instituicao") String instituicao,
            @RequestParam("senha") String senha) {

        System.out.println("Registro: Redirecionado para a página de login.");
        return "redirect:/login";
    }

    @GetMapping("/recoverpassword")
    public String recoverpassword() {
        return "recoverpassword";
    }

    @PostMapping("/recoverpassword")
    public String handleRecoverPassword(
            @RequestParam("email") String email) {

        System.out.println("Recuperação de E-mail: Redirecionado para a página de login.");
        return "redirect:/login";
    }

    @GetMapping("/perfil/paciente")
    public String perfilPaciente() {
        return "perfil-paciente";
    }
    @GetMapping("/perfil/doutor")
    public String perfilDoutor() {
        return "perfil-doutor";
    }

}
