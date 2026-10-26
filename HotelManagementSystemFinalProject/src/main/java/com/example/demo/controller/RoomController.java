package com.example.demo.controller;

import com.example.demo.model.Room;
import com.example.demo.service.RoomService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rooms")
public class RoomController {
    @org.springframework.web.bind.annotation.InitBinder
    public void configureBinding(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("id", "roomNumber", "type", "price");
    }


    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        if (!model.containsAttribute("room")) {
            model.addAttribute("room", new Room());
        }
        return "rooms";
    }

    @GetMapping("/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return roomService.getRoomById(id)
                .map(room -> {
                    model.addAttribute("rooms", roomService.getAllRooms());
                    model.addAttribute("room", room);
                    return "rooms";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Room not found.");
                    return "redirect:/rooms";
                });
    }

    @PostMapping("/save")
    public String saveRoom(@jakarta.validation.Valid @ModelAttribute Room room, org.springframework.validation.BindingResult errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", errors.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("room", room);
            return "redirect:/rooms";
        }
        boolean isNew = room.getId() == null;
        try {
            roomService.saveRoom(room);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to save: check for duplicate email or room number and related records.");
            redirectAttributes.addFlashAttribute("room", room);
            return "redirect:/rooms";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Room added successfully!" : "Room updated successfully!");
        return "redirect:/rooms";
    }

    @PostMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete room with existing bookings.");
        }
        return "redirect:/rooms";
    }
}
