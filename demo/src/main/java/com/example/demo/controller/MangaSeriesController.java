package com.example.demo.controller;

import com.example.demo.model.MangaSeries;
import com.example.demo.model.UserVolume;
import com.example.demo.service.MangaSeriesService;
import com.example.demo.service.ScrapingService;
import com.example.demo.service.UserVolumeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class MangaSeriesController {

    private final MangaSeriesService mangaSeriesService;
    private final UserVolumeService userVolumeService;
    private final ScrapingService scrapingService;

    public MangaSeriesController(MangaSeriesService mangaSeriesService, UserVolumeService userVolumeService,
                                  ScrapingService scrapingService) {
        this.mangaSeriesService = mangaSeriesService;
        this.userVolumeService = userVolumeService;
        this.scrapingService = scrapingService;
    }

    /** Avvia subito l'aggiornamento da Anilist/AnimeClick, senza aspettare lo scheduler. */
    @PostMapping("/admin/aggiorna")
    public String aggiornaOra() {
        scrapingService.aggiornaTutto();
        return "redirect:/";
    }

    /** Tabella con tutta la collezione. */
    @GetMapping("/")
    public String elenco(Model model) {
        List<MangaSeries> serie = mangaSeriesService.findAll();
        Map<Long, Integer> ultimoPossedutoPerSerie = new HashMap<>();
        Map<Long, Integer> arretratoPerSerie = new HashMap<>();

        for (MangaSeries s : serie) {
            Integer ultimoPosseduto = userVolumeService.findUltimoVolumeNumero(s.getId());
            ultimoPossedutoPerSerie.put(s.getId(), ultimoPosseduto);

            int posseduto = ultimoPosseduto != null ? ultimoPosseduto : 0;
            int uscitiIt = s.getLatestVolumeIt() != null ? s.getLatestVolumeIt() : 0;
            arretratoPerSerie.put(s.getId(), Math.max(0, uscitiIt - posseduto));
        }

        model.addAttribute("serie", serie);
        model.addAttribute("ultimoPossedutoPerSerie", ultimoPossedutoPerSerie);
        model.addAttribute("arretratoPerSerie", arretratoPerSerie);
        return "elenco";
    }

    /** Form per aggiungere una nuova serie. */
    @GetMapping("/serie/nuova")
    public String formNuovaSerie(Model model) {
        model.addAttribute("mangaSeries", new MangaSeries());
        return "serie-form";
    }

    @PostMapping("/serie")
    public String salvaSerie(@ModelAttribute MangaSeries mangaSeries) {
        mangaSeriesService.save(mangaSeries);
        return "redirect:/";
    }

    /** Dettaglio di una serie con i volumi posseduti. */
    @GetMapping("/serie/{id}")
    public String dettaglioSerie(@PathVariable Long id, Model model) {
        List<UserVolume> volumi = userVolumeService.findBySeries(id);
        MangaSeries mangaSeries = mangaSeriesService.findById(id);
        model.addAttribute("mangaSeries", mangaSeries);
        model.addAttribute("volumi", volumi);
        model.addAttribute("volumiMancanti", calcolaVolumiMancanti(volumi, mangaSeries.getLatestVolumeIt()));
        model.addAttribute("nuovoVolume", new UserVolume());
        return "serie-dettaglio";
    }

    /**
     * Calcola i numeri "buchi" tra i volumi REGOLARI, fino al più alto tra
     * l'ultimo posseduto e l'ultimo uscito in Italia. Variant, artbook e
     * spin-off (e qualunque volume senza numero) vengono ignorati.
     */
    private List<Integer> calcolaVolumiMancanti(List<UserVolume> volumi, Integer latestVolumeIt) {
        Set<Integer> posseduti = volumi.stream()
                .filter(v -> v.getEditionType() == UserVolume.EditionType.REGULAR)
                .map(UserVolume::getVolumeNumber)
                .filter(n -> n != null)
                .collect(Collectors.toSet());
        int massimoPosseduto = posseduti.stream().mapToInt(Integer::intValue).max().orElse(0);
        int uscitiIt = latestVolumeIt != null ? latestVolumeIt : 0;
        int massimo = Math.max(massimoPosseduto, uscitiIt);

        if (massimo == 0) {
            return List.of();
        }
        List<Integer> mancanti = new ArrayList<>();
        for (int n = 1; n <= massimo; n++) {
            if (!posseduti.contains(n)) {
                mancanti.add(n);
            }
        }
        return mancanti;
    }

    @PostMapping("/serie/{id}/elimina")
    public String eliminaSerie(@PathVariable Long id) {
        mangaSeriesService.delete(id);
        return "redirect:/";
    }

    /** Registra un volume posseduto per una serie. */
    @PostMapping("/serie/{seriesId}/volumi")
    public String aggiungiVolume(@PathVariable Long seriesId, @ModelAttribute UserVolume nuovoVolume,
                                 RedirectAttributes redirectAttributes) {
        try {
            userVolumeService.addVolume(seriesId, nuovoVolume);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errore", e.getMessage());
        }
        return "redirect:/serie/" + seriesId;
    }

    @PostMapping("/serie/{seriesId}/volumi/{volumeId}/elimina")
    public String eliminaVolume(@PathVariable Long seriesId, @PathVariable Long volumeId) {
        userVolumeService.delete(volumeId);
        return "redirect:/serie/" + seriesId;
    }
}