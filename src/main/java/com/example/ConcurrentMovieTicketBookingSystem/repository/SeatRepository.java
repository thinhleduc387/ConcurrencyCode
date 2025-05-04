package com.example.ConcurrentMovieTicketBookingSystem.repository;

import com.example.ConcurrentMovieTicketBookingSystem.model.Seat;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class SeatRepository {
    private ConcurrentHashMap<Integer, Seat> seats = new ConcurrentHashMap<>();

    public SeatRepository(int numberOfSeats) {
        for (int i = 1; i <= numberOfSeats; i++) {
            seats.put(i, new Seat(i));
        }
    }

    public Seat getSeat(int seatNumber) {
        return seats.get(seatNumber);
    }

    public Collection<Seat> getSeats() {
        return seats.values();
    }
}