package com.example.gomplay.global.places;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class PlaceResponse {
    private String name;
    private String address;
    private double lat;
    private double lng;
    private double rating;
    private String distance;
}