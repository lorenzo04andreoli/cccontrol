package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.DTO.UserListItem;
import com.ConselhoDaComunidade.JudicialControl.DTO.UserRegistrationDto;
import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsersController {

    private static final Logger log = LoggerFactory.getLogger(AdminUsersController.class);

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository ur;

    @Autowired
    private SessionRegistry sessionRegistry;

    @GetMapping("/new")
    public String newUserForm(Model model) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setRole("USER");
        model.addAttribute("userDto", dto);
        return "admin/newUser";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute("userDto") UserRegistrationDto dto,
                             BindingResult result,
                             Model model) {

        if (result.hasErrors()) return "admin/newUser";

        if (!dto.getSenha().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "As senhas não conferem");
            return "admin/newUser";
        }

        if (!dto.isCpfValido()) {
            result.rejectValue("cpf", "error.cpf", "CPF inválido");
            return "admin/newUser";
        }

        if (ur.findByCpf(dto.getCpf()).isPresent()) {
            result.rejectValue("cpf", "error.cpf", "CPF já cadastrado");
            return "admin/newUser";
        }

        User user = new User();
        user.setNome(dto.getNome());
        user.setCpf(dto.getCpf());
        user.setSenha(passwordEncoder.encode(dto.getSenha()));

        String role = (dto.getRole() == null) ? "USER" : dto.getRole().trim().toUpperCase();
        Set<String> roles = switch (role) {
            case "ADMIN" -> Set.of("ROLE_ADMIN");
            case "VIEWER" -> Set.of("ROLE_VIEWER");
            case "AUDIENCE" -> Set.of("ROLE_AUDIENCE");
            default -> Set.of("ROLE_USER");
        };
        user.setRoles(roles);

        ur.save(user);
        return "redirect:/?created";
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = new ArrayList<>();
        ur.findAll().forEach(users::add);

        users.sort(Comparator.comparing(User::getNome, String.CASE_INSENSITIVE_ORDER));

        List<UserListItem> itens = users.stream()
                .map(u -> new UserListItem(
                        u.getNome(),
                        u.getCpf(),
                        isUserOnline(u.getCpf())
                ))
                .toList();

        model.addAttribute("users", itens);
        model.addAttribute("totalUsers", itens.size());
        return "admin/users";
    }

    private boolean isUserOnline(String cpf) {
        // SessionRegistry guarda "principals" autenticados
        for (Object principal : sessionRegistry.getAllPrincipals()) {

            String username = null;

            if (principal instanceof UserDetails ud) {
                username = ud.getUsername();
            } else if (principal instanceof String s) {
                username = s;
            }

            if (username != null && username.replaceAll("\\D", "").equals(cpf.replaceAll("\\D", ""))) {
                // false => não incluir sessões expiradas
                return !sessionRegistry.getAllSessions(principal, false).isEmpty();
            }
        }
        return false;
    }
}

