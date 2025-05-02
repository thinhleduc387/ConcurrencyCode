package com.example.ConcurrentMovieTicketBookingSystem.repository;

import com.example.ConcurrentMovieTicketBookingSystem.model.Seat;

import java.util.ArrayList;
import java.util.List;

public class SeatRepository {
    private List<Seat> seats = new ArrayList<>();
    public SeatRepository(int numberOfSeats) {
        for (int i = 1; i <= numberOfSeats; i++) {
            seats.add(new Seat(i));
        }
    }
    public List<Seat> getSeats() {
        return seats;

    }
    public Seat getSeat(int seatNumber) {
        return seats.get(seatNumber - 1); // Ghế số 1 có chỉ số 0 trong danh sách
    }
}
