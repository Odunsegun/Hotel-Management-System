package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @ManyToMany
    @JoinTable(
            name = "booking_services",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<ProvidedService> services = new ArrayList<>();

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal nightlyRate;

    @jakarta.persistence.ElementCollection
    @jakarta.persistence.CollectionTable(name = "booking_service_prices", joinColumns = @JoinColumn(name = "booking_id"))
    @jakarta.persistence.MapKeyColumn(name = "service_id")
    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private java.util.Map<Long, BigDecimal> servicePrices = new java.util.HashMap<>();

    public BigDecimal getNightlyRate() { return nightlyRate; }
    public void setNightlyRate(BigDecimal rate) { nightlyRate = rate; }
    public java.util.Map<Long, BigDecimal> getServicePrices() { return servicePrices; }

    public Booking() {
    }

    public Booking(Customer customer, Room room, LocalDate startDate, LocalDate endDate) {
        this.customer = customer;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public List<ProvidedService> getServices() {
        return services;
    }

    public void setServices(List<ProvidedService> services) {
        this.services = services != null ? services : new ArrayList<>();
    }

    public long getNights() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public BigDecimal getRoomTotal() {
        if (nightlyRate == null) {
            return BigDecimal.ZERO;
        }
        return nightlyRate.multiply(BigDecimal.valueOf(getNights()));
    }

    public BigDecimal getServicesTotal() {
        if (services == null || services.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return servicePrices.values().stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotal() {
        return getRoomTotal().add(getServicesTotal());
    }

    @Override
    public String toString() {
        return "Booking [id=" + id + ", startDate=" + startDate + ", endDate=" + endDate + ", status=" + status + "]";
    }
}
