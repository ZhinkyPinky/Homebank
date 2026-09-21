package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.TransactionHeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionHeadRepository extends JpaRepository<TransactionHeadEntity, Integer> {}
