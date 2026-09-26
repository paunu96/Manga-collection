package com.example.demo.repository;

import com.example.demo.model.MangaSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MangaSeriesRepository extends JpaRepository<MangaSeries, Long> {

    Optional<MangaSeries> findByTitleIgnoreCase(String title);

    Optional<MangaSeries> findByAnilistId(Integer anilistId);
}
