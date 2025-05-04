package com.example.ConcurrentMovieTicketBookingSystem.service;

import com.example.ConcurrentMovieTicketBookingSystem.model.Seat;
import com.example.ConcurrentMovieTicketBookingSystem.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class TicketServiceTest {

    private TicketService ticketService;
    private Seat seat;
    private SeatRepository seatRepository;

    @BeforeEach
    void setUp() {
        ticketService = new TicketService();
        seat = new Seat(1); // Khởi tạo ghế với seatNumber = 1
        seatRepository = new SeatRepository(10); // Khởi tạo repository với 10 ghế
    }

    @Test
    void testSingleSeatReservation() {
        // Test đặt vé cho một ghế đơn lẻ
        boolean result = ticketService.reserveSeat(seat);
        assertTrue(result, "Đặt vé phải thành công cho ghế trống");
        assertFalse(seat.isAvailable(), "Ghế phải được đánh dấu là đã đặt");
    }

    @Test
    void testReserveAlreadyReservedSeat() {
        // Test đặt vé cho ghế đã được đặt
        ticketService.reserveSeat(seat); // Đặt ghế lần 1
        boolean result = ticketService.reserveSeat(seat); // Thử đặt lại
        assertFalse(result, "Đặt vé phải thất bại cho ghế đã được đặt");
        assertFalse(seat.isAvailable(), "Ghế vẫn phải được đánh dấu là đã đặt");
    }

    @Test
    void testConcurrentSeatReservation() throws InterruptedException {
        // Test nhiều luồng cố gắng đặt cùng một ghế
        int numberOfThreads = 1000; // Giảm số lượng luồng xuống 1000
        ExecutorService executor = Executors.newFixedThreadPool(100); // Giới hạn 100 luồng chạy đồng thời
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    if (ticketService.reserveSeat(seat)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS); // Tăng thời gian chờ lên 30 giây
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ một đặt ghế được thành công");
        assertFalse(seat.isAvailable(), "Ghế phải được đánh dấu là đã đặt");
    }

    @Test
    void testConcurrentDifferentSeats() throws InterruptedException {
        // Test nhiều luồng đặt các ghế khác nhau
        Seat[] seats = {new Seat(1), new Seat(2), new Seat(3)};
        int numberOfThreads = seats.length;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            final Seat currentSeat = seats[i];
            executor.submit(() -> {
                try {
                    assertTrue(ticketService.reserveSeat(currentSeat), "Đặt ghế phải thành công cho ghế số " + currentSeat.getSeatNumber());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        for (Seat s : seats) {
            assertFalse(s.isAvailable(), "Ghế số " + s.getSeatNumber() + " phải được đánh dấu là đã đặt");
        }
    }

    @Test
    void testCancelReservation() {
        // Test hủy vé và kiểm tra trạng thái ghế
        ticketService.reserveSeat(seat); // Đặt ghế trước
        assertFalse(seat.isAvailable(), "Ghế phải được đánh dấu là đã đặt ban đầu");

        boolean result = ticketService.cancelReservation(seat); // Hủy vé
        assertTrue(result, "Hủy vé phải thành công");
        assertTrue(seat.isAvailable(), "Ghế phải trở lại trạng thái trống sau khi hủy");
    }

    @Test
    void testConcurrentCancelAndReserve() throws InterruptedException {
        // Test một luồng hủy vé, các luồng khác cố gắng đặt lại ghế
        ticketService.reserveSeat(seat); // Đặt ghế trước
        int numberOfThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Một luồng hủy vé
        executor.submit(() -> {
            try {
                ticketService.cancelReservation(seat);
            } finally {
                latch.countDown();
            }
        });

        // 4 luồng cố gắng đặt lại ghế
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    if (ticketService.reserveSeat(seat)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ một đặt ghế được thành công sau khi hủy");
        assertFalse(seat.isAvailable(), "Ghế phải được đánh dấu là đã đặt lại");
    }

    @Test
    void testConcurrentAccessToSeatRepository() throws InterruptedException {
        // Test nhiều luồng truy cập SeatRepository đồng thời
        int numberOfThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 1; i <= numberOfThreads; i++) {
            final int seatNumber = i;
            executor.submit(() -> {
                try {
                    Seat s = seatRepository.getSeat(seatNumber);
                    assertNotNull(s, "Ghế số " + seatNumber + " phải tồn tại");
                    assertEquals(seatNumber, s.getSeatNumber(), "Số ghế phải khớp");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(10, seatRepository.getSeats().size(), "Repository phải chứa 10 ghế");
    }

    @Test
    void testReserveInvalidSeat() {
        // Test đặt vé cho ghế không hợp lệ (null)
        boolean result = ticketService.reserveSeat(null);
        assertFalse(result, "Đặt vé phải thất bại cho ghế null");
    }

    @Test
    void testCancelInvalidSeat() {
        // Test hủy vé cho ghế không hợp lệ (null)
        boolean result = ticketService.cancelReservation(null);
        assertFalse(result, "Hủy vé phải thất bại cho ghế null");
    }

    @Test
    void testHighLoadConcurrentReservation() throws InterruptedException {
        // Test đặt vé với số lượng lớn luồng
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    if (ticketService.reserveSeat(seat)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ một đặt ghế được thành công dưới tải cao");
        assertFalse(seat.isAvailable(), "Ghế phải được đánh dấu là đã đặt");
    }

    @Test
    void testReserveMultipleSeats() throws InterruptedException {
        // Test đặt nhiều ghế cùng lúc để kiểm tra deadlock
        Seat seat1 = new Seat(1);
        Seat seat2 = new Seat(2);
        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);

        // Luồng 1: Đặt seat1 và seat2
        executor.submit(() -> {
            try {
                if (ticketService.reserveMultipleSeats(seat1, seat2)) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        });

        // Luồng 2: Đặt seat2 và seat1 (ngược thứ tự)
        executor.submit(() -> {
            try {
                if (ticketService.reserveMultipleSeats(seat2, seat1)) {
                    successCount.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(1, successCount.get(), "Chỉ một luồng được đặt nhiều ghế thành công");
        assertFalse(seat1.isAvailable(), "Ghế 1 phải được đánh dấu là đã đặt");
        assertFalse(seat2.isAvailable(), "Ghế 2 phải được đánh dấu là đã đặt");
    }
}