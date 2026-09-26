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

    public UserVolume addVolume(Long seriesId, UserVolume volume) {
        MangaSeries series = mangaSeriesService.findById(seriesId);
        volume.setSeries(series);
        return repository.save(volume);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
