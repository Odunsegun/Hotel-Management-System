package com.example.demo.service;

import com.example.demo.exception.BookingValidationException;
import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import com.example.demo.model.ProvidedService;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ProvidedServiceRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ProvidedServiceRepository providedServiceRepository;

    private final com.example.demo.repository.RoomRepository roomRepository;
    private final com.example.demo.repository.CustomerRepository customerRepository;

    public BookingService(BookingRepository bookingRepository, ProvidedServiceRepository providedServiceRepository,
            com.example.demo.repository.RoomRepository roomRepository,
            com.example.demo.repository.CustomerRepository customerRepository) {
        this.roomRepository = roomRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
        this.providedServiceRepository = providedServiceRepository;
    }

    @Transactional
    public Booking saveBooking(Booking booking) {
        if (booking.getRoom() == null || booking.getRoom().getId() == null
                || booking.getCustomer() == null || booking.getCustomer().getId() == null) {
            throw new BookingValidationException("Please select a customer and room.");
        }
        // Serialize availability checks and saves on the room row, even when no bookings exist.
        var room = roomRepository.findByIdForUpdate(booking.getRoom().getId())
                .orElseThrow(() -> new BookingValidationException("Room not found."));
        var customer = customerRepository.findById(booking.getCustomer().getId())
                .orElseThrow(() -> new BookingValidationException("Customer not found."));
        Booking saved = booking.getId() == null ? new Booking() : bookingRepository.findById(booking.getId())
                .orElseThrow(() -> new BookingValidationException("Booking not found."));
        boolean roomChanged = saved.getRoom() == null || !saved.getRoom().getId().equals(room.getId());
        booking.setStatus(saved.getStatus());
        validateBooking(booking);
        List<Long> ids = booking.getServices().stream().map(ProvidedService::getId)
                .filter(java.util.Objects::nonNull).distinct().toList();
        var services = providedServiceRepository.findAllById(ids);
        if (services.size() != ids.size()) {
            throw new BookingValidationException("One or more selected services no longer exist.");
        }
        var prices = new java.util.HashMap<Long, java.math.BigDecimal>();
        for (var service : services) {
            prices.put(service.getId(), saved.getServicePrices().getOrDefault(service.getId(), service.getPrice()));
        }
        if (roomChanged) saved.setNightlyRate(room.getPrice());
        saved.setCustomer(customer);
        saved.setRoom(room);
        saved.setStartDate(booking.getStartDate());
        saved.setEndDate(booking.getEndDate());
        saved.setServices(new ArrayList<>(services));
        saved.getServicePrices().clear();
        saved.getServicePrices().putAll(prices);
        return bookingRepository.save(saved);
    }

    @Transactional(readOnly = true)
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findByIdWithServices(id);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingValidationException("Booking not found."));
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public void validateBooking(Booking booking) {
        if (booking.getCustomer() == null || booking.getCustomer().getId() == null) {
            throw new BookingValidationException("Please select a customer.");
        }
        if (booking.getRoom() == null || booking.getRoom().getId() == null) {
            throw new BookingValidationException("Please select a room.");
        }
        if (booking.getStartDate() == null || booking.getEndDate() == null) {
            throw new BookingValidationException("Please provide check-in and check-out dates.");
        }
        if (!booking.getEndDate().isAfter(booking.getStartDate())) {
            throw new BookingValidationException("Check-out date must be after check-in date.");
        }
        if (booking.getStatus() != BookingStatus.CANCELLED
                && bookingRepository.existsOverlappingBooking(
                booking.getRoom().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getId()
        )) {
            throw new BookingValidationException("This room is already booked for the selected dates.");
        }
    }
}
