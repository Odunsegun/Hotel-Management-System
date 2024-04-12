package com.example.demo.repository;

import com.example.demo.model.ProvidedService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvidedServiceRepository extends JpaRepository<ProvidedService, Long> {
}
