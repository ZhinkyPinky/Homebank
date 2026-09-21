package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.businessLogic.security.authorization.CustomerAccessPolicy;
import com.example.Homebank.businessLogic.security.authorization.TransactionHeadAccessPolicy;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.TransactionHeadViewRepository;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.DeleteTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.CreateTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.UpdateTransactionHeadDTO;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionHeadServiceTests {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private CustomerAccessPolicy customerAccessPolicy;

    @Mock
    private TransactionHeadAccessPolicy transactionHeadAccessPolicy;

    @Mock
    private TransactionHeadViewRepository transactionHeadViewRepository;

    @InjectMocks
    private TransactionHeadService transactionHeadService;

    @Test
    void getTransactionHead_returnsMappedDtoWhenFound() {
        TransactionHeadView view = transactionHeadView(10, 1, 2);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(view));

        TransactionHeadDTO result = transactionHeadService.getTransactionHead(10);

        assertEquals(10, result.id());
        assertEquals(1, result.lenderId());
        assertEquals(2, result.borrowerId());
        verify(transactionHeadAccessPolicy).requireAccess(result);
    }

    @Test
    void getTransactionHead_throwsAccessDeniedWhenNeitherCustomerIsAccessible() {
        TransactionHeadView view = transactionHeadView(10, 1, 2);
        when(transactionHeadViewRepository.findById(10)).thenReturn(Optional.of(view));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException(
                "TRANSACTION_HEAD",
                10,
                "read",
                "You do not have permission to access this transaction head.",
                Map.of("lenderId", 1, "borrowerId", 2)
        );
        doThrow(denied).when(transactionHeadAccessPolicy).requireAccess(
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
        when(transactionHeadViewRepository.findById(55)).thenReturn(Optional.empty());

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
        when(transactionHeadViewRepository.findAllByLenderIdOrBorrowerId(3)).thenReturn(List.of(
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

        verify(transactionHeadViewRepository, never()).findAllByLenderIdOrBorrowerId(3);
    }

    @Test
    void getTransactionHeadForCustomer_returnsHeadWhenCustomerIsAccessibleAndLinked() {
        when(transactionHeadViewRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 3, 8)));

        TransactionHeadDTO result = transactionHeadService.getTransactionHeadForCustomer(3, 30);

        assertEquals(30, result.id());
        assertEquals(3, result.lenderId());
        verify(customerAccessPolicy).requireReadAccess(3);
        verify(transactionHeadAccessPolicy).requireLinkedToCustomer(3, result);
    }

    @Test
    void getTransactionHeadForCustomer_throwsAccessDeniedWhenHeadIsNotLinkedToCustomer() {
        when(transactionHeadViewRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 1, 2)));
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

        verify(transactionHeadViewRepository, never()).findById(30);
    }

    @Test
    void getTransactionHeadForAccessibleCustomer_skipsCustomerAccessCheckButValidatesLink() {
        when(transactionHeadViewRepository.findById(30)).thenReturn(Optional.of(transactionHeadView(30, 3, 8)));

        TransactionHeadDTO result = transactionHeadService.getTransactionHeadForAccessibleCustomer(3, 30);

        verify(customerAccessPolicy, never()).requireReadAccess(3);
        verify(transactionHeadAccessPolicy).requireLinkedToCustomer(3, result);
    }

    @Test
    void getTransactionHeadsForAccessibleCustomer_skipsCustomerAccessCheck() {
        when(transactionHeadViewRepository.findAllByLenderIdOrBorrowerId(3)).thenReturn(List.of(
                transactionHeadView(30, 3, 7)
        ));

        List<TransactionHeadDTO> result = transactionHeadService.getTransactionHeadsForAccessibleCustomer(3);

        assertEquals(1, result.size());
        verify(customerAccessPolicy, never()).requireReadAccess(3);
    }

    @Test
    void createTransactionHead_passesCreationSentinelAndNoVersion() {
        CreateTransactionHeadDTO dto = new CreateTransactionHeadDTO(1, 2, "Loan", "desc", LocalDate.parse("2026-01-01"), null, null);
        Map<String, Object> expected = Map.of("p_OUT_Id", 12);

        when(transactionHeadViewRepository.saveTransactionHead(
                -1,
                dto.lenderId(),
                dto.borrowerId(),
                dto.transactionName(),
                dto.description(),
                dto.startDate(),
                dto.prelEndDate(),
                dto.endDate(),
                null
        )).thenReturn(expected);

        transactionHeadService.createTransactionHead(dto);

        verify(customerAccessPolicy).requireReadAccess(1);
        verify(customerAccessPolicy).requireReadAccess(2);
        verify(transactionHeadViewRepository).saveTransactionHead(
                -1,
                dto.lenderId(),
                dto.borrowerId(),
                dto.transactionName(),
                dto.description(),
                dto.startDate(),
                dto.prelEndDate(),
                dto.endDate(),
                null
        );
    }

    @Test
    void createTransactionHead_inaccessibleLenderDoesNotWrite() {
        CreateTransactionHeadDTO dto = new CreateTransactionHeadDTO(1, 2, "Loan", null, LocalDate.parse("2026-01-01"), null, null);
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("CUSTOMER", 1, "read", "Denied");
        doThrow(denied).when(customerAccessPolicy).requireReadAccess(1);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class, () -> transactionHeadService.createTransactionHead(dto)));
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @Test
    void createTransactionHead_inaccessibleBorrowerDoesNotWrite() {
        CreateTransactionHeadDTO dto = new CreateTransactionHeadDTO(1, 2, "Loan", null, LocalDate.parse("2026-01-01"), null, null);
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("CUSTOMER", 2, "read", "Denied");
        doNothing().when(customerAccessPolicy).requireReadAccess(1);
        doThrow(denied).when(customerAccessPolicy).requireReadAccess(2);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class, () -> transactionHeadService.createTransactionHead(dto)));
        verifyNoInteractions(transactionHeadViewRepository);
    }

    @Test
    void updateTransactionHead_preservesParticipantsAndPassesExpectedVersion() {
        TransactionHeadView existing = transactionHeadView(12, 111, 222);
        UpdateTransactionHeadDTO dto = new UpdateTransactionHeadDTO("Updated", "new desc", LocalDate.parse("2026-02-01"), null, null, existing.getRowVersion());
        when(transactionHeadViewRepository.findById(12)).thenReturn(Optional.of(existing));

        transactionHeadService.updateTransactionHead(12, dto);

        verify(transactionHeadAccessPolicy).requireAccess(TransactionHeadDTO.fromView(existing));
        verify(transactionHeadViewRepository).saveTransactionHead(12, 111, 222, dto.transactionName(), dto.description(), dto.startDate(), null, null, existing.getRowVersion());
    }

    @Test
    void updateTransactionHead_deniedAccessDoesNotWrite() {
        TransactionHeadView existing = transactionHeadView(12, 111, 222);
        when(transactionHeadViewRepository.findById(12)).thenReturn(Optional.of(existing));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("TRANSACTION_HEAD", 12, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireAccess(TransactionHeadDTO.fromView(existing));
        UpdateTransactionHeadDTO dto = new UpdateTransactionHeadDTO("Updated", null, LocalDate.parse("2026-01-01"), null, null, existing.getRowVersion());

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class, () -> transactionHeadService.updateTransactionHead(12, dto)));
        verify(transactionHeadViewRepository).findById(12);
        org.mockito.Mockito.verifyNoMoreInteractions(transactionHeadViewRepository);
    }

    @Test
    void updateTransactionHead_staleVersionDoesNotWrite() {
        when(transactionHeadViewRepository.findById(12)).thenReturn(Optional.of(transactionHeadView(12, 1, 2)));
        UpdateTransactionHeadDTO dto = new UpdateTransactionHeadDTO("Updated", null, LocalDate.parse("2026-01-01"), null, null, LocalDateTime.parse("2025-01-01T00:00:00"));

        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () -> transactionHeadService.updateTransactionHead(12, dto));
        verify(transactionHeadViewRepository).findById(12);
        org.mockito.Mockito.verifyNoMoreInteractions(transactionHeadViewRepository);
    }

    @Test
    void deleteTransactionHead_authorizesStoredParticipantsAndUsesClientVersion() {
        TransactionHeadView existing = transactionHeadView(22, 111, 222);
        existing.setRowVersion(LocalDateTime.parse("2026-02-01T00:00:00"));
        DeleteTransactionHeadDTO request = deleteTransactionHeadDto(22);
        when(transactionHeadViewRepository.findById(22)).thenReturn(Optional.of(existing));

        transactionHeadService.deleteTransactionHead(request);

        verify(transactionHeadAccessPolicy).requireAccess(TransactionHeadDTO.fromView(existing));
        verify(transactionHeadViewRepository).deleteTransactionHead(22, request.rowVersion());
    }

    @Test
    void deleteTransactionHead_deniedStoredHeadDoesNotWrite() {
        TransactionHeadView existing = transactionHeadView(22, 111, 222);
        when(transactionHeadViewRepository.findById(22)).thenReturn(Optional.of(existing));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("TRANSACTION_HEAD", 22, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireAccess(TransactionHeadDTO.fromView(existing));

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionHeadService.deleteTransactionHead(deleteTransactionHeadDto(22))));
        verify(transactionHeadViewRepository).findById(22);
        org.mockito.Mockito.verifyNoMoreInteractions(transactionHeadViewRepository);
    }

    @Test
    void deleteTransactionHead_missingHeadDoesNotWrite() {
        DeleteTransactionHeadDTO request = deleteTransactionHeadDto(22);

        when(transactionHeadViewRepository.findById(22)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionHeadService.deleteTransactionHead(request));
        verify(transactionHeadViewRepository).findById(22);
        org.mockito.Mockito.verifyNoMoreInteractions(transactionHeadViewRepository);
        org.mockito.Mockito.verifyNoInteractions(transactionHeadAccessPolicy);
    }

    @Test
    void deleteTransactionHead_rethrowsRepositoryException() {
        DeleteTransactionHeadDTO request = deleteTransactionHeadDto(22);
        RuntimeException boom = new RuntimeException("delete failed");

        when(transactionHeadViewRepository.findById(22)).thenReturn(Optional.of(transactionHeadView(22, 1, 2)));
        when(transactionHeadViewRepository.deleteTransactionHead(request.id(), request.rowVersion())).thenThrow(boom);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> transactionHeadService.deleteTransactionHead(request));

        assertSame(boom, exception);
        verify(transactionHeadViewRepository).deleteTransactionHead(request.id(), request.rowVersion());
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

    private DeleteTransactionHeadDTO deleteTransactionHeadDto(int id) {
        return new DeleteTransactionHeadDTO(
                id,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private UserEntity user(int id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }
}
