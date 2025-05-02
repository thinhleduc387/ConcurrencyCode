package com.example.ConcurrentMovieTicketBookingSystem.model;

import java.util.concurrent.atomic.AtomicBoolean;

public class Seat {
    private int seatNumber;
    private AtomicBoolean isAvailable;
    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.isAvailable = new AtomicBoolean(true); // Mặc định ghế trống
    }
    public int getSeatNumber() {
        return seatNumber;
    }
    public boolean isAvailable() {
        return isAvailable.get();
    }
    public void reserve() {
        isAvailable.set(false);
    }
}

