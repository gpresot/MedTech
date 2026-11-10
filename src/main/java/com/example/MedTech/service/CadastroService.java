package com.example.MedTech.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Usuario;
import com.example.MedTech.repository.ProfissionalRepository;
import com.example.MedTech.repository.UsuarioRepository;

@Service
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PasswordEncoder passwordEncoder;

    public CadastroService(
            UsuarioRepository usuarioRepository,
            ProfissionalRepository profissionalRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.profissionalRepository = profissionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void cadastrarDoutor(
            String nome,
            String cpf,
            String especialidade,
            String crm,
            String telefone,
            String email,
            String senha) {

        String emailNormalizado = email.trim().toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Já existe uma conta cadastrada com este e-mail.");
        }

        Especialidade especialidadeEnum;

        try {
            especialidadeEnum = Especialidade.valueOf(
                    especialidade.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Especialidade inválida.");
        }

        // Cria o profissional
        MedicoEspecialista medico = new MedicoEspecialista();

        medico.setNome(nome);
        medico.setCpf(cpf);
        medico.setEmail(emailNormalizado);
        medico.setTelefone(telefone);
        medico.setCrm(crm);
        medico.setEspecialidade(especialidadeEnum);

        // Cria a conta de acesso
        Usuario usuario = new Usuario(
                emailNormalizado,
                passwordEncoder.encode(senha));

        // Salva os dois registros no banco
        profissionalRepository.save(medico);
        usuarioRepository.save(usuario);
    }
}