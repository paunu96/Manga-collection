package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AggiornamentoScheduler {

    private static final Logger log = LoggerFactory.getLogger(AggiornamentoScheduler.class);

    private final ScrapingService scrapingService;

    public AggiornamentoScheduler(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    /** Ogni giorno alle 6:00 (ora del server). */
    @Scheduled(cron = "0 0 6 * * *")
    public void aggiornamentoGiornaliero() {
        log.info("Avvio aggiornamento automatico giornaliero (Anilist + AnimeClick)");
        scrapingService.aggiornaTutto();
        log.info("Aggiornamento automatico giornaliero completato");
    }
}
