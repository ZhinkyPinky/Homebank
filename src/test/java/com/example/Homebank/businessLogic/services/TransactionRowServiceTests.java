package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.authorization.TransactionHeadAccessPolicy;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.dataAccess.repositories.TransactionRowViewRepository;
import com.example.Homebank.dataAccess.views.TransactionRowView;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.CreateTransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.UpdateTransactionRowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class TransactionRowServiceTests {

    @Mock
    private TransactionRowViewRepository transactionRowViewRepository;

    @Mock
    private TransactionHeadAccessPolicy transactionHeadAccessPolicy;

    @InjectMocks
    private TransactionRowService transactionRowService;

    @Test
    void getTransactionRowById_existingRow_returnsMappedDto() {
        TransactionRowView view = transactionRowView(700, 70);
        when(transactionRowViewRepository.findById(700)).thenReturn(Optional.of(view));

        TransactionRowDTO result = transactionRowService.getTransactionRowById(700);

        assertEquals(700, result.id());
        assertEquals(70, result.transactionHeadId());
        verify(transactionHeadAccessPolicy).requireReadAccess(70);
    }

    @Test
    void getTransactionRowById_missingRow_throwsResourceNotFound() {
        when(transactionRowViewRepository.findById(999)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionRowService.getTransactionRowById(999)
        );

        assertEquals("TRANSACTION_ROW", exception.getResourceType());
        assertEquals(999, exception.getResourceId());
        assertEquals("The transaction row could not be found.", exception.getMessage());
        verifyNoInteractions(transactionHeadAccessPolicy);
    }

    @Test
    void getAllByTransactionHeadId_accessibleParent_returnsMappedDtos() {
        when(transactionRowViewRepository.findAllByTransactionHeadId(10)).thenReturn(List.of(
                transactionRowView(100, 10),
                transactionRowView(101, 10)
        ));

        List<TransactionRowDTO> result = transactionRowService.getAllByTransactionHeadId(10);

        assertEquals(2, result.size());
        assertEquals(100, result.get(0).id());
        assertEquals(101, result.get(1).id());
        var order = inOrder(transactionHeadAccessPolicy, transactionRowViewRepository);
        order.verify(transactionHeadAccessPolicy).requireReadAccess(10);
        order.verify(transactionRowViewRepository).findAllByTransactionHeadId(10);
    }

    @Test
    void updateTransactionRow_matchingVersion_delegatesToRepository() {
        TransactionRowDTO dto = transactionRowDto(80, 8);
        when(transactionRowViewRepository.findById(80)).thenReturn(Optional.of(transactionRowView(80, 8)));
        Map<String, Object> expected = Map.of("p_OUT_Id", 80);

        when(transactionRowViewRepository.saveTransactionRow(
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

        transactionRowService.updateTransactionRow(80, updateRequest(dto));

        verify(transactionHeadAccessPolicy).requireWriteAccess(8);

        verify(transactionRowViewRepository).saveTransactionRow(
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
    void deleteTransactionRow_repositoryFailure_rethrowsException() {
        TransactionRowDTO dto = transactionRowDto(90, 9);
        RuntimeException boom = new RuntimeException("delete failed");
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        when(transactionRowViewRepository.deleteTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.rowVersion()
        )).thenThrow(boom);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionRowService.deleteTransactionRow(dto.id(), dto.rowVersion()));

        assertSame(boom, exception);
        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verify(transactionRowViewRepository).deleteTransactionRow(
                dto.id(),
                dto.transactionHeadId(),
                dto.transactionRowNo(),
                dto.rowVersion()
        );
    }

    @Test
    void getTransactionRowById_deniedParent_preventsRead() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireReadAccess(9);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionRowService.getTransactionRowById(90)));

        verify(transactionHeadAccessPolicy).requireReadAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void updateTransactionRow_deniedParent_preventsWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("CUSTOMER", 2, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionRowService.updateTransactionRow(90, updateRequest(transactionRowDto(90, 9)))));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void deleteTransactionRow_deniedParent_preventsWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("CUSTOMER", 2, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionRowService.deleteTransactionRow(90, null)));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void createTransactionRow_deniedParent_preventsInsert() {
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("CUSTOMER", 2, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionRowService.createTransactionRow(createRequest())));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoInteractions(transactionRowViewRepository);
    }

    @Test
    void getAllByTransactionHeadId_deniedParent_preventsList() {
        ResourceAccessDeniedException denied = new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied");
        doThrow(denied).when(transactionHeadAccessPolicy).requireReadAccess(9);

        assertSame(denied, assertThrows(ResourceAccessDeniedException.class,
                () -> transactionRowService.getAllByTransactionHeadId(9)));

        verify(transactionHeadAccessPolicy).requireReadAccess(9);
        verifyNoInteractions(transactionRowViewRepository);
    }

    @Test
    void updateTransactionRow_missingPathId_doesNotWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.updateTransactionRow(90, updateRequest(transactionRowDto(90, 9))));

        verifyNoInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void updateTransactionRow_staleVersion_doesNotWrite() {
        TransactionRowView stored = transactionRowView(90, 9);
        stored.setRowVersion(LocalDateTime.parse("2026-02-01T00:00:00"));
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(stored));

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> transactionRowService.updateTransactionRow(90, updateRequest(transactionRowDto(90, 9))));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void createTransactionRow_accessibleParent_insertsWithGeneratedIdRowNumberAndVersion() {
        CreateTransactionRowDTO dto = createRequest();

        transactionRowService.createTransactionRow(dto);

        var order = inOrder(transactionHeadAccessPolicy, transactionRowViewRepository);
        order.verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        order.verify(transactionRowViewRepository).saveTransactionRow(-1, 9, -1,
                dto.typeOfTransactionCode(), dto.name(), dto.description(), dto.paymentDate(), dto.amount(), null);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void deleteTransactionRow_matchingVersion_authorizesBeforeDeletingStoredRow() {
        TransactionRowView stored = transactionRowView(90, 9);
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(stored));

        transactionRowService.deleteTransactionRow(90, stored.getRowVersion());

        var order = inOrder(transactionRowViewRepository, transactionHeadAccessPolicy);
        order.verify(transactionRowViewRepository).findById(90);
        order.verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        order.verify(transactionRowViewRepository).deleteTransactionRow(90, 9, stored.getTransactionRowNo(), stored.getRowVersion());
    }

    @Test
    void deleteTransactionRow_staleClientVersion_doesNotWrite() {
        TransactionRowView stored = transactionRowView(90, 9);
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(stored));

        assertThrows(ObjectOptimisticLockingFailureException.class,
                () -> transactionRowService.deleteTransactionRow(90,
                        stored.getRowVersion().minusDays(1)));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void deleteTransactionRow_missingRow_doesNotAuthorizeOrWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.deleteTransactionRow(90,
                        LocalDateTime.parse("2026-01-01T00:00:00")));

        assertEquals("TRANSACTION_ROW", exception.getResourceType());
        assertEquals(90, exception.getResourceId());
        verifyNoInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void createTransactionRow_missingParent_preventsInsert() {
        ResourceNotFoundException missing = new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing");
        doThrow(missing).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(missing, assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.createTransactionRow(createRequest())));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoInteractions(transactionRowViewRepository);
    }

    @Test
    void getAllByTransactionHeadId_missingParent_preventsList() {
        ResourceNotFoundException missing = new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing");
        doThrow(missing).when(transactionHeadAccessPolicy).requireReadAccess(9);

        assertSame(missing, assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.getAllByTransactionHeadId(9)));

        verify(transactionHeadAccessPolicy).requireReadAccess(9);
        verifyNoInteractions(transactionRowViewRepository);
    }

    @Test
    void getTransactionRowById_missingParent_preventsRead() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceNotFoundException missing = new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing");
        doThrow(missing).when(transactionHeadAccessPolicy).requireReadAccess(9);

        assertSame(missing, assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.getTransactionRowById(90)));

        verify(transactionHeadAccessPolicy).requireReadAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void updateTransactionRow_missingParent_preventsWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceNotFoundException missing = new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing");
        doThrow(missing).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(missing, assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.updateTransactionRow(90, updateRequest(transactionRowDto(90, 9)))));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    @Test
    void deleteTransactionRow_missingParent_preventsWrite() {
        when(transactionRowViewRepository.findById(90)).thenReturn(Optional.of(transactionRowView(90, 9)));
        ResourceNotFoundException missing = new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing");
        doThrow(missing).when(transactionHeadAccessPolicy).requireWriteAccess(9);

        assertSame(missing, assertThrows(ResourceNotFoundException.class,
                () -> transactionRowService.deleteTransactionRow(90, null)));

        verify(transactionHeadAccessPolicy).requireWriteAccess(9);
        verifyNoMoreInteractions(transactionHeadAccessPolicy);
        verify(transactionRowViewRepository).findById(90);
        verifyNoMoreInteractions(transactionRowViewRepository);
    }

    private CreateTransactionRowDTO createRequest() {
        return new CreateTransactionRowDTO(9, "DEBIT", "Payment", "desc", LocalDate.parse("2026-01-15"), 50);
    }

    private UpdateTransactionRowDTO updateRequest(TransactionRowDTO dto) {
        return new UpdateTransactionRowDTO(dto.transactionRowNo(), dto.typeOfTransactionCode(), dto.name(),
                dto.description(), dto.paymentDate(), dto.amount(), dto.rowVersion());
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
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
