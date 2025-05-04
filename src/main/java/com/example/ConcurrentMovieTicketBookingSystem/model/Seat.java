package com.example.ConcurrentMovieTicketBookingSystem.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

public class Seat {
    private int seatNumber;
    private AtomicBoolean isAvailable;
    private final ReentrantLock lock;

    public Seat(int seatNumber) {
        this.seatNumber = seatNumber;
        this.isAvailable = new AtomicBoolean(true); // Mặc định ghế trống
        this.lock = new ReentrantLock(); // Khóa riêng cho từng ghế
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public boolean isAvailable() {
        return isAvailable.get();
    }

    public void reserve() {
        isAvailable.set(false); // Đánh dấu ghế là đã bán
    }

    public void setAvailable(boolean available) {
        isAvailable.set(available); // Cập nhật trạng thái ghế
    }

    public ReentrantLock getLock() {
        return lock;
    }
}