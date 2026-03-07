package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.views.CustomerView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerViewRepository extends JpaRepository<CustomerView, Integer> {
}
