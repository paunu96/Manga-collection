package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "manga_series")
public class MangaSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String author;

    @Column(name = "publisher_it")
    private String publisherIt;

    @Column(name = "status_it")
    private String statusIt;

    @Column(name = "latest_volume_it")
    private Integer latestVolumeIt = 0;

    @Column(name = "status_jp")
    private String statusJp;

    @Column(name = "latest_volume_jp")
    private Integer latestVolumeJp = 0;

    @Column(name = "animeclick_url", columnDefinition = "TEXT")
    private String animeclickUrl;

    @Column(name = "anilist_id")
    private Integer anilistId;

    @Column(name = "last_scraped_at")
    private LocalDateTime lastScrapedAt;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVolume> volumes = new ArrayList<>();

    public MangaSeries() {
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisherIt() {
        return publisherIt;
    }

    public void setPublisherIt(String publisherIt) {
        this.publisherIt = publisherIt;
    }

    public String getStatusIt() {
        return statusIt;
    }

    public void setStatusIt(String statusIt) {
        this.statusIt = statusIt;
    }

    public Integer getLatestVolumeIt() {
        return latestVolumeIt;
    }

    public void setLatestVolumeIt(Integer latestVolumeIt) {
        this.latestVolumeIt = latestVolumeIt;
    }

    public String getStatusJp() {
        return statusJp;
    }

    public void setStatusJp(String statusJp) {
        this.statusJp = statusJp;
    }

    public Integer getLatestVolumeJp() {
        return latestVolumeJp;
    }

    public void setLatestVolumeJp(Integer latestVolumeJp) {
        this.latestVolumeJp = latestVolumeJp;
    }

    public String getAnimeclickUrl() {
        return animeclickUrl;
    }

    public void setAnimeclickUrl(String animeclickUrl) {
        this.animeclickUrl = animeclickUrl;
    }

    public Integer getAnilistId() {
        return anilistId;
    }

    public void setAnilistId(Integer anilistId) {
        this.anilistId = anilistId;
    }

    public LocalDateTime getLastScrapedAt() {
        return lastScrapedAt;
    }

    public void setLastScrapedAt(LocalDateTime lastScrapedAt) {
        this.lastScrapedAt = lastScrapedAt;
    }

    public List<UserVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<UserVolume> volumes) {
        this.volumes = volumes;
    }

    /**
     * Quanti volumi mancano da recuperare rispetto all'ultimo uscito in Giappone.
     */
    @Transient
    public int getVolumiArretrati() {
        if (latestVolumeJp == null || latestVolumeIt == null) {
            return 0;
        }
        return Math.max(0, latestVolumeJp - latestVolumeIt);
    }
}
