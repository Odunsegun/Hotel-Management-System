package com.example.demo.service;

import com.example.demo.dto.DashboardStats;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.RoomRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final RoomRepository roomRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    public DashboardService(
            RoomRepository roomRepository,
            CustomerRepository customerRepository,
            BookingRepository bookingRepository
    ) {
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
    }

    public DashboardStats getDashboardStats() {
        LocalDate today = LocalDate.now();
        return new DashboardStats(
                roomRepository.count(),
                customerRepository.count(),
                bookingRepository.countActiveBookings(today),
                bookingRepository.countCheckInsToday(today),
                bookingRepository.countCheckOutsToday(today),
                bookingRepository.findTop5ByOrderByStartDateDesc()
        );
    }
}
