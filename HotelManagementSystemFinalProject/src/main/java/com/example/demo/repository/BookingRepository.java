package com.example.demo.repository;

import com.example.demo.model.Booking;
import com.example.demo.model.BookingStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findTop5ByOrderByStartDateDesc();

    long countByStatus(BookingStatus status);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.status = 'CONFIRMED'
            AND b.startDate <= :today AND b.endDate > :today
            """)
    long countActiveBookings(@Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.status = 'CONFIRMED' AND b.startDate = :today
            """)
    long countCheckInsToday(@Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(b) FROM Booking b
            WHERE b.status = 'CONFIRMED' AND b.endDate = :today
            """)
    long countCheckOutsToday(@Param("today") LocalDate today);

    @Query("""
            SELECT COUNT(b) > 0 FROM Booking b
            WHERE b.room.id = :roomId
            AND b.status = 'CONFIRMED'
            AND b.id <> COALESCE(:excludeId, -1)
            AND b.startDate < :endDate
            AND b.endDate > :startDate
            """)
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") Long excludeId
    );

    @Query("""
            SELECT DISTINCT b FROM Booking b
            LEFT JOIN FETCH b.services
            WHERE b.id = :id
            """)
    Optional<Booking> findByIdWithServices(@Param("id") Long id);
}
