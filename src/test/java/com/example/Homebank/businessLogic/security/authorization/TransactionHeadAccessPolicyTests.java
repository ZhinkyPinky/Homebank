package com.example.Homebank.businessLogic.security.authorization;

import com.example.Homebank.dataAccess.repositories.TransactionHeadViewRepository;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class TransactionHeadAccessPolicyTests {

    @Mock
    private CustomerAccessPolicy customerAccessPolicy;

    @Mock
    private TransactionHeadViewRepository transactionHeadViewRepository;

    @InjectMocks
    private TransactionHeadAccessPolicy transactionHeadAccessPolicy;

    @Test
    void requireAccess_allowsAccessThroughParticipatingCustomer() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);
        when(customerAccessPolicy.canReadAny(List.of(1, 2))).thenReturn(true);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireAccess(transactionHead));
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @Test
    void requireAccess_byId_allowsAccessThroughStoredParticipants() {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(111);
        head.setBorrowerId(222);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));
        when(customerAccessPolicy.canReadAny(List.of(111, 222))).thenReturn(true);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireAccess(10));

        verify(transactionHeadViewRepository).findById(10);
        verifyNoMoreInteractions(transactionHeadViewRepository);
        verify(customerAccessPolicy).canReadAny(List.of(111, 222));
    }

    @Test
    void requireAccess_byId_rejectsInaccessibleHeadWithStoredResourceDetails() {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(111);
        head.setBorrowerId(222);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));
        when(customerAccessPolicy.canReadAny(List.of(111, 222))).thenReturn(false);

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireAccess(10)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("lenderId", 111, "borrowerId", 222), exception.getMetadata());
        verify(customerAccessPolicy).canReadAny(List.of(111, 222));
    }

    @Test
    void requireAccess_byId_missingHeadReturnsNotFoundWithoutCheckingCustomers() {
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionHeadAccessPolicy.requireAccess(10)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        verifyNoInteractions(customerAccessPolicy);
    }

    @Test
    void requireAccess_rejectsInaccessibleHeadWithResourceDetails() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);
        when(customerAccessPolicy.canReadAny(List.of(1, 2))).thenReturn(false);

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireAccess(transactionHead)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("lenderId", 1, "borrowerId", 2), exception.getMetadata());
    }

    @Test
    void requireLinkedToCustomer_allowsLenderOrBorrower() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireLinkedToCustomer(1, transactionHead));
        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireLinkedToCustomer(2, transactionHead));
    }

    @Test
    void requireLinkedToCustomer_rejectsUnrelatedCustomerWithResourceDetails() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireLinkedToCustomer(3, transactionHead)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("customerId", 3, "reason", "NOT_LINKED_TO_CUSTOMER"), exception.getMetadata());
    }

    private TransactionHeadDTO transactionHead(int id, int lenderId, int borrowerId) {
        return new TransactionHeadDTO(
                id,
                lenderId,
                borrowerId,
                "Head",
                "Description",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                0,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
