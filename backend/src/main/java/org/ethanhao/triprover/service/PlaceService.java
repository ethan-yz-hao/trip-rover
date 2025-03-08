package org.ethanhao.triprover.service;

public interface PlaceService {

    /**
     * Get details for a specific place using its Google Place ID
     * 
     * @param placeId The Google Place ID
     * @return Place details from Google Maps API
     */
    Object getPlaceDetails(String placeId);

    /**
     * Search for places using the Google Places API
     * 
     * @param query  The search query
     * @param lat    Optional latitude for location-based search
     * @param lng    Optional longitude for location-based search
     * @param radius Optional radius in meters for location-based search
     * @return Search results from Google Maps API
     */
    Object searchPlaces(String query, Double lat, Double lng, Integer radius);

    /**
     * Get place predictions for autocomplete functionality
     * 
     * @param input  The input text for autocomplete
     * @param lat    Optional latitude for location-based predictions
     * @param lng    Optional longitude for location-based predictions
     * @param radius Optional radius in meters for location-based predictions
     * @return Autocomplete predictions from Google Maps API
     */
    Object autocompletePlaces(String input, Double lat, Double lng, Integer radius);
}