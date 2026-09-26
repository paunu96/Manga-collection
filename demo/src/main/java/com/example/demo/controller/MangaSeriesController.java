package com.example.demo.controller;

import com.example.demo.model.MangaSeries;
import com.example.demo.model.UserVolume;
import com.example.demo.service.MangaSeriesService;
import com.example.demo.service.ScrapingService;
import com.example.demo.service.UserVolumeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        Map<Long, Long> conteggioVolumi = new HashMap<>();
        for (MangaSeries s : serie) {
            conteggioVolumi.put(s.getId(), userVolumeService.countBySeries(s.getId()));
        }
        model.addAttribute("serie", serie);
        model.addAttribute("conteggioVolumi", conteggioVolumi);
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
        model.addAttribute("mangaSeries", mangaSeriesService.findById(id));
        model.addAttribute("volumi", volumi);
        model.addAttribute("volumiMancanti", calcolaVolumiMancanti(volumi));
        model.addAttribute("nuovoVolume", new UserVolume());
        return "serie-dettaglio";
    }

    /**
     * Dato l'elenco dei volumi posseduti, calcola i numeri "buchi":
     * es. se possiedi 1, 2, 4, 5, 8 → mancano 3, 6, 7 (fino all'ultimo posseduto).
     */
    private List<Integer> calcolaVolumiMancanti(List<UserVolume> volumi) {
        if (volumi.isEmpty()) {
            return List.of();
        }
        int massimo = volumi.stream().mapToInt(UserVolume::getVolumeNumber).max().orElse(0);
        Set<Integer> posseduti = volumi.stream()
                .map(UserVolume::getVolumeNumber)
                .collect(Collectors.toSet());
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
    public String aggiungiVolume(@PathVariable Long seriesId, @ModelAttribute UserVolume nuovoVolume) {
        userVolumeService.addVolume(seriesId, nuovoVolume);
        return "redirect:/serie/" + seriesId;
    }

    @PostMapping("/serie/{seriesId}/volumi/{volumeId}/elimina")
    public String eliminaVolume(@PathVariable Long seriesId, @PathVariable Long volumeId) {
        userVolumeService.delete(volumeId);
        return "redirect:/serie/" + seriesId;
    }
}
