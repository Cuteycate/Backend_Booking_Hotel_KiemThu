package com.example.hotel_booking_be_v1.controller;

import com.example.hotel_booking_be_v1.model.SearchResultDTO;
import com.example.hotel_booking_be_v1.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/autocomplete")
    public List<SearchResultDTO> autocomplete(@RequestParam("query") String query) {
        return searchService.searchLocationsAndHotels(query);
    }
}
