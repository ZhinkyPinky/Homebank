package com.example.Homebank;

import com.example.Homebank.dataAccess.entities.TransactionRowEntity;
import com.example.Homebank.presentation.dto.TransactionRowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CustomerDTOTests {

    @Test
    public void testFromEntity_Success() {
        final long id = 1L;
        final long transactionHeadId = 100L;
        final int transactionRowNo = 1;
        final String typeOfTransactionCode = "CODE123";
        final String name = "Test";
        final String description = "Test Description";
        final LocalDate paymentDate = LocalDate.of(2023, 10, 1);
        final int amount = 1000;
        final String transactionName = "Test Transaction Name";
        final String typeOfTransaction = "Test Type";
        final LocalDateTime rowVersion = LocalDateTime.now();

        final TransactionRowEntity mockEntity = new TransactionRowEntity();
        mockEntity.setId(id);
        mockEntity.setTransactionHeadId(transactionHeadId);
        mockEntity.setTransactionRowNo(transactionRowNo);
        mockEntity.setTypeOfTransactionCode(typeOfTransactionCode);
        mockEntity.setName(name);
        mockEntity.setDescription(description);
        mockEntity.setPaymentDate(paymentDate);
        mockEntity.setAmount(amount);
        mockEntity.setTransactionName(transactionName);
        mockEntity.setTypeOfTransaction(typeOfTransaction);
        mockEntity.setRowVersion(rowVersion);

        TransactionRowDTO result = TransactionRowDTO.fromEntity(mockEntity);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals(transactionHeadId, result.transactionHeadId());
        assertEquals(transactionRowNo, result.transactionRowNo());
        assertEquals(typeOfTransactionCode, result.typeOfTransactionCode());
        assertEquals(name, result.name());
        assertEquals(description, result.description());
        assertEquals(paymentDate, result.paymentDate());
        assertEquals(amount, result.amount());
        assertEquals(transactionName, result.transactionName());
        assertEquals(typeOfTransaction, result.typeOfTransaction());
        assertEquals(rowVersion, result.rowVersion());
    }
}
