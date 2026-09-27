package com.example.demo.repository;

import com.example.demo.model.UserVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVolumeRepository extends JpaRepository<UserVolume, Long> {

    List<UserVolume> findBySeriesIdOrderByVolumeNumber(Long seriesId);

    long countBySeriesId(Long seriesId);

    /** Il volume con il numero più alto posseduto per questa serie (utile per "ultimo posseduto"). */
    Optional<UserVolume> findFirstBySeriesIdOrderByVolumeNumberDesc(Long seriesId);
}