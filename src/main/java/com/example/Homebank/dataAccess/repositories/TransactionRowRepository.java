package com.example.Homebank.dataAccess.repositories;

import com.example.Homebank.dataAccess.views.TransactionRowView;
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
public interface TransactionRowRepository extends JpaRepository<TransactionRowView, Integer> {

    @Query(nativeQuery = true, value = "SELECT * FROM bank.vTransactionRow where TransactionHead_Id = :transactionHeadId ORDER BY TransactionRowNo")
    List<TransactionRowView> findAllByTransactionHeadId(int transactionHeadId);

    @Procedure(name = "SaveTransactionRow")
    Map<String, Object> saveTransactionRow(
            @Param("p_Id") int id,
            @Param("p_TransactionHead_Id") int transactionHeadId,
            @Param("p_TransactionRowNo") int transactionRowNo,
            @Param("p_TypeOfTransaction_Code") String typeOfTransactionCode,
            @Param("p_Name") String name,
            @Param("p_Description") String description,
            @Param("p_PaymentDate") LocalDate paymentDate,
            @Param("p_Amount") int amount,
            @Param("p_RowVersion") LocalDateTime rowVersion);

    @Procedure(name = "DeleteTransactionRow")
    Map<String, Object> deleteTransactionRow(
            @Param("p_Id") int id,
            @Param("p_TransactionHead_Id") int transactionHeadId,
            @Param("p_TransactionRowNo") int transactionRowNo,
            @Param("p_RowVersion") LocalDateTime rowVersion);
}
