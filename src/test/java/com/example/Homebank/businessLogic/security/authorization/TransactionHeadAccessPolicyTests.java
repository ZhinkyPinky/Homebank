package com.example.Homebank.businessLogic.security.authorization;

import com.example.Homebank.dataAccess.repositories.TransactionHeadViewRepository;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
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
    void requireReadAccess_accessibleParticipant_allowsAccess() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);
        when(customerAccessPolicy.canReadAny(List.of(1, 2))).thenReturn(true);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireReadAccess(transactionHead));
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @Test
    void requireReadAccess_headIdWithAccessibleParticipant_allowsAccess() {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(111);
        head.setBorrowerId(222);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));
        when(customerAccessPolicy.canReadAny(List.of(111, 222))).thenReturn(true);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireReadAccess(10));

        verify(transactionHeadViewRepository).findById(10);
        verifyNoMoreInteractions(transactionHeadViewRepository);
        verify(customerAccessPolicy).canReadAny(List.of(111, 222));
    }

    @Test
    void requireReadAccess_headIdWithInaccessibleParticipants_throwsAccessDeniedWithStoredResourceDetails() {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(111);
        head.setBorrowerId(222);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));
        when(customerAccessPolicy.canReadAny(List.of(111, 222))).thenReturn(false);

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireReadAccess(10)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("lenderId", 111, "borrowerId", 222), exception.getMetadata());
        verify(customerAccessPolicy).canReadAny(List.of(111, 222));
    }

    @Test
    void requireReadAccess_missingHeadId_throwsResourceNotFoundWithoutCheckingCustomers() {
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionHeadAccessPolicy.requireReadAccess(10)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        verifyNoInteractions(customerAccessPolicy);
    }

    @Test
    void requireReadAccess_inaccessibleParticipants_throwsAccessDeniedWithResourceDetails() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);
        when(customerAccessPolicy.canReadAny(List.of(1, 2))).thenReturn(false);

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireReadAccess(transactionHead)
        );

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("lenderId", 1, "borrowerId", 2), exception.getMetadata());
    }

    @Test
    void requireWriteAccess_existingHeadId_checksBothStoredParticipants() {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(1);
        head.setBorrowerId(2);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireWriteAccess(10));

        verify(customerAccessPolicy).requireReadAccess(1);
        verify(customerAccessPolicy).requireReadAccess(2);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void requireWriteAccess_headIdWithEitherParentInaccessible_throwsAccessDenied(int inaccessibleCustomerId) {
        TransactionHeadView head = new TransactionHeadView();
        head.setId(10);
        head.setLenderId(1);
        head.setBorrowerId(2);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(head));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException(
                "CUSTOMER", inaccessibleCustomerId, "read", "Denied");
        if (inaccessibleCustomerId == 2) {
            doNothing().when(customerAccessPolicy).requireReadAccess(1);
        }
        doThrow(denied).when(customerAccessPolicy).requireReadAccess(inaccessibleCustomerId);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireWriteAccess(10)));
    }

    @Test
    void requireWriteAccess_missingHeadId_throwsResourceNotFoundWithoutCheckingCustomers() {
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionHeadAccessPolicy.requireWriteAccess(10));

        assertEquals("TRANSACTION_HEAD", exception.getResourceType());
        assertEquals(10, exception.getResourceId());
        verifyNoInteractions(customerAccessPolicy);
    }

    @Test
    void requireWriteAccess_existingHeadDto_checksBothStoredParticipants() {
        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireWriteAccess(transactionHead(10, 1, 2)));

        verify(customerAccessPolicy).requireReadAccess(1);
        verify(customerAccessPolicy).requireReadAccess(2);
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @Test
    void requireWriteAccess_participantIds_checksBothCustomers() {
        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireWriteAccess(1, 2));

        verify(customerAccessPolicy).requireReadAccess(1);
        verify(customerAccessPolicy).requireReadAccess(2);
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void requireWriteAccess_eitherParentInaccessible_throwsAccessDenied(int inaccessibleCustomerId) {
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException(
                "CUSTOMER", inaccessibleCustomerId, "read", "Denied");
        if (inaccessibleCustomerId == 2) {
            doNothing().when(customerAccessPolicy).requireReadAccess(1);
        }
        doThrow(denied).when(customerAccessPolicy).requireReadAccess(inaccessibleCustomerId);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireWriteAccess(transactionHead(10, 1, 2))));
        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionHeadAccessPolicy.requireWriteAccess(1, 2)));
    }

    @Test
    void requireLinkedToCustomer_customerIsLenderOrBorrower_allowsAccess() {
        TransactionHeadDTO transactionHead = transactionHead(10, 1, 2);

        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireLinkedToCustomer(1, transactionHead));
        assertDoesNotThrow(() -> transactionHeadAccessPolicy.requireLinkedToCustomer(2, transactionHead));
    }

    @Test
    void requireLinkedToCustomer_unrelatedCustomer_throwsAccessDeniedWithResourceDetails() {
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
