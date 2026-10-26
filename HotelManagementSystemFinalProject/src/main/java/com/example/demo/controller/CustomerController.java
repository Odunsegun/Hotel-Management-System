package com.example.demo.controller;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    @org.springframework.web.bind.annotation.InitBinder
    public void configureBinding(org.springframework.web.bind.WebDataBinder binder) {
        binder.setAllowedFields("id", "name", "email", "phoneNumber");
    }


    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new Customer());
        }
        return "customers";
    }

    @GetMapping("/edit/{id}")
    public String editCustomer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return customerService.getCustomerById(id)
                .map(customer -> {
                    model.addAttribute("customers", customerService.getAllCustomers());
                    model.addAttribute("customer", customer);
                    return "customers";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Customer not found.");
                    return "redirect:/customers";
                });
    }

    @PostMapping("/save")
    public String saveCustomer(@jakarta.validation.Valid @ModelAttribute Customer customer, org.springframework.validation.BindingResult errors, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", errors.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("customer", customer);
            return "redirect:/customers";
        }
        boolean isNew = customer.getId() == null;
        try {
            customerService.saveCustomer(customer);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to save: check for duplicate email or room number and related records.");
            redirectAttributes.addFlashAttribute("customer", customer);
            return "redirect:/customers";
        }
        redirectAttributes.addFlashAttribute("successMessage",
                isNew ? "Customer added successfully!" : "Customer updated successfully!");
        return "redirect:/customers";
    }

    @PostMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Customer deleted successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete customer with existing bookings.");
        }
        return "redirect:/customers";
    }
}
