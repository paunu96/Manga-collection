package com.example.demo.service;

import com.example.demo.model.MangaSeries;
import com.example.demo.model.UserVolume;
import com.example.demo.model.UserVolume.EditionType;
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
     * Numero dell'ultimo volume REGOLARE posseduto (il più alto), o null se
     * non ne possiedi ancora nessuno. Varianti, artbook e spin-off non contano.
     */
    public Integer findUltimoVolumeNumero(Long seriesId) {
        return repository
                .findFirstBySeriesIdAndEditionTypeOrderByVolumeNumberDesc(seriesId, EditionType.REGULAR)
                .map(UserVolume::getVolumeNumber)
                .orElse(null);
    }

    /**
     * Registra un volume. Lancia IllegalArgumentException con un messaggio
     * leggibile se i dati non sono validi o se il volume regolare esiste già.
     */
    public UserVolume addVolume(Long seriesId, UserVolume volume) {
        if (volume.getEditionType() == null) {
            volume.setEditionType(EditionType.REGULAR);
        }
        EditionType tipo = volume.getEditionType();

        if (volume.getTitle() != null && volume.getTitle().isBlank()) {
            volume.setTitle(null);
        }

        if (tipo == EditionType.REGULAR) {
            if (volume.getVolumeNumber() == null) {
                throw new IllegalArgumentException("Per un volume regolare il numero è obbligatorio.");
            }
            if (repository.existsBySeriesIdAndVolumeNumberAndEditionType(
                    seriesId, volume.getVolumeNumber(), EditionType.REGULAR)) {
                throw new IllegalArgumentException("Possiedi già il volume regolare " + volume.getVolumeNumber()
                        + ". Se è una copia diversa, scegli il tipo Variant.");
            }
        } else if (volume.getVolumeNumber() == null && volume.getTitle() == null) {
            throw new IllegalArgumentException("Inserisci almeno il numero o il titolo del volume.");
        }

        // edizione è NOT NULL nel database
        String edizione = volume.getEdizione();
        if (edizione == null || edizione.isBlank()
                || (tipo != EditionType.REGULAR && edizione.equals("Normale"))) {
            volume.setEdizione(etichetta(tipo));
        }

        MangaSeries series = mangaSeriesService.findById(seriesId);
        volume.setSeries(series);
        return repository.save(volume);
    }

    private String etichetta(EditionType tipo) {
        return switch (tipo) {
            case REGULAR -> "Normale";
            case VARIANT -> "Variant";
            case SPECIAL -> "Speciale";
            case ARTBOOK -> "Artbook";
            case SPINOFF -> "Spin-off";
        };
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}