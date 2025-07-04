package com.example.scrapingProject.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.example.scrapingProject.records.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScrapingService {

    String BASE_URL = "https://pesdb.net/efootball/";
    
    public List<Player> scrapePlayers() {
        List<Player> playerList = new ArrayList<>();
        int currentPage = 1;
        boolean hasMorePages = true;
        
        while (hasMorePages) {
            try {
                // Construct URL for current page
                String url = currentPage == 1 ? BASE_URL : BASE_URL + "?page=" + currentPage;
                System.out.println("Scraping page " + currentPage + ": " + url);
                
                // Connect to the website and parse the document
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

                // Select the players table
                Elements table = doc.select("table.players");
                
                if (table.isEmpty()) {
                    System.out.println("No players table found on page " + currentPage);
                    hasMorePages = false;
                    break;
                }

                // Get all rows from the table body
                Elements rows = table.select("tbody tr");
                
                if (rows.isEmpty()) {
                    System.out.println("No player rows found on page " + currentPage);
                    hasMorePages = false;
                    break;
                }

                // Process each row
                for (Element row : rows) {
                    try {
                        Elements cells = row.select("td"); // removed th 
                        
                        if (cells.size() >= 8) {
                            String position = cells.get(0).text();
                            String player = cells.get(1).text();
                            String teamName = cells.get(2).text();
                            String nationality = cells.get(3).text();
                            String height = cells.get(4).text();
                            String weight = cells.get(5).text();
                            String age = cells.get(6).text();
                            String overall = cells.get(7).text();
                            
                            playerList.add(new Player(position, player, teamName, nationality, height, weight, age, overall));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing row on page " + currentPage + ": " + e.getMessage());
                    }
                }

                System.out.println("Scraped " + rows.size() + " players from page " + currentPage);
                
                // Check if there's a next page by looking for pagination elements
                hasMorePages = hasNextPage(doc, currentPage);
                currentPage++;
                
                // // Try reducing the sleep time to scrape fast. increase the scrape time if the website is blocking. 
                Thread.sleep(2000); 
                
            } catch (IOException e) {
                System.err.println("Error scraping page " + currentPage + ": " + e.getMessage());
                hasMorePages = false;
            
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Total players scraped: " + playerList.size());
        return playerList;
    }
    
    private boolean hasNextPage(Document doc, int currentPage) {
        // Method 1: Check for pagination links
        Elements paginationLinks = doc.select("a[href*='page=']");
        for (Element link : paginationLinks) {
            String href = link.attr("href");
            if (href.contains("page=" + (currentPage + 1))) {
                return true;
            }
        }
        
        // Method 2: Check for "Next" button or similar
        Elements nextButton = doc.select("a:contains(Next), a:contains(›), a:contains(>>)");
        if (!nextButton.isEmpty()) {
            return true;
        }
        
        // Method 3: Try to access the next page directly (fallback)
        try {
            String nextUrl = BASE_URL + "?page=" + (currentPage + 1);
            Document nextDoc = Jsoup.connect(nextUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();
            
            Elements nextTable = nextDoc.select("table.players tbody tr");
            return !nextTable.isEmpty();
            
        } catch (IOException e) {
            return false;
        }
    }
    
    // Alternative method with configurable page limit for testing
    public List<Player> scrapePlayersWithLimit(int maxPages) {
        List<Player> playerList = new ArrayList<>();
        
        for (int page = 1; page <= maxPages; page++) {
            try {
                String url = page == 1 ? BASE_URL : BASE_URL + "?page=" + page;
                System.out.println("Scraping page " + page + ": " + url);
                
                Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();

                Elements table = doc.select("table.players");
                if (table.isEmpty()) {
                    System.out.println("No more pages found at page " + page);
                    break;
                }

                Elements rows = table.select("tbody tr");
                if (rows.isEmpty()) {
                    System.out.println("No player rows found on page " + page);
                    break;
                }

                for (Element row : rows) {
                    try {
                        Elements cells = row.select("th, td");
                        if (cells.size() >= 8) {
                            String position = cells.get(0).text();
                            String player = cells.get(1).text();
                            String teamName = cells.get(2).text();
                            String nationality = cells.get(3).text();
                            String height = cells.get(4).text();
                            String weight = cells.get(5).text();
                            String age = cells.get(6).text();
                            String overall = cells.get(7).text();
                            
                            playerList.add(new Player(position, player, teamName, nationality, height, weight, age, overall));
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing row on page " + page + ": " + e.getMessage());
                    }
                }

                System.out.println("Scraped " + rows.size() + " players from page " + page);
              Thread.sleep(2000); // Try reducing the sleep time to scrape fast. increase the scrape time if the website is blocking. 
                
            } catch (IOException e) {
                System.err.println("Error scraping page " + page + ": " + e.getMessage());
                break;
            } 
            catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Total players scraped: " + playerList.size());
        return playerList;
    }
}
