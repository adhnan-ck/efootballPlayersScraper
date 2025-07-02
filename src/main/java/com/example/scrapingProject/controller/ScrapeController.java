package com.example.scrapingProject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scrapingProject.records.Player;
import com.example.scrapingProject.service.ScrapingService;

import java.util.List;

@RestController
public class ScrapeController {

    private final ScrapingService scrapingService;

    // Use constructor injection - it's a best practice
    @Autowired
    public ScrapeController(ScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @GetMapping("/players")
    public List<Player> getQuotes() {
        // Call the service to perform the scraping
        return scrapingService.scrapePlayers();
    }
}
