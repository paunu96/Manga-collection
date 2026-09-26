package com.example.demo.repository;

import com.example.demo.model.UserVolume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserVolumeRepository extends JpaRepository<UserVolume, Long> {

    List<UserVolume> findBySeriesIdOrderByVolumeNumber(Long seriesId);

    long countBySeriesId(Long seriesId);
}
