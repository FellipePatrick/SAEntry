package com.saentry.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

import java.util.Optional;
import org.springframework.stereotype.Service;

import com.saentry.domain.Agendado;
import com.saentry.repository.AgendadoRepository;

@Service
public class AgendadoService {
    private final AgendadoRepository agendadoRepository;

    public AgendadoService(AgendadoRepository agendadoRepository) {
        this.agendadoRepository = agendadoRepository;
    }

    public void deleteAgendado(Long id) {
        agendadoRepository.deleteById(id);
    }

    public Agendado findById(Long id) {
        return agendadoRepository.findById(id).orElse(null);
    }

    public Agendado saveAgendado(Agendado agendado) {
        return agendadoRepository.save(agendado);
    }

    public List<Agendado> findAll() {
        return agendadoRepository.findAll();
    }

  public Optional<Agendado> findByCpf(String cpf) {
    return agendadoRepository.findByCpf(cpf);
}
    public List<Agendado> findByStatusFila(String statusFila) {
        return agendadoRepository.findByStatusFila(statusFila);
    }
    public void processTextAfterSequence(String text) {
        String sequence = "Concluído";
        String endSequence = "Agendado";
        String[] lines = text.split("\n");
        boolean foundSequence = false;
        boolean firstLineOfSequence = false;
        boolean collectingName = false;
        StringBuilder nameBuilder = new StringBuilder();

        List<Agendado> agendados = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");  // Definindo o formato da data
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");  // Definindo o formato da hora

        Agendado agendado = null;

        for (String line : lines) {
            if (foundSequence) {
                if (firstLineOfSequence) {
                    String[] parts = line.trim().split(" ");
                    if (parts.length >= 3) {
                        LocalDate dataAgendamento = LocalDate.parse(parts[0], dateFormatter); // Usando o DateTimeFormatter
                        LocalTime horaAgendada = LocalTime.parse(parts[1], timeFormatter); // Usando o DateTimeFormatter
                        String cpf = parts[2];

                        agendado = new Agendado();
                        agendado.setData(dataAgendamento);
                        agendado.setHoraMarcada(horaAgendada);
                        agendado.setCpf(cpf);
                        firstLineOfSequence = false;
                        collectingName = true;
                    }
                } else if (collectingName && agendado != null) {
                    if (line.contains("@")) {
                        collectingName = false;
                        agendado.setNome(nameBuilder.toString().trim());
                        agendado.setEmail(line.trim().split(" ")[0]);

                        agendados.add(agendado);
                        this.saveAgendado(agendado);
                    } else {
                        nameBuilder.append(line.trim()).append(" ");
                    }
                }
                if (line.contains(endSequence)) {
                    foundSequence = false;
                    firstLineOfSequence = false;
                }
            } else if (line.contains(sequence)) {
                foundSequence = true;
                firstLineOfSequence = true;
                nameBuilder.setLength(0);
            }
        }
    }
}