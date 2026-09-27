package com.example.demo.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Estrae stato italiano e ultimo volume pubblicato in Italia da una scheda
 * AnimeClick (es. https://www.animeclick.it/manga/9556/one-piece).
 *
 * ATTENZIONE: AnimeClick non espone un'API, quindi questo scraper legge
 * l'HTML pubblico della pagina. Se AnimeClick cambia la struttura delle
 * proprie pagine, questa classe andrà aggiornata di conseguenza.
 */
@Service
public class AnimeClickScraper {

    private static final Logger log = LoggerFactory.getLogger(AnimeClickScraper.class);

    private static final Pattern PATTERN_DATA = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * @param statoIt        stato di pubblicazione in Italia (es. "in corso")
     * @param ultimoVolume   ultimo volume dell'edizione principale già uscito in Italia
     * @param volumiTotaliJp campo generico "Volumi" della scheda: conteggio complessivo
     *                       dell'opera (spesso allineato al Giappone), usato come stima
     *                       di riserva quando Anilist non fornisce questo dato
     */
    public record AnimeClickData(String statoIt, Integer ultimoVolume, Integer volumiTotaliJp) {
    }

    public AnimeClickData scrape(String schedaUrl, String titoloRicercaPersonalizzato) throws IOException {
        String urlBase = normalizzaUrlScheda(schedaUrl);

        Document doc = Jsoup.connect(urlBase)
                .userAgent("Mozilla/5.0 (compatible; CollezioneMangaBot/1.0)")
                .timeout(10_000)
                .get();

        String statoIt = estraiValoreDopoEtichetta(doc, "Stato in Italia");
        Integer volumiTotaliJp = estraiNumeroDopoEtichetta(doc, "Volumi");

        String titoloDaCercare = (titoloRicercaPersonalizzato != null && !titoloRicercaPersonalizzato.isBlank())
                ? titoloRicercaPersonalizzato.trim()
                : estraiTitoloBase(doc);
        Integer ultimoVolume = calcolaUltimoVolumeUscito(urlBase, titoloDaCercare);

        return new AnimeClickData(statoIt, ultimoVolume, volumiTotaliJp);
    }

    /**
     * Riporta l'URL alla scheda principale, togliendo un eventuale "/edizioni"
     * finale nel caso l'utente l'abbia incollato per sbaglio.
     */
    private String normalizzaUrlScheda(String schedaUrl) {
        String url = schedaUrl.replaceAll("/+$", "");
        if (url.endsWith("/edizioni")) {
            url = url.substring(0, url.length() - "/edizioni".length());
        }
        return url;
    }

    /**
     * Cerca un elemento il cui testo diretto corrisponde all'etichetta data
     * (es. "Stato in Italia") e restituisce il testo dell'elemento successivo
     * (il valore). Approccio generico che non dipende da classi CSS specifiche,
     * per essere più resistente a piccoli cambi di stile della pagina.
     */
    private String estraiValoreDopoEtichetta(Document doc, String etichetta) {
        for (Element el : doc.select("*")) {
            if (el.ownText().trim().equalsIgnoreCase(etichetta)) {
                Element successivo = el.nextElementSibling();
                if (successivo != null && !successivo.text().isBlank()) {
                    return successivo.text().trim();
                }
            }
        }
        return null;
    }

    /**
     * Come {@link #estraiValoreDopoEtichetta}, ma interpreta il valore come
     * numero intero (usato per il campo "Volumi" della scheda principale).
     */
    private Integer estraiNumeroDopoEtichetta(Document doc, String etichetta) {
        String valore = estraiValoreDopoEtichetta(doc, etichetta);
        if (valore == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(valore);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    /** Nessun manga reale supera questo numero di volumi: oltre questa soglia, il dato è quasi certamente un errore di lettura. */
    private static final int VOLUME_MASSIMO_PLAUSIBILE = 500;

    /**
     * Apre la pagina "edizioni" della scheda (elenco di ogni volume con la
     * relativa data di uscita) e restituisce il numero di volume più alto
     * tra quelli con data di uscita non futura, per il titolo indicato.
     */
    private Integer calcolaUltimoVolumeUscito(String urlBaseScheda, String titoloDaCercare) throws IOException {
        String edizioniUrl = urlBaseScheda + "/edizioni";
        Document doc = Jsoup.connect(edizioniUrl)
                .userAgent("Mozilla/5.0 (compatible; CollezioneMangaBot/1.0)")
                .timeout(10_000)
                .get();

        log.info("AnimeClick edizioni [{}] -> titolo pagina ricevuta: \"{}\", numero tabelle trovate: {}",
                edizioniUrl, doc.title(), doc.select("table").size());

        Element tabellaEdizioni = trovaTabellaEdizioni(doc);
        if (tabellaEdizioni == null) {
            log.warn("Nessuna tabella con 'Titolo'+'Uscita' trovata in {}", edizioniUrl);
            return null;
        }
        log.info("Tabella edizioni trovata, righe: {}", tabellaEdizioni.select("tr").size());

        Pattern patternTitoloVolume = costruisciPatternTitoloVolume(titoloDaCercare);
        log.info("Titolo usato per la ricerca: \"{}\"", titoloDaCercare);

        LocalDate oggi = LocalDate.now();
        int massimoVolumeUscito = 0;

        for (Element riga : tabellaEdizioni.select("tr")) {
            Elements celle = riga.select("td");
            if (celle.isEmpty()) {
                continue;
            }

            LocalDate dataUscita = null;
            Integer numeroVolume = null;

            for (Element cella : celle) {
                String testo = cella.text().trim();
                if (testo.isEmpty()) {
                    continue;
                }
                Matcher dataMatcher = PATTERN_DATA.matcher(testo);
                if (dataMatcher.find() && dataUscita == null) {
                    try {
                        dataUscita = LocalDate.parse(dataMatcher.group(), FORMATO_DATA);
                    } catch (Exception ignorato) {
                        // testo simile a una data ma non parsabile, ignora
                    }
                    continue;
                }
                if (numeroVolume == null && patternTitoloVolume != null) {
                    Matcher titoloMatcher = patternTitoloVolume.matcher(testo);
                    if (titoloMatcher.matches()) {
                        numeroVolume = Integer.parseInt(titoloMatcher.group(1));
                    }
                }
            }

            if (dataUscita == null || numeroVolume == null) {
                continue;
            }
            if (dataUscita.isAfter(oggi)) {
                continue;
            }
            if (numeroVolume <= VOLUME_MASSIMO_PLAUSIBILE) {
                massimoVolumeUscito = Math.max(massimoVolumeUscito, numeroVolume);
            }
        }

        log.info("Ultimo volume calcolato per \"{}\": {}", titoloDaCercare, massimoVolumeUscito);
        return massimoVolumeUscito > 0 ? massimoVolumeUscito : null;
    }

    /**
     * Ricava il titolo "nudo" della serie dal &lt;title&gt; della pagina, che su
     * AnimeClick ha il formato "Titolo - edizioni - (Manga)".
     */
    private String estraiTitoloBase(Document doc) {
        String titolo = doc.title();
        int posizioneTrattino = titolo.indexOf(" - ");
        if (posizioneTrattino > 0) {
            titolo = titolo.substring(0, posizioneTrattino);
        }
        return titolo.trim();
    }

    /**
     * Costruisce un pattern che riconosce SOLO le righe dell'edizione
     * principale, del tipo "Titolo &lt;numero&gt;" (es. "One Piece 113"),
     * escludendo ristampe, edizioni speciali, artbook e altri prodotti
     * collegati che iniziano con lo stesso titolo ma proseguono con altre
     * parole (es. "One Piece New Edition 14", "One Piece Green").
     */
    private Pattern costruisciPatternTitoloVolume(String titoloBase) {
        if (titoloBase == null || titoloBase.isBlank()) {
            return null;
        }
        return Pattern.compile("^" + Pattern.quote(titoloBase) + "\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Trova, tra tutte le tabelle della pagina, quella con l'elenco delle
     * edizioni: la riconosce perché contiene sia "Titolo" che "Uscita"
     * nell'intestazione. Ignora ogni altra tabella presente nella pagina
     * (menu, contenuti correlati, ecc.) per evitare di leggere numeri
     * provenienti da sezioni non pertinenti.
     */
    private Element trovaTabellaEdizioni(Document doc) {
        for (Element tabella : doc.select("table")) {
            String testo = tabella.text();
            if (testo.contains("Titolo") && testo.contains("Uscita")) {
                return tabella;
            }
        }
        return null;
    }
}