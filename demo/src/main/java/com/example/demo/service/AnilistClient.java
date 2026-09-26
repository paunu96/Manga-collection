package com.example.demo.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Interroga l'API GraphQL pubblica di Anilist (https://anilist.co) per
 * ottenere stato e numero di volumi totali di un'opera, dato il suo anilistId.
 *
 * Nota: il campo "volumes" per opere ancora in corso è mantenuto dalla
 * community di Anilist e non sempre è aggiornato in tempo reale ad ogni
 * uscita in Giappone.
 */
@Service
public class AnilistClient {

    private static final String ENDPOINT = "https://graphql.anilist.co";

    private static final String QUERY = """
            query ($id: Int) {
              Media(id: $id, type: MANGA) {
                status
                volumes
              }
            }
            """;

    private final RestClient restClient = RestClient.create();

    public record AnilistData(String status, Integer volumes) {
    }

    private record GraphQlRequest(String query, Map<String, Object> variables) {
    }

    private record MediaWrapper(AnilistData Media) {
    }

    private record GraphQlResponse(MediaWrapper data) {
    }

    /**
     * Restituisce i dati Anilist per l'id passato, o null se l'id è nullo
     * o la chiamata non produce risultati utilizzabili.
     */
    public AnilistData fetch(Integer anilistId) {
        if (anilistId == null) {
            return null;
        }
        GraphQlRequest richiesta = new GraphQlRequest(QUERY, Map.of("id", anilistId));

        GraphQlResponse risposta = restClient.post()
                .uri(ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(richiesta)
                .retrieve()
                .body(GraphQlResponse.class);

        if (risposta == null || risposta.data() == null) {
            return null;
        }
        return risposta.data().Media();
    }
}
