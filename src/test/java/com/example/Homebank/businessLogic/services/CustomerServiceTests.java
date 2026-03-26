package com.example.Homebank.businessLogic.services;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.dataAccess.repositories.CustomerViewRepository;
import com.example.Homebank.dataAccess.views.CustomerView;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.presentation.dto.composite.CustomersAndTransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTests {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerViewRepository customerViewRepository;

    @Mock
    private TransactionHeadService transactionHeadService;

    @Mock
    private TransactionRowService transactionRowService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void getCustomers_returnsOnlyAccessibleCustomers() {
        UserEntity user = new UserEntity();
        user.setId(7);

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(1, 2));
        when(customerViewRepository.findAllById(List.of(1, 2))).thenReturn(List.of(
                customerView(1, "C1"),
                customerView(2, "C2")
        ));

        List<CustomerDTO> result = customerService.getCustomers();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals(2, result.get(1).id());

        verify(customerRepository).findAccessibleCustomerIds(7);
        verify(customerViewRepository).findAllById(List.of(1, 2));
    }

    @Test
    void getCustomers_returnsEmptyWhenNoAccessibleCustomers() {
        UserEntity user = new UserEntity();
        user.setId(7);

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of());

        List<CustomerDTO> result = customerService.getCustomers();

        assertEquals(0, result.size());
        verify(customerRepository).findAccessibleCustomerIds(7);
        verify(customerViewRepository, never()).findAllById(List.of());
    }

    @Test
    void getCustomersAndTransactionHead_throwsWhenHeadIsNotAccessible() {
        UserEntity user = new UserEntity();
        user.setId(7);

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(1, 2));
        when(customerViewRepository.findAllById(List.of(1, 2))).thenReturn(List.of(
                customerView(1, "C1"),
                customerView(2, "C2")
        ));
        when(transactionHeadService.getTransactionHead(10)).thenReturn(transactionHead(10, 99, 100));

        assertThrows(AccessDeniedException.class, () -> customerService.getCustomersAndTransactionHead(10));
    }

    @Test
    void getCustomersAndTransactionHead_returnsWhenHeadIsAccessible() {
        UserEntity user = new UserEntity();
        user.setId(7);

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(1, 2));
        when(customerViewRepository.findAllById(List.of(1, 2))).thenReturn(List.of(
                customerView(1, "C1"),
                customerView(2, "C2")
        ));
        when(transactionHeadService.getTransactionHead(10)).thenReturn(transactionHead(10, 2, 100));

        CustomersAndTransactionHeadDTO result = customerService.getCustomersAndTransactionHead(10);

        assertEquals(2, result.customers().size());
        assertEquals(10, result.transactionHead().id());
    }

    @Test
    void deleteCustomer_ownerDeletesSuccessfully() {
        int customerId = 12;
        UserEntity authenticatedUser = user(7);
        CustomerEntity customer = customerEntity(customerId, user(7));

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(customerId);

        verify(customerRepository).deleteById(customerId);
    }

    @Test
    void deleteCustomer_nonOwnerThrowsAccessDeniedAndDoesNotDelete() {
        int customerId = 12;
        UserEntity authenticatedUser = user(7);
        CustomerEntity customer = customerEntity(customerId, user(99));

        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(ResourceAccessDeniedException.class, () -> customerService.deleteCustomer(customerId));

        verify(customerRepository, never()).deleteById(customerId);
    }

    private CustomerView customerView(int id, String name) {
        return new CustomerView(
                id,
                name,
                "desc",
                "KONTO",
                0,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private TransactionHeadDTO transactionHead(int id, int lenderId, int borrowerId) {
        return new TransactionHeadDTO(
                id,
                lenderId,
                borrowerId,
                "Head",
                "desc",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                0,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private UserEntity user(int id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }

    private CustomerEntity customerEntity(int customerId, UserEntity owner) {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setId(customerId);
        customerEntity.setOwner(owner);
        return customerEntity;
    }
}
