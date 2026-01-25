package com.ConselhoDaComunidade.JudicialControl.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMsg",
                    "CPF ou senha incorretos. Após 5 tentativas, a conta será bloqueada por 15 minutos.");
        }
        return "login";
    }
}
