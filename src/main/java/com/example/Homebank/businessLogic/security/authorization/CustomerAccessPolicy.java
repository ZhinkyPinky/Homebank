package com.example.Homebank.businessLogic.security.authorization;

import com.example.Homebank.businessLogic.security.AuthenticatedUserProvider;
import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.repositories.CustomerRepository;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Centralizes ownership and membership authorization rules for customers.
 */
@Component
@RequiredArgsConstructor
public class CustomerAccessPolicy {
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final CustomerRepository customerRepository;

    /**
     * Requires the user to own or be a member of the specified customer.
     *
     * @param customerId ID of the customer being accessed.
     * @throws ResourceAccessDeniedException if the user cannot access the customer.
     */
    public void requireReadAccess(int customerId) {
        int authenticatedUserId = authenticatedUserProvider.getAuthenticatedUser().getId();
        if (!canReadAny(authenticatedUserId, List.of(customerId))) {
            throw new ResourceAccessDeniedException(
                    "CUSTOMER",
                    customerId,
                    "read",
                    "You do not have permission to access this customer.",
                    Map.of("userId", authenticatedUserId)
            );
        }
    }

    /**
     * Requires the user to own the specified customer.
     *
     * @param customer Customer whose ownership is required.
     * @param action   Action included in structured error details.
     * @param message  Client-facing denial message.
     * @throws ResourceAccessDeniedException if the user does not own the customer.
     */
    public void requireOwnership(CustomerEntity customer, String action, String message) {
        int authenticatedUserId = authenticatedUserProvider.getAuthenticatedUser().getId();
        Integer ownerId = customer.getOwner() == null ? null : customer.getOwner().getId();
        if (ownerId == null || ownerId != authenticatedUserId) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("ownerId", ownerId);
            metadata.put("userId", authenticatedUserId);

            throw new ResourceAccessDeniedException(
                    "CUSTOMER",
                    customer.getId(),
                    action,
                    message,
                    metadata
            );
        }
    }

    /**
     * Returns whether the user can read at least one of the supplied customers.
     *
     * @param customerIds Customer IDs to check.
     */
    public boolean canReadAny(List<Integer> customerIds) {
        int authenticatedUserId = authenticatedUserProvider.getAuthenticatedUser().getId();
        return canReadAny(authenticatedUserId, customerIds);
    }

    private boolean canReadAny(int authenticatedUserId, List<Integer> customerIds) {
        List<Integer> accessibleCustomerIds = customerRepository.findAccessibleCustomerIds(authenticatedUserId);
        return customerIds.stream().anyMatch(accessibleCustomerIds::contains);
    }
}
