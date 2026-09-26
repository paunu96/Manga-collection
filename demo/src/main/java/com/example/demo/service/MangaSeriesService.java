package com.example.demo.service;

import com.example.demo.model.MangaSeries;
import com.example.demo.repository.MangaSeriesRepository;
import org.springframework.stereotype.Service;

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
}
