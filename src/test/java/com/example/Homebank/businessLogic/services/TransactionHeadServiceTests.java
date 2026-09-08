package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.authorization.CustomerAccessPolicy;
import com.example.Homebank.businessLogic.security.authorization.TransactionHeadAccessPolicy;
import com.example.Homebank.dataAccess.repositories.TransactionHeadRepository;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionHeadServiceTests {

    @Mock
    private CustomerAccessPolicy customerAccessPolicy;

    @Mock
    private TransactionHeadAccessPolicy transactionHeadAccessPolicy;

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
        verify(transactionHeadAccessPolicy).requireReadAccess(result);
    }

    @Test
    void getTransactionHead_throwsAccessDeniedWhenNeitherCustomerIsAccessible() {
        TransactionHeadView view = transactionHeadView(10, 1, 2);
        when(transactionHeadRepository.findById(10)).thenReturn(Optional.of(view));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException(
                "TRANSACTION_HEAD",
                10,
                "read",
                "You do not have permission to access this transaction head.",
                Map.of("lenderId", 1, "borrowerId", 2)
        );
        doThrow(denied).when(transactionHeadAccessPolicy).requireReadAccess(
                any(TransactionHeadDTO.class)
        );

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadService.getTransactionHead(10)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("lenderId", 1, "borrowerId", 2), exception.getMetadata());
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
        verify(customerAccessPolicy).requireReadAccess(3);
    }

    @Test
    void getTransactionHeadsByCustomerId_throwsAccessDeniedWhenCustomerIsNotAccessible() {
        doThrow(new ResourceAccessDeniedException("CUSTOMER", 3, "read", "Denied"))
                .when(customerAccessPolicy).requireReadAccess(3);

        assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadService.getTransactionHeadsByCustomerId(3)
        );

        verify(transactionHeadRepository, never()).findAllByLenderIdOrBorrowerId(3);
    }

    @Test
    void getTransactionHeadForCustomer_returnsHeadWhenCustomerIsAccessibleAndLinked() {
        when(transactionHeadRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 3, 8)));

        TransactionHeadDTO result = transactionHeadService.getTransactionHeadForCustomer(3, 30);

        assertEquals(30, result.id());
        assertEquals(3, result.lenderId());
        verify(customerAccessPolicy).requireReadAccess(3);
        verify(transactionHeadAccessPolicy).requireLinkedToCustomer(3, result);
    }

    @Test
    void getTransactionHeadForCustomer_throwsAccessDeniedWhenHeadIsNotLinkedToCustomer() {
        when(transactionHeadRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 1, 2)));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException(
                "TRANSACTION_HEAD",
                30,
                "read",
                "You do not have permission to access this transaction head for the specified customer.",
                Map.of("customerId", 3, "reason", "NOT_LINKED_TO_CUSTOMER")
        );
        doThrow(denied).when(transactionHeadAccessPolicy).requireLinkedToCustomer(
                org.mockito.ArgumentMatchers.eq(3),
                any(TransactionHeadDTO.class)
        );

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadService.getTransactionHeadForCustomer(3, 30)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(30, exception.getResourceId());
        assertEquals(Map.of("customerId", 3, "reason", "NOT_LINKED_TO_CUSTOMER"), exception.getMetadata());
    }

    @Test
    void getTransactionHeadForCustomer_throwsAccessDeniedBeforeLoadingHeadWhenCustomerIsNotAccessible() {
        doThrow(new ResourceAccessDeniedException("CUSTOMER", 3, "read", "Denied"))
                .when(customerAccessPolicy).requireReadAccess(3);

        assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadService.getTransactionHeadForCustomer(3, 30)
        );

        verify(transactionHeadRepository, never()).findById(30);
    }

    @Test
    void getTransactionHeadForAccessibleCustomer_skipsCustomerAccessCheckButValidatesLink() {
        when(transactionHeadRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 3, 8)));

        TransactionHeadDTO result = transactionHeadService.getTransactionHeadForAccessibleCustomer(3, 30);

        verify(customerAccessPolicy, never()).requireReadAccess(3);
        verify(transactionHeadAccessPolicy).requireLinkedToCustomer(3, result);
    }

    @Test
    void getTransactionHeadsForAccessibleCustomer_skipsCustomerAccessCheck() {
        when(transactionHeadRepository.findAllByLenderIdOrBorrowerId(3)).thenReturn(List.of(
                transactionHeadView(30, 3, 7)
        ));

        List<TransactionHeadDTO> result = transactionHeadService.getTransactionHeadsForAccessibleCustomer(3);

        assertEquals(1, result.size());
        verify(customerAccessPolicy, never()).requireReadAccess(3);
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
