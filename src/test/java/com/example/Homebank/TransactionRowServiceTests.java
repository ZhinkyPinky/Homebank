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

    private final TransactionRowEntity mockEntity = new TransactionRowEntity(
            1L,
            1L,
            1,
            "",
            "Test transaction row",
            "This is a test entity",
            LocalDate.of(2000, 1, 1),
            1000,
            "Test transaction",
            "Test",
            LocalDateTime.now()
    );

    @Test
    public void testGetTransactionRowById_Success() {
        long transactionRowId = 1L;

        when(transactionRowRepository.findById(transactionRowId)).thenReturn(Optional.of(mockEntity));

        TransactionRowDTO result = transactionRowService.getTransactionRowById(transactionRowId);

        assertNotNull(result);
        assertEquals(transactionRowId, result.id());

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
