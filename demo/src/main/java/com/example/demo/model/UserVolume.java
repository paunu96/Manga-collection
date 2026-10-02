package com.example.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_volume")
public class UserVolume {

    public enum EditionType { REGULAR, VARIANT, SPECIAL, ARTBOOK, SPINOFF }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private MangaSeries series;

    /** Nullable: artbook e altri volumi senza numero. Lo 0 è un numero valido. */
    @Column(name = "volume_number")
    private Integer volumeNumber;

    @Column(name = "price_paid", precision = 5, scale = 2)
    private BigDecimal pricePaid;

    @Column(name = "acquired_at")
    private LocalDate acquiredAt;

    @Column(name = "cover_price", precision = 5, scale = 2)
    private BigDecimal coverPrice;

    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Etichetta libera, es. "Normale", "Variant cover Lucca 2025". */
    @Column(length = 100)
    private String edizione = "Normale";

    /** Tipo di edizione usato dalla logica (arretrato, posseduti). */
    @Enumerated(EnumType.STRING)
    @Column(name = "edition_type", nullable = false, length = 30)
    private EditionType editionType = EditionType.REGULAR;

    /** Titolo per artbook, spin-off e volumi senza numero. */
    @Column(length = 255)
    private String title;

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

    public EditionType getEditionType() {
        return editionType;
    }

    public void setEditionType(EditionType editionType) {
        this.editionType = editionType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}