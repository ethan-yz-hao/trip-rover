package org.ethanhao.triprover.controller;

import org.ethanhao.triprover.domain.ResponseResult;
import org.ethanhao.triprover.service.PlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/place")
@Slf4j
public class PlaceController {

    private final PlaceService placeService;

    @Autowired
    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @GetMapping("/details/{placeId}")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<?> getPlaceDetails(@PathVariable String placeId) {
        log.info("Fetching place details for placeId: {}", placeId);
        Object placeDetails = placeService.getPlaceDetails(placeId);
        return new ResponseResult<>(200, "Success", placeDetails);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<?> searchPlaces(@RequestParam String query,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius) {
        log.info("Searching places with query: {}, location: ({}, {}), radius: {}",
                query, lat, lng, radius);
        Object searchResults = placeService.searchPlaces(query, lat, lng, radius);
        return new ResponseResult<>(200, "Success", searchResults);
    }

    @GetMapping("/autocomplete")
    @PreAuthorize("hasAuthority('user:all')")
    public ResponseResult<?> autocompletePlaces(@RequestParam String input,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Integer radius) {
        log.info("Autocomplete places with input: {}, location: ({}, {}), radius: {}",
                input, lat, lng, radius);
        Object autocompleteResults = placeService.autocompletePlaces(input, lat, lng, radius);
        return new ResponseResult<>(200, "Success", autocompleteResults);
    }
}