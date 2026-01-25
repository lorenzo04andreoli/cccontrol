package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import com.ConselhoDaComunidade.JudicialControl.security.TwoFactorAuthenticationToken;
import com.ConselhoDaComunidade.JudicialControl.security.TwoFactorRateLimitService;
import com.ConselhoDaComunidade.JudicialControl.util.QRCodeUtil;
import com.ConselhoDaComunidade.JudicialControl.util.TOTPUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;


import java.util.Optional;

@Controller
public class TwoFactorController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TwoFactorRateLimitService rateLimitService;

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

        // QRCode em Base64 (PNG)
        String qrCodeBase64 = QRCodeUtil.toBase64Png(otpAuthUrl, 220, 220);

        model.addAttribute("secret", user.getTotpSecret());
        model.addAttribute("otpAuthUrl", otpAuthUrl);
        model.addAttribute("qrCodeBase64", qrCodeBase64);
        model.addAttribute("twoFactorEnabled", user.isTwoFactorEnabled());
        model.addAttribute("msg", msg);

        return "profile/2fa";
    }



    @PostMapping("/profile/2fa/enable")
    public String enable2fa(@RequestParam("code") String code, Authentication auth){
        String cpf = auth.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return "redirect:/login";

        code = (code == null) ? "" : code.replaceAll("\\D", "");
        if (code.length() != 6) return "redirect:/profile/2fa?msg=invalid";

        boolean ok = TOTPUtil.verifyCode(user.getTotpSecret(), Integer.parseInt(code), 1);

        if (ok) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            return "redirect:/profile/2fa?msg=enabled";
        }
        return "redirect:/profile/2fa?msg=invalid";
    }


    @PostMapping("/profile/2fa/disable")
    public String disable2fa(Authentication auth,
                             @RequestParam("confirm") boolean confirm,
                             @RequestParam("code") String code) {

        if (!confirm) return "redirect:/profile/2fa";

        String cpf = auth.getName();
        User user = userRepository.findByCpf(cpf).orElse(null);
        if (user == null) return "redirect:/login";

        String clean = (code == null) ? "" : code.replaceAll("\\D", "");
        if (clean.length() != 6) return "redirect:/profile/2fa?msg=invalid";

        boolean ok = TOTPUtil.verifyCode(user.getTotpSecret(), Integer.parseInt(clean), 1);
        if (!ok) return "redirect:/profile/2fa?msg=invalid";

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
    public String verifyTwoFactor(@RequestParam("code") String code,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  HttpSession session) {

        String cpf = (String) session.getAttribute("2fa_user");
        Boolean required = (Boolean) session.getAttribute("2fa_required");
        Long startedAt = (Long) session.getAttribute("2fa_started_at");

        if (cpf == null || required == null || !required) return "redirect:/login";

        // TTL do pré-2FA
        long now = System.currentTimeMillis();
        if (startedAt == null || (now - startedAt) > 5 * 60_000L) {
            session.invalidate();
            return "redirect:/login?invalid";
        }

        String ip = getClientIp(request);
        String key = cpf + "|" + ip;

        if (rateLimitService.isBlocked(key)) {
            return "redirect:/two-factor?error=true";
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new TwoFactorAuthenticationToken(cpf, code)
            );

            // migra id da sessão
            request.changeSessionId();

            // cria um contexto limpo e seta a auth
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // salva na sessão
            SecurityContextRepository repo = new HttpSessionSecurityContextRepository();
            repo.saveContext(context, request, response);

            // limpa flags do pré-2FA
            session.removeAttribute("2fa_required");
            session.removeAttribute("2fa_user");
            session.removeAttribute("2fa_started_at");

            rateLimitService.onSuccess(key);

            return "redirect:/";
        } catch (Exception ex) {
            ex.printStackTrace();
            rateLimitService.onFailure(key);
            return "redirect:/two-factor?error=true";
        }

    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return request.getRemoteAddr();
    }


    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return "usuário";
        return "***" + cpf.substring(3, 9) + "**";
    }
}
