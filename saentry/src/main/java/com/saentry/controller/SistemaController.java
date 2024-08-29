package com.saentry.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saentry.domain.Usuario;
import com.saentry.domain.BaseDados;
import com.saentry.service.BaseDadosService;
import com.saentry.service.FileStorageService;
import com.saentry.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
public class SistemaController {

    private UsuarioService UsuarioService;
    private FileStorageService fileStorageService;
    private BaseDadosService baseDadosService;
    
    public SistemaController(UsuarioService UsuarioService) {
        this.UsuarioService = UsuarioService;
        this.fileStorageService = fileStorageService;
        this.baseDadosService = baseDadosService;

    }

    // Gerencia das bases de dados
    @PostMapping("/baseDados")
    public String doCadastroDados(@ModelAttribute BaseDados BaseDados, RedirectAttributes redirectAttributes) {
        BaseDados baseDados = this.baseDadosService.saveBaseDados(BaseDados);
        
        return "entity";
    }
    


    @GetMapping("/")
    public String teste(Model model) {
        return "home/index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login/index";
    }

    




    // Gerencia de Usuario

    @GetMapping("/user")
    public String user(Model model) {
        Usuario usuario = new Usuario();
        model.addAttribute("usuario", usuario);
        return "user/index";
    }

    @PostMapping("/user")
    public ModelAndView cadastrarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioExistente = UsuarioService.findByCredenciais(usuario.getCredenciais());

        if (usuarioExistente.isPresent()) {
            redirectAttributes.addFlashAttribute("msgV", "Usuário já cadastrado com essas credenciais.");
        } else {
            UsuarioService.cadastreUser(usuario);
            redirectAttributes.addFlashAttribute("msg", "Cadastro realizado com sucesso");
        }

        return new ModelAndView("redirect:/users");
    }

    @PostMapping("/user/atualizar")
    public ModelAndView atualizarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            UsuarioService.updateUser(usuario);
            redirectAttributes.addFlashAttribute("msg", "Usuário atualizado com sucesso");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }
        return new ModelAndView("redirect:/users");
    }

    @GetMapping("/users")
    public String users(Model model) {
        List<Usuario> usuarios = UsuarioService.findAll();
        Usuario usuario = new Usuario();
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarios", usuarios);
        return "users/index";
    }

    @GetMapping("/users/deletar/{id}")
    public String doDeletar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuarioExistente = UsuarioService.findById(id);

        if (usuarioExistente.isPresent()) {
            UsuarioService.deleteUser(id);
            redirectAttributes.addFlashAttribute("msg", "Usuário deletado com sucesso");
        } else {
            redirectAttributes.addFlashAttribute("msgV", "Usuário não encontrado");
        }

        return "redirect:/users";
    }

    @GetMapping("/users/{id}")
    public ModelAndView editUser(@PathVariable Long id, HttpSession session) {
        Optional<Usuario> usuarioOptional = UsuarioService.findById(id);

        if (usuarioOptional.isPresent()) {
            ModelAndView mv = new ModelAndView("users/edit");
            mv.addObject("usuario", usuarioOptional.get());
            return mv;
        }
        return new ModelAndView("redirect:/");
    }

}
