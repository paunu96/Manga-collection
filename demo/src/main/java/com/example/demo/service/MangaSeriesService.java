package com.example.demo.service;

import com.example.demo.model.MangaSeries;
import com.example.demo.repository.MangaSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MangaSeriesService {

    private final MangaSeriesRepository repository;

    public MangaSeriesService(MangaSeriesRepository repository) {
        this.repository = repository;
    }

    public List<MangaSeries> findAll() {
        return repository.findAll();
    }

    public MangaSeries findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Serie non trovata: id=" + id));
    }

    public MangaSeries save(MangaSeries series) {
        return repository.save(series);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Aggiorna i dati recuperati dallo scraping/API (AnimeClick + Anilist)
     * e imposta il timestamp dell'ultimo aggiornamento.
     */
    public MangaSeries updateScrapedData(Long id, String statusIt, Integer latestVolumeIt,
                                          String statusJp, Integer latestVolumeJp) {
        MangaSeries series = findById(id);
        if (statusIt != null) series.setStatusIt(statusIt);
        if (latestVolumeIt != null) series.setLatestVolumeIt(latestVolumeIt);
        if (statusJp != null) series.setStatusJp(statusJp);
        if (latestVolumeJp != null) series.setLatestVolumeJp(latestVolumeJp);
        series.setLastScrapedAt(LocalDateTime.now());
        return repository.save(series);
    }
     /**
     * Aggiorna SOLO i campi modificabili dal form di modifica, partendo
     * dall'entità già salvata. Non si fa save() dell'oggetto bindato dal form:
     * non contiene lastScrapedAt, volumes ecc., e con cascade ALL +
     * orphanRemoval la lista volumi vuota cancellerebbe tutti i volumi.
     *
     * @throws IllegalArgumentException se il titolo è vuoto o la serie non esiste
     */
    @Transactional
    public MangaSeries updateFromForm(Long id, MangaSeries dati) {
        if (dati.getTitle() == null || dati.getTitle().isBlank()) {
            throw new IllegalArgumentException("Il titolo è obbligatorio.");
        }
        MangaSeries serie = findById(id);

        serie.setTitle(dati.getTitle().trim());
        serie.setAuthor(vuotoANull(dati.getAuthor()));
        serie.setPublisherIt(vuotoANull(dati.getPublisherIt()));
        serie.setStatusIt(vuotoANull(dati.getStatusIt()));
        serie.setLatestVolumeIt(dati.getLatestVolumeIt() != null ? dati.getLatestVolumeIt() : 0);
        serie.setStatusJp(vuotoANull(dati.getStatusJp()));
        serie.setLatestVolumeJp(dati.getLatestVolumeJp() != null ? dati.getLatestVolumeJp() : 0);
        serie.setAnimeclickUrl(vuotoANull(dati.getAnimeclickUrl()));
        serie.setAnilistId(dati.getAnilistId());
        serie.setTitoloRicercaAnimeclick(vuotoANull(dati.getTitoloRicercaAnimeclick()));

        // NON toccati: id, lastScrapedAt, volumes
        return repository.save(serie);
    }

    private String vuotoANull(String valore) {
        return (valore == null || valore.isBlank()) ? null : valore.trim();
    }
}
