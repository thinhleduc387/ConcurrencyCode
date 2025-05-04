package com.example.ConcurrentMovieTicketBookingSystem.service;

import com.example.ConcurrentMovieTicketBookingSystem.model.Seat;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class TicketService {
    private final ReentrantLock lock = new ReentrantLock();

    public boolean reserveSeat(Seat seat) {
        try {
            if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                return false; // Timeout, không thể khóa
            }
            try {
                if (seat == null || !seat.isAvailable()) {
                    return false;
                }
                seat.reserve(); // Đánh dấu ghế là đã bán
                return true;
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean cancelReservation(Seat seat) {
        try {
            if (!lock.tryLock(2, TimeUnit.SECONDS)) {
                return false; // Timeout, không thể khóa
            }
            try {
                if (seat == null || seat.isAvailable()) {
                    return false;
                }
                seat.setAvailable(true); // Đánh dấu ghế là trống
                return true;
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    // Phương thức mới: Đặt nhiều ghế cùng lúc
    public boolean reserveMultipleSeats(Seat seat1, Seat seat2) {
        // Sử dụng thứ tự khóa cố định để tránh deadlock
        ReentrantLock lock1 = seat1.getLock();
        ReentrantLock lock2 = seat2.getLock();

        // Khóa theo thứ tự cố định dựa trên seatNumber
        ReentrantLock firstLock = seat1.getSeatNumber() < seat2.getSeatNumber() ? lock1 : lock2;
        ReentrantLock secondLock = seat1.getSeatNumber() < seat2.getSeatNumber() ? lock2 : lock1;

        try {
            if (!firstLock.tryLock(2, TimeUnit.SECONDS)) {
                return false;
            }
            try {
                if (!secondLock.tryLock(2, TimeUnit.SECONDS)) {
                    return false;
                }
                try {
                    if (seat1.isAvailable() && seat2.isAvailable()) {
                        seat1.reserve();
                        seat2.reserve();
                        return true;
                    }
                    return false;
                } finally {
                    secondLock.unlock();
                }
            } finally {
                firstLock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}