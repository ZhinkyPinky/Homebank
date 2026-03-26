package com.example.Homebank.businessLogic.services;

import com.example.Homebank.dataAccess.repositories.TransactionRowRepository;
import com.example.Homebank.dataAccess.views.TransactionRowView;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.TransactionRowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionRowServiceTests {

    @Mock
    private TransactionRowRepository transactionRowRepository;

    @InjectMocks
    private TransactionRowService transactionRowService;

    @Test
    void getTransactionRowById_returnsMappedDtoWhenFound() {
        TransactionRowView view = transactionRowView(700, 70);
        when(transactionRowRepository.findById(700)).thenReturn(Optional.of(view));

        TransactionRowDTO result = transactionRowService.getTransactionRowById(700);

        assertEquals(700, result.id());
        assertEquals(70, result.transactionHeadId());
    }

    @Test
    void getTransactionRowById_throwsResourceNotFoundWhenMissing() {
        when(transactionRowRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionRowService.getTransactionRowById(999)
        );

        assertEquals("TRANSACTION_ROW", exception.getResourceType());
        assertEquals(999, exception.getResourceId());
        assertEquals("The transaction row could not be found.", exception.getMessage());
    }

    @Test
    void getAllByTransactionHeadId_returnsMappedDtos() {
        when(transactionRowRepository.findAllByTransactionHeadId(10)).thenReturn(List.of(
                transactionRowView(100, 10),
                transactionRowView(101, 10)
        ));

        List<TransactionRowDTO> result = transactionRowService.getAllByTransactionHeadId(10);

        assertEquals(2, result.size());
        assertEquals(100, result.get(0).id());
        assertEquals(101, result.get(1).id());
    }

    @Test
    void saveTransactionRow_delegatesToRepository() {
        TransactionRowDTO dto = transactionRowDto(80, 8);
        Map<String, Object> expected = Map.of("p_OUT_Id", 80);

        when(transactionRowRepository.saveTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.typeOfTransactionCode(),
                dto.name(),
                dto.description(),
                dto.paymentDate(),
                dto.amount(),
                dto.rowVersion()
        )).thenReturn(expected);

        transactionRowService.saveTransactionRow(dto);

        verify(transactionRowRepository).saveTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.typeOfTransactionCode(),
                dto.name(),
                dto.description(),
                dto.paymentDate(),
                dto.amount(),
                dto.rowVersion()
        );
    }

    @Test
    void deleteTransactionRow_rethrowsRepositoryException() {
        TransactionRowDTO dto = transactionRowDto(90, 9);
        RuntimeException boom = new RuntimeException("delete failed");
        when(transactionRowRepository.deleteTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.rowVersion()
        )).thenThrow(boom);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionRowService.deleteTransactionRow(dto));

        assertSame(boom, exception);
        verify(transactionRowRepository).deleteTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.rowVersion()
        );
    }

    private TransactionRowView transactionRowView(int id, int transactionHeadId) {
        return new TransactionRowView(
                id,
                transactionHeadId,
                1,
                "DEBIT",
                "Row " + id,
                "desc",
                LocalDate.parse("2026-01-15"),
                50,
                "Head",
                "Debit",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private TransactionRowDTO transactionRowDto(int id, int transactionHeadId) {
        return new TransactionRowDTO(
                id,
                transactionHeadId,
                1,
                "DEBIT",
                "Row " + id,
                "desc",
                LocalDate.parse("2026-01-15"),
                50,
                "Head",
                "Debit",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
