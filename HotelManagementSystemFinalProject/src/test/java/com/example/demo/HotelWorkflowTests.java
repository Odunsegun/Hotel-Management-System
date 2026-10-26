package com.example.demo;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.service.BookingService;
import com.example.demo.exception.BookingValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("demo")
@AutoConfigureMockMvc
class HotelWorkflowTests {
    @Autowired BookingService bookings;
    @Autowired RoomRepository rooms;
    @Autowired CustomerRepository customers;
    @Autowired ProvidedServiceRepository services;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MockMvc mvc;

    Booking request(Room room, Customer customer, LocalDate start) {
        return new Booking(customer, room, start, start.plusDays(2));
    }

    @Test void pricesRemainStableAndCancellationReleasesRoom() {
        var tx = new TransactionTemplate(transactions);
        tx.executeWithoutResult(status -> {
            var room = rooms.save(new Room(901, "Test", new BigDecimal("100.00")));
            var customer = customers.findAll().get(0);
            var extra = services.save(new ProvidedService("Test service", "Test", new BigDecimal("10.00")));
            var input = request(room, customer, LocalDate.now().plusDays(20));
            input.setServices(List.of(extra));
            var booking = bookings.saveBooking(input);
            room.setPrice(new BigDecimal("500.00"));
            extra.setPrice(new BigDecimal("99.00"));
            rooms.saveAndFlush(room);
            services.saveAndFlush(extra);
            assertThat(booking.getGrandTotal()).isEqualByComparingTo("210.00");
            var edit = request(room, customer, input.getStartDate());
            edit.setId(booking.getId());
            edit.setServices(List.of(extra));
            assertThat(bookings.saveBooking(edit).getGrandTotal()).isEqualByComparingTo("210.00");
            bookings.cancelBooking(booking.getId());
            assertThat(bookings.saveBooking(request(room, customer, input.getStartDate())).getStatus())
                    .isEqualTo(BookingStatus.CONFIRMED);
            status.setRollbackOnly();
        });
    }

    @Test void simultaneousRequestsCannotDoubleBook() throws Exception {
        var room = rooms.saveAndFlush(new Room(902, "Concurrent", new BigDecimal("100.00")));
        var customer = customers.findAll().get(0);
        var start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<Boolean> reserve = () -> {
            start.await();
            try {
                bookings.saveBooking(request(room, customer, LocalDate.now().plusDays(30)));
                return true;
            } catch (BookingValidationException expected) { return false; }
        };
        try {
            var first = executor.submit(reserve);
            var second = executor.submit(reserve);
            start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally { executor.shutdownNow(); }
    }

    @Test void invalidInputsAndDuplicatesReturnUsefulMessages() throws Exception {
        mvc.perform(post("/customers/save").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "").param("email", "bad").param("phoneNumber", ""))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
        mvc.perform(post("/customers/save").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "Duplicate").param("email", "alice@email.com").param("phoneNumber", "123"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
        mvc.perform(post("/bookings/save").with(user("staff").roles("STAFF")).with(csrf())
                .param("customer.id", "1").param("room.id", "999999")
                .param("startDate", "2027-01-01").param("endDate", "2027-01-03"))
                .andExpect(status().is3xxRedirection()).andExpect(flash().attributeExists("errorMessage"));
    }

    @Test void staffCannotChangeCatalogAndPagesRender() throws Exception {
        mvc.perform(post("/rooms/save").with(user("staff").roles("STAFF")).with(csrf()))
                .andExpect(status().isForbidden());
        for (String path : List.of("/", "/bookings", "/customers", "/rooms", "/services", "/bookings/1/invoice")) {
            mvc.perform(get(path).with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
        }
        mvc.perform(post("/customers/save").with(user("admin").roles("ADMIN")).with(csrf())
                .param("name", "New guest").param("email", "new@example.com").param("phoneNumber", "123"))
                .andExpect(flash().attribute("successMessage", "Customer added successfully!"));
    }
}
