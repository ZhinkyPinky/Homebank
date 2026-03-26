package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.SecurityContextUtility;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
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
class TransactionHeadServiceTests {

    @Mock
    private SecurityContextUtility securityContextUtility;

    @Mock
    private TransactionHeadRepository transactionHeadRepository;

    @InjectMocks
    private TransactionHeadService transactionHeadService;

    @Test
    void getTransactionHead_returnsMappedDtoWhenFound() {
        TransactionHeadView view = transactionHeadView(10, 1, 2);
        when(transactionHeadRepository.findById(10)).thenReturn(Optional.of(view));

        TransactionHeadDTO result = transactionHeadService.getTransactionHead(10);

        assertEquals(10, result.id());
        assertEquals(1, result.lenderId());
        assertEquals(2, result.borrowerId());
    }

    @Test
    void getTransactionHead_throwsResourceNotFoundWhenMissing() {
        when(transactionHeadRepository.findById(55)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionHeadService.getTransactionHead(55)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(55, exception.getResourceId());
        assertEquals("Transaction head could not be found.", exception.getMessage());
    }

    @Test
    void getTransactionHeadsByCustomerId_returnsMappedDtos() {
        when(transactionHeadRepository.findAllByLenderIdOrBorrowerId(3)).thenReturn(List.of(
                transactionHeadView(30, 3, 7),
                transactionHeadView(31, 8, 3)
        ));

        List<TransactionHeadDTO> result = transactionHeadService.getTransactionHeadsByCustomerId(3);

        assertEquals(2, result.size());
        assertEquals(30, result.get(0).id());
        assertEquals(31, result.get(1).id());
    }

    @Test
    void saveTransactionHead_delegatesToRepository() {
        TransactionHeadDTO dto = transactionHeadDto(12, 1, 2);
        Map<String, Object> expected = Map.of("p_OUT_Id", 12);

        when(transactionHeadRepository.saveTransactionHead(
                dto.id(),
                dto.lenderId(),
                dto.borrowerId(),
                dto.transactionName(),
                dto.description(),
                dto.startDate(),
                dto.prelEndDate(),
                dto.endDate(),
                dto.rowVersion()
        )).thenReturn(expected);

        transactionHeadService.saveTransactionHead(dto);

        verify(transactionHeadRepository).saveTransactionHead(
                dto.id(),
                dto.lenderId(),
                dto.borrowerId(),
                dto.transactionName(),
                dto.description(),
                dto.startDate(),
                dto.prelEndDate(),
                dto.endDate(),
                dto.rowVersion()
        );
    }

    @Test
    void deleteTransactionHead_rethrowsRepositoryException() {
        TransactionHeadDTO dto = transactionHeadDto(22, 1, 2);
        RuntimeException boom = new RuntimeException("delete failed");
        when(transactionHeadRepository.deleteTransactionHead(dto.id(), dto.rowVersion())).thenThrow(boom);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionHeadService.deleteTransactionHead(dto));

        assertSame(boom, exception);
        verify(transactionHeadRepository).deleteTransactionHead(dto.id(), dto.rowVersion());
    }

    private TransactionHeadView transactionHeadView(int id, int lenderId, int borrowerId) {
        return new TransactionHeadView(
                id,
                lenderId,
                borrowerId,
                "Head " + id,
                "desc",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                100,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private TransactionHeadDTO transactionHeadDto(int id, int lenderId, int borrowerId) {
        return new TransactionHeadDTO(
                id,
                lenderId,
                borrowerId,
                "Head " + id,
                "desc",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                100,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
