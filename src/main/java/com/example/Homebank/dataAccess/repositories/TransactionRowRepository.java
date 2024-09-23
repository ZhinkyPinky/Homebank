package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.TransactionRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRowRepository extends JpaRepository<TransactionRow, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM bank.vTransactionRow where TransactionHead_Id = :transactionHeadId")
    List<TransactionRow> findAllByTransactionHeadId(long transactionHeadId);
}
