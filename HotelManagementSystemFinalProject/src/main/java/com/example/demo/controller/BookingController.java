package com.example.demo.controller;

import com.example.demo.exception.BookingValidationException;
import com.example.demo.model.Booking;
import com.example.demo.model.ProvidedService;
import com.example.demo.service.BookingService;
import com.example.demo.service.CustomerService;
import com.example.demo.service.ProvidedServiceService;
import com.example.demo.service.RoomService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    @org.springframework.web.bind.annotation.InitBinder
    public void configureBinding(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("id", "customer.id", "room.id", "startDate", "endDate");
    }


    private final BookingService bookingService;
    private final CustomerService customerService;
    private final RoomService roomService;
    private final ProvidedServiceService providedServiceService;

    public BookingController(
            BookingService bookingService,
            CustomerService customerService,
            RoomService roomService,
            ProvidedServiceService providedServiceService
    ) {
        this.bookingService = bookingService;
        this.customerService = customerService;
        this.roomService = roomService;
        this.providedServiceService = providedServiceService;
    }

    @GetMapping
    public String listBookings(Model model) {
        Booking booking = model.containsAttribute("booking")
                ? (Booking) model.getAttribute("booking")
                : new Booking();
        populateBookingPage(model, booking);
        return "bookings";
    }

    @GetMapping("/edit/{id}")
    public String editBooking(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookingService.getBookingById(id)
                .map(booking -> {
                    populateBookingPage(model, booking);
                    return "bookings";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
                    return "redirect:/bookings";
                });
    }

    @PostMapping("/save")
    public String saveBooking(
            @jakarta.validation.Valid @ModelAttribute Booking booking,
            org.springframework.validation.BindingResult errors,
            @RequestParam(value = "serviceIds", required = false) Long[] serviceIds,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please enter a valid customer, room and dates.");
            redirectAttributes.addFlashAttribute("booking", booking);
            return "redirect:/bookings";
        }
        boolean isNew = booking.getId() == null;
        try {
            if (serviceIds != null && serviceIds.length > 0) {
                List<ProvidedService> selectedServices = new ArrayList<>();
                for (Long serviceId : serviceIds) {
                    ProvidedService service = new ProvidedService();
                    service.setId(serviceId);
                    selectedServices.add(service);
                }
                booking.setServices(selectedServices);
            } else {
                booking.setServices(new ArrayList<>());
            }
            bookingService.saveBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage",
                    isNew ? "Booking created successfully!" : "Booking updated successfully!");
            return "redirect:/bookings";
        } catch (BookingValidationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("booking", booking);
            return "redirect:/bookings";
        }
    }

    @PostMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (BookingValidationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/{id}/invoice")
    public String invoice(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return bookingService.getBookingById(id)
                .map(booking -> {
                    model.addAttribute("booking", booking);
                    return "invoice";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Booking not found.");
                    return "redirect:/bookings";
                });
    }

    private void populateBookingPage(Model model, Booking booking) {
        model.addAttribute("bookings", bookingService.getAllBookings());
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("rooms", roomService.getAllRooms());
        model.addAttribute("allServices", providedServiceService.getAllProvidedServices());
        model.addAttribute("booking", booking);
        model.addAttribute("selectedServiceIds",
                booking.getServices() != null && !booking.getServices().isEmpty()
                        ? booking.getServices().stream().map(ProvidedService::getId).toList()
                        : List.of());
    }
}
