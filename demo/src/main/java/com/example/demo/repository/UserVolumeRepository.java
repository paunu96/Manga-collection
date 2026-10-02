package com.example.demo.repository;

import com.example.demo.model.UserVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserVolumeRepository extends JpaRepository<UserVolume, Long> {

    /** Volumi della serie ordinati per numero (i volumi senza numero finiscono in fondo). */
    List<UserVolume> findBySeriesIdOrderByVolumeNumber(Long seriesId);

    long countBySeriesId(Long seriesId);

    /** True se esiste già un volume di quella serie con lo stesso numero e lo stesso tipo di edizione. */
    boolean existsBySeriesIdAndVolumeNumberAndEditionType(
            Long seriesId, Integer volumeNumber, UserVolume.EditionType editionType);

    /** Il volume del tipo indicato con il numero più alto (usato per "ultimo posseduto" con REGULAR). */
    Optional<UserVolume> findFirstBySeriesIdAndEditionTypeOrderByVolumeNumberDesc(
            Long seriesId, UserVolume.EditionType editionType);
}