package com.example.Homebank.businessLogic.security.authorization;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.entities.UserEntity;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAccessPolicyTests {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerAccessPolicy customerAccessPolicy;

    @Test
    void requireReadAccess_allowsAccessibleCustomer() {
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(3));

        assertDoesNotThrow(() -> customerAccessPolicy.requireReadAccess(3));
    }

    @Test
    void requireReadAccess_rejectsInaccessibleCustomerWithResourceDetails() {
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(1, 2));

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> customerAccessPolicy.requireReadAccess(3)
        );

        assertEquals("CUSTOMER", exception.getResourceType());
        assertEquals(3, exception.getResourceId());
        assertEquals("read", exception.getAction());
        assertEquals(Map.of("userId", 7), exception.getMetadata());
    }

    @Test
    void canReadAny_matchesAnyAccessibleCustomer() {
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));
        when(customerRepository.findAccessibleCustomerIds(7)).thenReturn(List.of(2));

        assertTrue(customerAccessPolicy.canReadAny(List.of(1, 2)));
        assertFalse(customerAccessPolicy.canReadAny(List.of(3, 4)));
    }

    @Test
    void requireOwnership_allowsOwner() {
        CustomerEntity customer = customer(3, 7);
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));

        assertDoesNotThrow(() -> customerAccessPolicy.requireOwnership(customer, "update", "Denied"));
    }

    @Test
    void requireOwnership_rejectsNonOwnerWithResourceDetails() {
        CustomerEntity customer = customer(3, 8);
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));

        ResourceAccessDeniedException exception = assertThrows(
                ResourceAccessDeniedException.class,
                () -> customerAccessPolicy.requireOwnership(customer, "delete", "Denied")
        );

        assertEquals("CUSTOMER", exception.getResourceType());
        assertEquals(3, exception.getResourceId());
        assertEquals("delete", exception.getAction());
        assertEquals(Map.of("ownerId", 8, "userId", 7), exception.getMetadata());
    }

    @Test
    void requireOwnership_rejectsCustomerWithoutOwner() {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(3);
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user(7));

        assertThrows(
                ResourceAccessDeniedException.class,
                () -> customerAccessPolicy.requireOwnership(customer, "update", "Denied")
        );
    }

    private CustomerEntity customer(int customerId, int ownerId) {
        CustomerEntity customer = new CustomerEntity();
        customer.setId(customerId);
        customer.setOwner(user(ownerId));
        return customer;
    }

    private UserEntity user(int id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }
}
