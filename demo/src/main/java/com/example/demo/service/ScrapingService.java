package com.example.demo.service;

import com.example.demo.model.MangaSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScrapingService {

    private static final Logger log = LoggerFactory.getLogger(ScrapingService.class);

    private final MangaSeriesService mangaSeriesService;
    private final AnilistClient anilistClient;
    private final AnimeClickScraper animeClickScraper;

    public ScrapingService(MangaSeriesService mangaSeriesService,
                            AnilistClient anilistClient,
                            AnimeClickScraper animeClickScraper) {
        this.mangaSeriesService = mangaSeriesService;
        this.anilistClient = anilistClient;
        this.animeClickScraper = animeClickScraper;
    }

    /**
     * Scorre tutte le serie e prova ad aggiornare stato/volumi da Anilist
     * (JP) e AnimeClick (IT). Se una singola serie fallisce (rete, pagina
     * non trovata, ecc.), viene registrato un log e si passa alla successiva
     * senza bloccare le altre.
     */
    public void aggiornaTutto() {
        for (MangaSeries serie : mangaSeriesService.findAll()) {
            String statusIt = serie.getStatusIt();
            Integer latestVolumeIt = serie.getLatestVolumeIt();
            String statusJp = serie.getStatusJp();
            Integer latestVolumeJp = serie.getLatestVolumeJp();

            if (serie.getAnilistId() != null) {
                try {
                    AnilistClient.AnilistData dati = anilistClient.fetch(serie.getAnilistId());
                    if (dati != null) {
                        if (dati.status() != null) {
                            statusJp = mappaStatoAnilist(dati.status());
                        }
                        if (dati.volumes() != null) {
                            latestVolumeJp = dati.volumes();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Errore Anilist per la serie '{}' (anilistId={}): {}",
                            serie.getTitle(), serie.getAnilistId(), e.getMessage());
                }
            }

            if (serie.getAnimeclickUrl() != null && !serie.getAnimeclickUrl().isBlank()) {
                try {
                    AnimeClickScraper.AnimeClickData dati = animeClickScraper.scrape(serie.getAnimeclickUrl());
                    if (dati != null) {
                        if (dati.statoIt() != null) {
                            statusIt = dati.statoIt();
                        }
                        if (dati.ultimoVolume() != null) {
                            latestVolumeIt = dati.ultimoVolume();
                        }
                    }
                } catch (Exception e) {
                    log.warn("Errore AnimeClick per la serie '{}' ({}): {}",
                            serie.getTitle(), serie.getAnimeclickUrl(), e.getMessage());
                }
            }

            mangaSeriesService.updateScrapedData(serie.getId(), statusIt, latestVolumeIt, statusJp, latestVolumeJp);
        }
    }

    private String mappaStatoAnilist(String statoAnilist) {
        return switch (statoAnilist) {
            case "FINISHED" -> "Concluso";
            case "RELEASING" -> "In corso";
            case "NOT_YET_RELEASED" -> "Non ancora uscito";
            case "CANCELLED" -> "Cancellato";
            case "HIATUS" -> "In pausa";
            default -> statoAnilist;
        };
    }
}
