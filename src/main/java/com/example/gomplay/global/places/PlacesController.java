package com.example.gomplay.global.places;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlacesController {

    private final PlacesService placesService;

    @GetMapping("/nearby")
    public ResponseEntity<List<PlaceResponse>> getNearbyPlaces(
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(required = false) String sportType,
        @RequestParam(defaultValue = "rating") String sortBy) {
    return ResponseEntity.ok(placesService.getNearbyPlaces(lat, lng, sportType, sortBy));
    }

    
}