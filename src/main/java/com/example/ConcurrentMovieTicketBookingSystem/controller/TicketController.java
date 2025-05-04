package com.example.ConcurrentMovieTicketBookingSystem.controller;

import com.example.ConcurrentMovieTicketBookingSystem.model.Movie;
import com.example.ConcurrentMovieTicketBookingSystem.model.Seat;
import com.example.ConcurrentMovieTicketBookingSystem.repository.SeatRepository;
import com.example.ConcurrentMovieTicketBookingSystem.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
public class TicketController {
    private final SeatRepository seatRepo = new SeatRepository(10); // Giả sử có 10 ghế
    private final TicketService ticketService = new TicketService();
    private final Movie movie = new Movie("Avengers", "Action", 100);

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("seats", seatRepo.getSeats());
        model.addAttribute("movie", movie);
        model.addAttribute("error", false); // Khởi tạo error để tránh null
        return "index";
    }

    @PostMapping("/reserve")
    public String reserveSeat(@RequestParam("seatNumber") int seatNumber, Model model) {
        Seat seat = seatRepo.getSeat(seatNumber);
        boolean success = ticketService.reserveSeat(seat);
        if (success) {
            model.addAttribute("message", "Đặt vé thành công cho ghế số " + seatNumber);
            model.addAttribute("error", false);
        } else {
            model.addAttribute("message", "Ghế số " + seatNumber + " đã được đặt.");
            model.addAttribute("error", true);
        }
        model.addAttribute("seats", seatRepo.getSeats());
        model.addAttribute("movie", movie);
        return "index";
    }

    @PostMapping("/cancel")
    public String cancelReservation(@RequestParam("seatNumber") int seatNumber, Model model) {
        Seat seat = seatRepo.getSeat(seatNumber);
        boolean success = ticketService.cancelReservation(seat);
        if (success) {
            model.addAttribute("message", "Hủy vé thành công cho ghế số " + seatNumber);
            model.addAttribute("error", false);
        } else {
            model.addAttribute("message", "Không thể hủy vé cho ghế số " + seatNumber + ". Ghế đã trống.");
            model.addAttribute("error", true);
        }
        model.addAttribute("seats", seatRepo.getSeats());
        model.addAttribute("movie", movie);
        return "index";
    }

    @GetMapping("/seats")
    @ResponseBody
    public Collection<Seat> getSeats() {
        return seatRepo.getSeats();
    }
}