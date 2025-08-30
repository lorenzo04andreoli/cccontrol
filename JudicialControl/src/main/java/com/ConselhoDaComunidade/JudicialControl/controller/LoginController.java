package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import com.ConselhoDaComunidade.JudicialControl.service.CookieService;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserRepository ur;

    @Autowired
    private ReeducandoService reeducandoService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/logar")
    public String loginUser(User user, Model model, HttpServletResponse response) throws UnsupportedEncodingException {
        String cpf = user.getCpf().trim();
        String senha = user.getSenha().trim();

        System.out.println("Tentando login com: " + cpf + " | " + senha);

        Optional<User> userLogged = ur.login(cpf, senha);
        if (userLogged.isPresent()) {
            User u = userLogged.get();
            CookieService.setCookie(response, "userId", String.valueOf(u.getId()), 20000);
            CookieService.setCookie(response, "username", u.getNome(), 20000);
            return "redirect:/";
        }

        model.addAttribute("erro", "Usuário inválido");
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
                            Model model,
                            HttpServletRequest request) throws UnsupportedEncodingException {
        String nomeUsuario = CookieService.getCookie(request, "username");
        model.addAttribute("nome", nomeUsuario);

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
    public String logout(HttpServletResponse response) throws UnsupportedEncodingException {
        CookieService.setCookie(response, "userId", "", 0);
        CookieService.setCookie(response, "username", "", 0);
        return "redirect:/login";
    }

    @GetMapping("/userRegister")
    public String register() {
        return "register";
    }

    @PostMapping("/userRegister")
    public String userRegister(@Valid User user, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/userRegister";
        }

        ur.save(user);
        return "redirect:/login";
    }
}
