package com.example.demo.service;

import com.example.demo.model.MangaSeries;
import com.example.demo.model.UserVolume;
import com.example.demo.repository.UserVolumeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserVolumeService {

    private final UserVolumeRepository repository;
    private final MangaSeriesService mangaSeriesService;

    public UserVolumeService(UserVolumeRepository repository, MangaSeriesService mangaSeriesService) {
        this.repository = repository;
        this.mangaSeriesService = mangaSeriesService;
    }

    public List<UserVolume> findBySeries(Long seriesId) {
        return repository.findBySeriesIdOrderByVolumeNumber(seriesId);
    }

    public long countBySeries(Long seriesId) {
        return repository.countBySeriesId(seriesId);
    }

    /**
     * Numero dell'ultimo volume posseduto (il più alto), o null se non ne
     * possiedi ancora nessuno. A differenza del conteggio, questo valore
     * non viene alterato da un eventuale volume 0 o da varianti duplicate.
     */
    public Integer findUltimoVolumeNumero(Long seriesId) {
        return repository.findFirstBySeriesIdOrderByVolumeNumberDesc(seriesId)
                .map(UserVolume::getVolumeNumber)
                .orElse(null);
    }

    public UserVolume addVolume(Long seriesId, UserVolume volume) {
        MangaSeries series = mangaSeriesService.findById(seriesId);
        volume.setSeries(series);
        if (volume.getEdizione() == null || volume.getEdizione().isBlank()) {
            volume.setEdizione("Normale");
        }
        return repository.save(volume);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}