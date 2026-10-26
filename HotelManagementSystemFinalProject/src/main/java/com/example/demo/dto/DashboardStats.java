package com.example.demo.dto;

import com.example.demo.model.Booking;
import java.util.List;

public class DashboardStats {

    private final long roomCount;
    private final long customerCount;
    private final long activeBookings;
    private final long checkInsToday;
    private final long checkOutsToday;
    private final List<Booking> recentBookings;

    public DashboardStats(
            long roomCount,
            long customerCount,
            long activeBookings,
            long checkInsToday,
            long checkOutsToday,
            List<Booking> recentBookings
    ) {
        this.roomCount = roomCount;
        this.customerCount = customerCount;
        this.activeBookings = activeBookings;
        this.checkInsToday = checkInsToday;
        this.checkOutsToday = checkOutsToday;
        this.recentBookings = recentBookings;
    }

    public long getRoomCount() {
        return roomCount;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public long getActiveBookings() {
        return activeBookings;
    }

    public long getCheckInsToday() {
        return checkInsToday;
    }

    public long getCheckOutsToday() {
        return checkOutsToday;
    }

    public List<Booking> getRecentBookings() {
        return recentBookings;
    }
}
