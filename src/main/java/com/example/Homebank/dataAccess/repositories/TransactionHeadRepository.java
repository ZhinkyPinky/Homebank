package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.entities.TransactionHeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Repository
public interface TransactionHeadRepository extends JpaRepository<TransactionHeadEntity, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM bank.vTransactionHead WHERE Lender_Id = :customerId OR Borrower_Id = :customerId")
    List<TransactionHeadEntity> findAllByLenderIdOrBorrowerId(long customerId);

    @Procedure(name = "SaveTransactionHead")
    Map<String, Object> saveTransactionHead(
            @Param("p_Id") Long id,
            @Param("p_Lender_Id") Long lenderId,
            @Param("p_Borrower_Id") Long borrowerId,
            @Param("p_TransactionName") String transactionName,
            @Param("p_Description") String description,
            @Param("p_StartDate") LocalDate startDate,
            @Param("p_PrelEndDate") LocalDate prelEndDate,
            @Param("p_EndDate") LocalDate endDate,
            @Param("p_RowVersion") LocalDateTime rowVersion
    );


    @Procedure(name = "DeleteTransactionHead")
    Map<String, Object> deleteTransactionHead(
            @Param("p_Id") Long id,
            @Param("p_RowVersion") LocalDateTime rowVersion
    );
}
