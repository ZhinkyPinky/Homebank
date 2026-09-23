package com.example.Homebank.businessLogic.security.authorization;

import com.example.Homebank.dataAccess.repositories.TransactionHeadViewRepository;
import com.example.Homebank.dataAccess.views.TransactionHeadView;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Centralizes authorization and resource-hierarchy rules for transaction heads.
 */
@Component
@RequiredArgsConstructor
public class TransactionHeadAccessPolicy {
    private final CustomerAccessPolicy customerAccessPolicy;
    private final TransactionHeadViewRepository transactionHeadViewRepository;

    /**
     * Loads a head and checks access through its stored lender or borrower.
     *
     * @param transactionHeadId ID of the transaction head being accessed.
     * @throws ResourceNotFoundException if the transaction head does not exist.
     * @throws ResourceAccessDeniedException if neither participating customer is accessible.
     */
    public void requireAccess(int transactionHeadId) {
        TransactionHeadView transactionHead = transactionHeadViewRepository.findById(transactionHeadId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TRANSACTION_HEAD", transactionHeadId, "Transaction head could not be found."
                ));

        requireAccess(TransactionHeadDTO.fromView(transactionHead));
    }

    /**
     * Requires read access to either the lender or borrower customer. This permits both reading
     * and editing the transaction head; its lender and borrower remain fixed after creation.
     *
     * @param transactionHead Transaction head being accessed.
     * @throws ResourceAccessDeniedException if neither participating customer is accessible.
     */
    public void requireAccess(TransactionHeadDTO transactionHead) {
        int lenderId = transactionHead.lenderId();
        int borrowerId = transactionHead.borrowerId();

        if (!customerAccessPolicy.canReadAny(List.of(lenderId, borrowerId))) {
            throw new ResourceAccessDeniedException(
                    "TRANSACTION_HEAD",
                    transactionHead.id(),
                    "read",
                    "You do not have permission to access this transaction head.",
                    Map.of("lenderId", lenderId, "borrowerId", borrowerId)
            );
        }
    }

    /**
     * Requires a transaction head to use the specified customer as lender or borrower.
     *
     * @param customerId      Customer ID from the resource path.
     * @param transactionHead Transaction head that must be linked to the customer.
     * @throws ResourceAccessDeniedException if the transaction head is not linked to the customer.
     */
    public void requireLinkedToCustomer(int customerId, TransactionHeadDTO transactionHead) {
        int lenderId = transactionHead.lenderId();
        int borrowerId = transactionHead.borrowerId();

        if (lenderId != customerId && borrowerId != customerId) {
            throw new ResourceAccessDeniedException(
                    "TRANSACTION_HEAD",
                    transactionHead.id(),
                    "read",
                    "You do not have permission to access this transaction head for the specified customer.",
                    Map.of("customerId", customerId, "reason", "NOT_LINKED_TO_CUSTOMER")
            );
        }
    }
}
