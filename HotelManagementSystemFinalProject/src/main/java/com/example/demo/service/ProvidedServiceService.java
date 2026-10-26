package com.example.demo.service;

import com.example.demo.model.ProvidedService;
import com.example.demo.repository.ProvidedServiceRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProvidedServiceService {

    private final ProvidedServiceRepository providedServiceRepository;

    public ProvidedServiceService(ProvidedServiceRepository providedServiceRepository) {
        this.providedServiceRepository = providedServiceRepository;
    }

    public ProvidedService saveProvidedService(ProvidedService providedService) {
        return providedServiceRepository.save(providedService);
    }

    public List<ProvidedService> getAllProvidedServices() {
        return providedServiceRepository.findAll();
    }

    public Optional<ProvidedService> getProvidedServiceById(Long id) {
        return providedServiceRepository.findById(id);
    }

    public void deleteProvidedService(Long id) {
        providedServiceRepository.deleteById(id);
    }
}
