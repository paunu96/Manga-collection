package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_volume",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_series_volume_edizione",
                columnNames = {"series_id", "volume_number", "edizione"}
        ))
public class UserVolume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private MangaSeries series;

    @Column(name = "volume_number", nullable = false)
    private Integer volumeNumber;

    @Column(name = "price_paid", precision = 5, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "acquired_at")
    private LocalDate acquiredAt;

    @Column(name = "cover_price", precision = 5, scale = 2)
    private BigDecimal coverPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Distingue varianti dello stesso volume (es. "Normale", "Variant cover"). */
    @Column(length = 100)
    private String edizione = "Normale";

    public UserVolume() {
    }

    // --- Getters e Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MangaSeries getSeries() {
        return series;
    }

    public void setSeries(MangaSeries series) {
        this.series = series;
    }

    public Integer getVolumeNumber() {
        return volumeNumber;
    }

    public void setVolumeNumber(Integer volumeNumber) {
        this.volumeNumber = volumeNumber;
    }

    public BigDecimal getPricePaid() {
        return pricePaid;
    }

    public void setPricePaid(BigDecimal pricePaid) {
        this.pricePaid = pricePaid;
    }

    public LocalDate getAcquiredAt() {
        return acquiredAt;
    }

    public void setAcquiredAt(LocalDate acquiredAt) {
        this.acquiredAt = acquiredAt;
    }

    public BigDecimal getCoverPrice() {
        return coverPrice;
    }

    public void setCoverPrice(BigDecimal coverPrice) {
        this.coverPrice = coverPrice;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getEdizione() {
        return edizione;
    }

    public void setEdizione(String edizione) {
        this.edizione = edizione;
    }
}