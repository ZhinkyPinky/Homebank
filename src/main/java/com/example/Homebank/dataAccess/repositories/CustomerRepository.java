package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Integer> {
    @Query("SELECT DISTINCT c.id FROM CustomerEntity c LEFT JOIN c.userCustomers uc " +
            "WHERE c.owner.id = :userId OR uc.user.id = :userId")
    List<Integer> findAccessibleCustomerIds(@Param("userId") int userId);
}
