package com.saentry.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.saentry.domain.Usuario;
import com.saentry.domain.Agendado;
import com.saentry.domain.BaseDados;
import com.saentry.service.AgendadoService;
import com.saentry.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class SistemaController {

    private UsuarioService UsuarioService;
    private AgendadoService agendadoService;

    public SistemaController(UsuarioService UsuarioService, AgendadoService agendadoService) {
        this.UsuarioService = UsuarioService;
        this.agendadoService = agendadoService;

    }

    @PostMapping("/salvarBase")
    public ModelAndView doCadastroDados(@ModelAttribute BaseDados s, Errors errors,
            @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {

        if (errors.hasErrors()) {
            return new ModelAndView("home/index").addObject("base", s);
        }

        File tempFile = File.createTempFile("temp", ".pdf");
        try {
            file.transferTo(tempFile);

            PDDocument document = PDDocument.load(tempFile);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(document);
            document.close();

            this.agendadoService.processTextAfterSequence(text);

            redirectAttributes.addFlashAttribute("msg", "Dados salvos com sucesso!");
            return new ModelAndView("redirect:/");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg", "Erro ao processar o arquivo PDF");
            return new ModelAndView("redirect:/");

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Agendado> agendados = this.agendadoService.findAll();
        model.addAttribute("agendados", agendados);
        Agendado agendado = new Agendado();
        agendado.setNome("Guest");
        model.addAttribute("agendado", agendado);
        return "home/index";
    }

    @PostMapping("/search")
    public ModelAndView index(@ModelAttribute Agendado a, @RequestParam("cpf") String cpf, Errors errors,
            RedirectAttributes redirectAttributes) {
        List<Agendado> agendados = this.agendadoService.findAll();
        ModelAndView modelAndView = new ModelAndView("home/index");
        modelAndView.addObject("agendados", agendados);

        Optional<Agendado> s = this.agendadoService.findByCpf(cpf);
        if (s.isPresent()) {
            modelAndView.addObject("agendado", s.get());
        } else {
            redirectAttributes.addFlashAttribute("msgV", "Usuário não agendado!");
            return new ModelAndView("redirect:/");
        }

        return modelAndView;
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
