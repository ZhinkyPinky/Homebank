package com.example.Homebank;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.dataAccess.entities.TransactionRowEntity;
import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import com.example.Homebank.presentation.dto.TransactionRowDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionRowServiceTests {
    @Mock
    private TransactionRowRepository transactionRowRepository;

    @InjectMocks
    private TransactionRowService transactionRowService;

    @Test
    public void testGetTransactionRowById_Success() {
        long transactionRowId = 1L;
        TransactionRowEntity mockEntity = new TransactionRowEntity();
        mockEntity.setId(transactionRowId);
        mockEntity.setTransactionHeadId(100L);
        mockEntity.setTransactionRowNo(1);
        mockEntity.setTypeOfTransactionCode("CODE123");
        mockEntity.setName("Test Transaction");
        mockEntity.setDescription("Test Description");
        mockEntity.setPaymentDate(LocalDate.of(2023, 10, 1));
        mockEntity.setAmount(1000);
        mockEntity.setTransactionName("Test Transaction Name");
        mockEntity.setTypeOfTransaction("Test Type");
        mockEntity.setRowVersion(LocalDateTime.now());

        when(transactionRowRepository.findById(transactionRowId)).thenReturn(Optional.of(mockEntity));

        TransactionRowDTO result = transactionRowService.getTransactionRowById(transactionRowId);

        assertNotNull(result);
        assertEquals(transactionRowId, result.id());
        assertEquals("Test Transaction", result.name());
        assertEquals(1000, result.amount());

        verify(transactionRowRepository, times(1)).findById(transactionRowId);
    }

    @Test
    public void testGetTransactionRowById_NotFound() {
        long transactionRowId = 999L;

        when(transactionRowRepository.findById(transactionRowId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> transactionRowService.getTransactionRowById(transactionRowId));

        assertEquals("The transaction row could not be found.", exception.getMessage());

        verify(transactionRowRepository, times(1)).findById(transactionRowId);
    }
}
