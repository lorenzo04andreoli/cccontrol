package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.User;
import com.ConselhoDaComunidade.JudicialControl.repository.UserRepository;
import com.ConselhoDaComunidade.JudicialControl.service.storage.ProfilePhotosStorage;
import com.ConselhoDaComunidade.JudicialControl.service.storage.LocalProfilePhotoStorage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    private final UserRepository userRepository;
    private final ProfilePhotosStorage storage;

    private final LocalProfilePhotoStorage localStorageOrNull;

    public UsuarioController(UserRepository userRepository,
                             ProfilePhotosStorage storage,
                             org.springframework.beans.factory.ObjectProvider<LocalProfilePhotoStorage> localProvider) {
        this.userRepository = userRepository;
        this.storage = storage;
        this.localStorageOrNull = localProvider.getIfAvailable();
    }

    @GetMapping
    public String perfil(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        User dbUser = userRepository.findByCpf(user.getCpf()).orElse(user);

        String fotoUrl = storage.resolvePhotoUrl(dbUser.getFotoUrl());

        model.addAttribute("user", dbUser);
        model.addAttribute("fotoUrl", fotoUrl);

        String role = dbUser.getRoles().stream().findFirst().orElse("ROLE_USER");
        model.addAttribute("roleLabel", role.replace("ROLE_", ""));

        return "usuario/perfil";
    }

    @PostMapping("/foto")
    @ResponseBody
    public ResponseEntity<?> uploadFoto(@RequestParam("foto") MultipartFile foto) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();
        User dbUser = userRepository.findByCpf(user.getCpf()).orElseThrow();

        String keyOrPath = storage.uploadUserProfilePhoto(dbUser.getId(), foto);
        dbUser.setFotoUrl(keyOrPath);
        userRepository.save(dbUser);

        String fotoUrl = storage.resolvePhotoUrl(keyOrPath);
        return ResponseEntity.ok(Map.of("fotoUrl", fotoUrl));
    }

    @GetMapping("/foto/view")
    public ResponseEntity<Resource> viewLocal(@RequestParam("path") String path) throws Exception {
        if (localStorageOrNull == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // segurança básica: evita traversal e garante que está dentro do root
        Path filePath = localStorageOrNull.resolveOnDisk(path);
        if (!filePath.startsWith(localStorageOrNull.getRootDir())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // content type simples por extensão
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String p = path.toLowerCase();
        if (p.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (p.endsWith(".webp")) mediaType = MediaType.parseMediaType("image/webp");
        else if (p.endsWith(".jpg") || p.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .body(resource);
    }
}
