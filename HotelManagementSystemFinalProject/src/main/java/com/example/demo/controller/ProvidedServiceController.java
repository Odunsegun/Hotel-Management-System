package com.example.demo.controller;

import com.example.demo.model.ProvidedService;
import com.example.demo.service.ProvidedServiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/services")
public class ProvidedServiceController {
    @org.springframework.web.bind.annotation.InitBinder
    public void configureBinding(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("id", "name", "description", "price");
    }


    private final ProvidedServiceService providedServiceService;

    public ProvidedServiceController(ProvidedServiceService providedServiceService) {
        this.providedServiceService = providedServiceService;
    }

    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("services", providedServiceService.getAllProvidedServices());
        if (!model.containsAttribute("providedService")) {
            model.addAttribute("providedService", new ProvidedService());
        }
        return "services";
    }

    @GetMapping("/edit/{id}")
    public String editService(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return providedServiceService.getProvidedServiceById(id)
                .map(service -> {
                    model.addAttribute("services", providedServiceService.getAllProvidedServices());
                    model.addAttribute("providedService", service);
                    return "services";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Service not found.");
                    return "redirect:/services";
                });
    }

    @PostMapping("/save")
    public String saveProvidedService(
            @jakarta.validation.Valid @ModelAttribute("providedService") ProvidedService providedService, org.springframework.validation.BindingResult errors,
            RedirectAttributes redirectAttributes
    ) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", errors.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("providedService", providedService);
            return "redirect:/services";
        }
        boolean isNew = providedService.getId() == null;
        try {
            providedServiceService.saveProvidedService(providedService);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to save: check for duplicate email or room number and related records.");
            redirectAttributes.addFlashAttribute("providedService", providedService);
            return "redirect:/services";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Service added successfully!" : "Service updated successfully!");
        return "redirect:/services";
    }

    @PostMapping("/delete/{id}")
    public String deleteProvidedService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            providedServiceService.deleteProvidedService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Service deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete service linked to bookings.");
        }
        return "redirect:/services";
    }
}
