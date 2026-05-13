package com.example.gomplay.global.places;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlacesService {

    @Value("${google.places.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private static final Logger log = LoggerFactory.getLogger(PlacesService.class);

    public List<PlaceResponse> getNearbyPlaces(double lat, double lng, String sportType, String sortBy) {
        String keyword = getKeyword(sportType);

        String url = "https://places.googleapis.com/v1/places:searchText";

        String requestBody = """
                {
                    "textQuery": "%s",
                    "locationBias": {
                        "circle": {
                            "center": {
                                "latitude": %f,
                                "longitude": %f
                            },
                            "radius": 2000.0
                        }
                    }
                }
                """.formatted(keyword, lat, lng);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", "places.displayName,places.formattedAddress,places.location,places.rating");

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

        List<PlaceResponse> places = new ArrayList<>();

        if (response != null && response.containsKey("places")) {
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("places");

            for (Map<String, Object> result : results) {
                Map<String, Object> displayName = (Map<String, Object>) result.get("displayName");
                String name = displayName != null ? (String) displayName.get("text") : "";
                String address = (String) result.getOrDefault("formattedAddress", "");
                double rating = result.containsKey("rating")
                        ? ((Number) result.get("rating")).doubleValue() : 0.0;

                Map<String, Object> location = (Map<String, Object>) result.get("location");
                double placeLat = ((Number) location.get("latitude")).doubleValue();
                double placeLng = ((Number) location.get("longitude")).doubleValue();

                String distance = calculateDistance(lat, lng, placeLat, placeLng);

                places.add(new PlaceResponse(name, address, placeLat, placeLng, rating, distance));
            }
        }

        if ("distance".equals(sortBy)) {
            places.sort((a, b) -> {
                double distA = parseDistance(a.getDistance());
                double distB = parseDistance(b.getDistance());
                return Double.compare(distA, distB);
            });
        } else {
            places.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        }

        return places;
    }

    private String getKeyword(String sportType) {
        if (sportType == null) return "운동 시설";
        return switch (sportType) {
            case "축구", "풋살" -> "풋살장 축구장";
            case "농구" -> "농구장";
            case "배드민턴" -> "배드민턴장";
            case "테니스" -> "테니스장";
            case "헬스" -> "헬스장 피트니스";
            case "등산" -> "등산로 공원";
            case "런닝" -> "공원 트랙 운동장";
            case "자전거" -> "자전거 도로";
            case "당구" -> "당구장";
            case "야구" -> "야구장 배팅연습장";
            case "볼링" -> "볼링장";
            default -> "운동 시설";
        };
    }

    private double parseDistance(String distance) {
        if (distance.endsWith("km")) {
            return Double.parseDouble(distance.replace("km", "")) * 1000;
        } else {
            return Double.parseDouble(distance.replace("m", ""));
        }
    }

    private String calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        if (distance < 1000) {
            return (int) distance + "m";
        } else {
            return String.format("%.1fkm", distance / 1000);
        }
    }
}