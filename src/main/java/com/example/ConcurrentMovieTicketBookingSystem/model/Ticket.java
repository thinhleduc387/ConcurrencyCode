package com.example.ConcurrentMovieTicketBookingSystem.model;

public class Ticket {
    private Movie movie;
    private Seat seat;

    public Ticket(Movie movie, Seat seat) {
        this.movie = movie;
        this.seat = seat;
    }
    public Movie getMovie() {
        return movie;
    }
    public Seat getSeat() {
        return seat;
    }
}
