package com.example.ConcurrentMovieTicketBookingSystem.model;

public class Movie {
    private String title;
    private String genre;
    private double ticketPrice;

    // Constructor, getters and setters
    public Movie(String title, String genre, double ticketPrice) {
        this.title = title;
        this.genre = genre;
        this.ticketPrice = ticketPrice;
    }
    public String getTitle() {
        return title;
    }
    public String getGenre() {
        return genre;
    }
    public double getTicketPrice() {
        return ticketPrice;
    }
}
