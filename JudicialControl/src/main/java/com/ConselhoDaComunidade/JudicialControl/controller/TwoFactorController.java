package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import com.ConselhoDaComunidade.JudicialControl.util.TOTPUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class TwoFactorController {

    @Autowired
    private UserRepository userRepository;


    @GetMapping("/profile/2fa")
    public String profile2fa(Model model, @RequestParam(required = false) String msg, Authentication auth){
        String cpf = auth.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return "redirect:/login";

        if (user.getTotpSecret() == null){
            String secret = TOTPUtil.generateSecret();
            user.setTotpSecret(secret);
            userRepository.save(user);
        }

        String issuer = "JudicialControl";
        String account = user.getCpf();
        String otpAuthUrl = TOTPUtil.getOtpAuthURL(issuer, account, user.getTotpSecret());

        model.addAttribute("secret", user.getTotpSecret());
        model.addAttribute("otpAuthUrl", otpAuthUrl);
        model.addAttribute("twoFactorEnabled", user.isTwoFactorEnabled());
        model.addAttribute("msg", msg);
        return "profile/2fa";
    }



    @PostMapping("/profile/2fa/enable")
    public String enable2fa(@RequestParam("code") int code, Authentication auth, Model model){
        String cpf = auth.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return "redirect:/login";

        boolean ok = TOTPUtil.verifyCode(user.getTotpSecret(), code, 1);
        if (ok) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            return "redirect:/profile/2fa?msg=enabled";
        } else {
            return "redirect:/profile/2fa?msg=invalid";
        }
    }

    @PostMapping("profile/2fa/disable")
    public String disable2fa(Authentication auth, @RequestParam("confirm") boolean confirm){
        if (!confirm) return "redirect:/profile/2fa";

        String cpf = auth.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return "redirect:/login";

        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null);
        userRepository.save(user);
        return "redirect:/profile/2fa?msg=disabled";
    }

    @GetMapping("/two-factor")
    public String showTwoFactorPage(HttpSession session, Model model) {
        Boolean required = (Boolean) session.getAttribute("2fa_required");
        String cpf = (String) session.getAttribute("2fa_user");
        if (required == null || !required || cpf == null) {
            return "redirect:/login";
        }
        model.addAttribute("cpfMasked", maskCpf(cpf));
        return "twofactor/verify"; // template para digitar código
    }

    @PostMapping("/two-factor/verify")
    public String verifyTwoFactor(@RequestParam("code") int code,
                                  HttpSession session) {

        String cpf = (String) session.getAttribute("2fa_user");
        if (cpf == null) return "redirect:/login";

        Optional<User> optionalUser = userRepository.findByCpf(cpf);
        if (optionalUser.isEmpty()) {
            session.removeAttribute("2fa_required");
            session.removeAttribute("2fa_user");
            return "redirect:/login?error";
        }
        User user = optionalUser.get();
        boolean ok = TOTPUtil.verifyCode(user.getTotpSecret(), code, 3);

        if (!ok) {
            return "redirect:/two-factor?error=true";
        }

        // autenticar o usuário programaticamente
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // limpa sessão temporária
        session.removeAttribute("2fa_required");
        session.removeAttribute("2fa_user");

        return "redirect:/";
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "usuário";
        return "***" + cpf.substring(3, 9) + "**";
    }
}
