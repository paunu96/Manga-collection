package com.example.demo.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    private static final Pattern PATTERN_DATA = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");
    private static final Pattern PATTERN_PREZZO = Pattern.compile("\\d+[,.]\\d{2}\\s*€");
    private static final Pattern PATTERN_NUMERO_FINALE = Pattern.compile("(\\d+)\\s*$");
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public record AnimeClickData(String statoIt, Integer ultimoVolume) {
    }

    public AnimeClickData scrape(String schedaUrl) throws IOException {
        Document doc = Jsoup.connect(schedaUrl)
                .userAgent("Mozilla/5.0 (compatible; CollezioneMangaBot/1.0)")
                .timeout(10_000)
                .get();

        String statoIt = estraiValoreDopoEtichetta(doc, "Stato in Italia");
        Integer ultimoVolume = calcolaUltimoVolumeUscito(schedaUrl);

        return new AnimeClickData(statoIt, ultimoVolume);
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
     * Apre la pagina "edizioni" della scheda (elenco di ogni volume con la
     * relativa data di uscita) e restituisce il numero di volume più alto
     * tra quelli con data di uscita non futura.
     */
    private Integer calcolaUltimoVolumeUscito(String schedaUrl) throws IOException {
        String edizioniUrl = schedaUrl.replaceAll("/+$", "") + "/edizioni";
        Document doc = Jsoup.connect(edizioniUrl)
                .userAgent("Mozilla/5.0 (compatible; CollezioneMangaBot/1.0)")
                .timeout(10_000)
                .get();

        LocalDate oggi = LocalDate.now();
        int massimoVolumeUscito = 0;

        for (Element riga : doc.select("table tr")) {
            Elements celle = riga.select("td");
            if (celle.isEmpty()) {
                continue;
            }

            LocalDate dataUscita = null;
            String celleTitolo = null;

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
                // Scarta celle che sono prezzi o sequenze di piccoli numeri (voti)
                if (PATTERN_PREZZO.matcher(testo).find()) {
                    continue;
                }
                if (celleTitolo == null && PATTERN_NUMERO_FINALE.matcher(testo).find()) {
                    celleTitolo = testo;
                }
            }

            if (dataUscita == null || celleTitolo == null) {
                continue;
            }
            if (dataUscita.isAfter(oggi)) {
                continue;
            }

            Matcher numeroMatcher = PATTERN_NUMERO_FINALE.matcher(celleTitolo);
            if (numeroMatcher.find()) {
                int numero = Integer.parseInt(numeroMatcher.group(1));
                massimoVolumeUscito = Math.max(massimoVolumeUscito, numero);
            }
        }

        return massimoVolumeUscito > 0 ? massimoVolumeUscito : null;
    }
}
