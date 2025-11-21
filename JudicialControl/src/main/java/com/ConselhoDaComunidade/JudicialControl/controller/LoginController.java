package com.ConselhoDaComunidade.JudicialControl.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.ConselhoDaComunidade.JudicialControl.DTO.UserRegistrationDto;
import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

@Controller
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository ur;

    @Autowired
    private ReeducandoService reeducandoService;

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(required = false) String expired,
                            @RequestParam(required = false) String invalid,
                            Model model){
        if (error != null){
            model.addAttribute("error", "CPF ou senha incorretos. Após 5 tentativas, a conta será bloqueada por 15 minutos.");
        }
        if (expired != null) {
            model.addAttribute("msg", "Sua sessão foi encerrada porque sua conta foi acessada em outro dispositivo.");
        } else if (invalid != null) {
            model.addAttribute("msg", "Sua sessão expirou. Faça login novamente.");
        }
        return "login";
    }

    @GetMapping("/relatorios")
    public String relatorios(Model model) {
        model.addAttribute("resumoStatus", reeducandoService.getResumoStatus());
        model.addAttribute("porMes", reeducandoService.getCadastradosPorMes());
        model.addAttribute("tendenciaAtrasos", reeducandoService.getTendenciaAtrasosUltimosMeses());

        return "relatorios";
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String frequencia,
                            @RequestParam(required = false) String termo,
                            @RequestParam(required = false) String filtro,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User usuarioLogado = (User) auth.getPrincipal();

        int size = 10;

        Page<Reeducando> pagina = reeducandoService.buscarPorFiltros(frequencia, termo, filtro, page, size);

        model.addAttribute("pagina", pagina);
        model.addAttribute("reeducandos", pagina.getContent());
        model.addAttribute("frequenciaSelecionada", frequencia);
        model.addAttribute("termoBusca", termo);
        model.addAttribute("filtroStatus", filtro);
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalReeducandos", pagina.getTotalElements());

        return "index";
    }


    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login";
    }

    @GetMapping("/admin/users/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newUserForm(Model model) {
        model.addAttribute("userDto", new UserRegistrationDto());
        return "admin/newUser";
    }

    @PostMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String createUser(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "admin/newUser";
        }

        if (!dto.getSenha().equals(dto.getConfirmPassword())){
            result.rejectValue("confirmPassword", "error.confirmPassword", "As senhas não conferem");
            return "admin/newUser";
        }

        if (!dto.isCpfValido()){
            result.rejectValue("cpf", "error.cpf", "CPF inválido");
            return "admin/newUser";
        }

        if (ur.findByCpf(dto.getCpf()).isPresent()) {
            boolean isAdminRequest = true; // mude para false em endpoints públicos

            if (isAdminRequest) {
                result.rejectValue("cpf", "error.cpf", "CPF já cadastrado");
            } else {
                result.rejectValue("cpf", "error.cpf",
                        "Não foi possível realizar o cadastro. Verifique os dados e tente novamente.");
                log.warn("Tentativa de registro com CPF existente: {}", dto.getCpf());
            }

            return "admin/newUser";
        }

        User user = new User();

        user.setNome(dto.getNome());
        user.setCpf(dto.getCpf());
        user.setSenha(passwordEncoder.encode(dto.getSenha()));
        user.setRoles(Set.of("ROLE_USER"));

        ur.save(user);

        return "redirect:/?created";
    }

}
