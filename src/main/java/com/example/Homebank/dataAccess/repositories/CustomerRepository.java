package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.views.CustomerView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerView, Long> {
}
