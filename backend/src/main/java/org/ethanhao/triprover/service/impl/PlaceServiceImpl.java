package org.ethanhao.triprover.service.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.ethanhao.triprover.service.PlaceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlaceServiceImpl implements PlaceService {

    private final RestTemplate restTemplate;
    private final String apiKey;

    // Base URLs for Google Maps APIs (new version)
    private static final String PLACE_DETAILS_URL = "https://places.googleapis.com/v1/places/";
    private static final String PLACE_SEARCH_URL = "https://places.googleapis.com/v1/places:searchText";
    private static final String PLACE_AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete";

    // Fields to request for place details
    private static final String PLACE_DETAILS_FIELDS = "id,displayName,formattedAddress,location,types,photos,addressComponents,viewport";

    public PlaceServiceImpl(
            @Value("${google.maps.api.key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    @Override
    public Object getPlaceDetails(String placeId) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(PLACE_DETAILS_URL + placeId)
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("X-Goog-FieldMask", PLACE_DETAILS_FIELDS);
            headers.set("Content-Type", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Failed to get place details: {}", response.getStatusCode());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place details not found");
            }
        } catch (Exception e) {
            log.error("Error fetching place details", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error fetching place details: " + e.getMessage());
        }
    }

    @Override
    public Object searchPlaces(String query, Double lat, Double lng, Integer radius) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(PLACE_SEARCH_URL)
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("Content-Type", "application/json");

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("textQuery", query);

            // Add location bias if coordinates are provided
            if (lat != null && lng != null) {
                Map<String, Object> locationBias = new HashMap<>();
                Map<String, Object> circle = new HashMap<>();
                Map<String, Object> center = new HashMap<>();

                center.put("latitude", lat);
                center.put("longitude", lng);
                circle.put("center", center);

                // Use radius if provided, otherwise default to 5000 meters
                circle.put("radius", radius != null ? radius : 5000);

                locationBias.put("circle", circle);
                requestBody.put("locationBias", locationBias);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return responseBody != null ? responseBody.get("places") : new HashMap<>();
            } else {
                log.error("Failed to search places: {}", response.getStatusCode());
                return new HashMap<>(); // Return empty results instead of throwing an error
            }
        } catch (Exception e) {
            log.error("Error searching places", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error searching places: " + e.getMessage());
        }
    }

    @Override
    public Object autocompletePlaces(String input, Double lat, Double lng, Integer radius) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(PLACE_AUTOCOMPLETE_URL)
                    .queryParam("key", apiKey)
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Goog-Api-Key", apiKey);
            headers.set("Content-Type", "application/json");

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", input);

            // Add location bias if coordinates are provided
            if (lat != null && lng != null) {
                Map<String, Object> locationBias = new HashMap<>();
                Map<String, Object> rectangle = new HashMap<>();

                // Create a rectangle around the provided point
                double latDelta = radius != null ? radius / 111000.0 : 0.05; // Convert meters to degrees or use default
                double lngDelta = radius != null ? radius / (111000.0 * Math.cos(Math.toRadians(lat))) : 0.05;

                Map<String, Object> southwest = new HashMap<>();
                southwest.put("latitude", lat - latDelta);
                southwest.put("longitude", lng - lngDelta);

                Map<String, Object> northeast = new HashMap<>();
                northeast.put("latitude", lat + latDelta);
                northeast.put("longitude", lng + lngDelta);

                rectangle.put("low", southwest);
                rectangle.put("high", northeast);

                locationBias.put("rectangle", rectangle);
                requestBody.put("locationBias", locationBias);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                return responseBody != null ? responseBody.get("predictions") : new HashMap<>();
            } else {
                log.error("Failed to get autocomplete predictions: {}", response.getStatusCode());
                return new HashMap<>(); // Return empty results instead of throwing an error
            }
        } catch (Exception e) {
            log.error("Error getting autocomplete predictions", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error getting autocomplete predictions: " + e.getMessage());
        }
    }
}