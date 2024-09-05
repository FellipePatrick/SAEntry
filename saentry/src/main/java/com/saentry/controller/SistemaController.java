package com.saentry.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

import java.util.Comparator;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import com.saentry.domain.Usuario;
import com.saentry.domain.Agendado;
import com.saentry.service.AgendadoService;
import com.saentry.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class SistemaController {

    private UsuarioService UsuarioService;
    private AgendadoService agendadoService;

    public SistemaController(UsuarioService UsuarioService, AgendadoService agendadoService) {
        this.UsuarioService = UsuarioService;
        this.agendadoService = agendadoService;
    }

    @GetMapping("/bases")
    public String base(Model model) {
        Agendado agendado = new Agendado();
        model.addAttribute("agendado", agendado);
        return "base/index";
    }

    @PostMapping("/bases")
    public ModelAndView cadastrarBase(@ModelAttribute Agendado agendado, RedirectAttributes redirectAttributes) {
        try {
            this.agendadoService.saveAgendado(agendado);
            redirectAttributes.addFlashAttribute("msg", "Agendado cadastrado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }
        return new ModelAndView("redirect:/");
    }

    @PostMapping("/salvarBase")
    public ModelAndView doCadastroDados(@ModelAttribute String s, Errors errors,@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) throws IOException {
        if (errors.hasErrors()) {
            return new ModelAndView("home/index");
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
            redirectAttributes.addFlashAttribute("msgV", "Erro ao processar o arquivo PDF");
            return new ModelAndView("redirect:/");

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @PostMapping("/painel/saveLocalStorageData")
    @ResponseBody
    public void saveLocalStorageData(@RequestBody Map<String, Object> data, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> ultimasPessoasChamadas = (List<String>) data.get("ultimasPessoasChamadas");
        session.setAttribute("ultimasPessoasChamadas", ultimasPessoasChamadas);
    }

    @GetMapping("/painel")
    public String painel(Model model, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<String> ultimasPessoasChamadas = (List<String>) session.getAttribute("ultimasPessoasChamadas");
        if (ultimasPessoasChamadas == null) {
            ultimasPessoasChamadas = new ArrayList<>();
        }
        List<Agendado> agendados = new ArrayList<>();
        for (String s : ultimasPessoasChamadas) {
            Long id = Long.parseLong(s);
            Agendado agendado = this.agendadoService.findById(id);
            if (agendado != null) {
                agendados.add(agendado);
            }
        }
        model.addAttribute("agendados", agendados);
     
        return "painel/index";
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Agendado> agendados = this.agendadoService.findAll();
        List<Agendado> agendadosHoje = new ArrayList<>();
        LocalDate dataAtual = LocalDate.now();
        for (Agendado agendado : agendados) {
            if (agendado.getData().equals(dataAtual) && agendado.getStatusFila().equals("Geral")) {
                agendadosHoje.add(agendado);
            }
        }
        agendadosHoje.sort(Comparator.comparing(Agendado::getHoraMarcada));

        model.addAttribute("agendados", agendadosHoje);
        Agendado agendado = new Agendado();
        agendado.setNome("Guest");
        model.addAttribute("status", "geral");

        model.addAttribute("agendado", agendado);
        return "home/index";
    }

    @GetMapping("/fila")
    public ModelAndView fila(@ModelAttribute String s, RedirectAttributes redirectAttributes) {
        List<Agendado> agendados = this.agendadoService.findByStatusFila("Fila");
        agendados.sort(Comparator.comparing(Agendado::getHoraChegada));

        Agendado agendado = new Agendado();
        agendado.setNome("Guest");
        ModelAndView modelAndView = new ModelAndView("home/index");
        modelAndView.addObject("status", "fila");
        modelAndView.addObject("agendados", agendados);
        modelAndView.addObject("agendado", agendado);
        return modelAndView;
    }

    @GetMapping("/atendimento")
    public ModelAndView atendimento(@ModelAttribute String s, RedirectAttributes redirectAttributes) {
        List<Agendado> agendados = this.agendadoService.findByStatusFila("Atendimento");
        List<Agendado> agendadosHoje = new ArrayList<>();
        LocalDate dataAtual = LocalDate.now();
        for (Agendado agendado : agendados) {
            if (agendado.getData().equals(dataAtual) && agendado.getStatusFila().equals("Atendimento")) {
                agendadosHoje.add(agendado);
            }
        }
        Agendado agendado = new Agendado();
        agendado.setNome("Guest");
        ModelAndView modelAndView = new ModelAndView("home/index");
        modelAndView.addObject("status", "atendimento");
        modelAndView.addObject("agendados", agendadosHoje);
        modelAndView.addObject("agendado", agendado);

        return modelAndView;
    }

    @GetMapping("/adicionarAtendimento/{id}")
    public ModelAndView doAdicionarAtendimento(@ModelAttribute String s, RedirectAttributes redirectAttributes,
            @PathVariable Long id) {
        Agendado agendado = this.agendadoService.findById(id);

        if (agendado == null) {
            redirectAttributes.addFlashAttribute("msgV", "Agendado não encontrado");
            return new ModelAndView("redirect:/");
        }
        try {
            this.agendadoService.update(agendado.getId(), "Atendimento");
            redirectAttributes.addFlashAttribute("msg", "Agendado notificado para atendimento");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }

        return new ModelAndView("redirect:/atendimento");
    }

    @GetMapping("/deletarAgendado/{id}")
    public ModelAndView doDeletarAgendado(@ModelAttribute String s, RedirectAttributes redirectAttributes,
            @PathVariable Long id) {
        Agendado agendado = this.agendadoService.findById(id);
        if (agendado == null) {
            redirectAttributes.addFlashAttribute("msgV", "Agendado não encontrado");
            return new ModelAndView("redirect:/");
        }
        try {
            this.agendadoService.deleteAgendado(agendado.getId());
            redirectAttributes.addFlashAttribute("msg", "Agendado deletado com sucesso");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/atendida")
    public ModelAndView atendida(@ModelAttribute String s, RedirectAttributes redirectAttributes) {
        List<Agendado> agendados = this.agendadoService.findByStatusFila("Atendida");
        Agendado agendado = new Agendado();
        agendado.setNome("Guest");
        ModelAndView modelAndView = new ModelAndView("home/index");
        modelAndView.addObject("agendados", agendados);
        modelAndView.addObject("status", "atendidas");
        modelAndView.addObject("agendado", agendado);
        return modelAndView;
    }

    @PostMapping("/search")
    public ModelAndView index(@ModelAttribute String c, @RequestParam("cpf") String cpf, Errors errors, RedirectAttributes redirectAttributes) {
        cpf = cpf.replace(".", "").replace("-", "");
        List<Agendado> agendados = this.agendadoService.findAll();
        List<Agendado> agendadosHoje = new ArrayList<>();
        LocalDate dataAtual = LocalDate.now();
        for (Agendado agendado : agendados) {
            if (agendado.getData().equals(dataAtual) && agendado.getStatusFila().equals("Geral")) {
                agendadosHoje.add(agendado);
            }
        }
        
        ModelAndView modelAndView = new ModelAndView("home/index");
        modelAndView.addObject("status", "geral");
        modelAndView.addObject("agendados", agendadosHoje);
        if (cpf.isEmpty()) {
            redirectAttributes.addFlashAttribute("msgV", "CPF não pode ser vazio");
            return new ModelAndView("redirect:/");
        }

        List<Optional<Agendado>> lista = this.agendadoService.findByCpf(cpf);
        if (lista.size() > 0) {
            boolean flag = false;
            for (Optional<Agendado> s : lista) {
                if (s.get().getData().equals(dataAtual) && s.get().getStatusFila().equals("Geral")) {
                    modelAndView.addObject("agendado", s.get());
                    return modelAndView;
                }
            }
            if (!flag) {
                redirectAttributes.addFlashAttribute("msgV", "Usuário já foi adicionado a fila!");
                return new ModelAndView("redirect:/");
            }
        } else {
            redirectAttributes.addFlashAttribute("msgV", "Usuário não agendado!");
            return new ModelAndView("redirect:/");
        }

        return modelAndView;
    }

    @PostMapping("/adicionarFila")
    public ModelAndView doAdicionarFila(@ModelAttribute Agendado agendado, Errors errors,
            RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            return new ModelAndView("home/index");
        }
        try {
            this.agendadoService.update(agendado.getId(), "Fila");
            redirectAttributes.addFlashAttribute("msg", "Agendado adicionado a fila");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }

        return new ModelAndView("redirect:/");
    }

    @GetMapping("/adicionarAtendida/{id}")
    public ModelAndView doAdicionarAtendida(@ModelAttribute String s, @PathVariable Long id, Errors errors,
            RedirectAttributes redirectAttributes) {
        Agendado agendado = this.agendadoService.findById(id);
        if (agendado == null) {
            redirectAttributes.addFlashAttribute("msgV", "Agendado não encontrado");
            return new ModelAndView("redirect:/");
        }
        try {
            this.agendadoService.update(agendado.getId(), "Atendida");
            redirectAttributes.addFlashAttribute("msg", "Atendimento finalizado");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("msgV", e.getMessage());
        }
        return new ModelAndView("redirect:/atendida");
    }

    @GetMapping("/login")
    public String login(Model model) {
        return "login/index";
    }

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
