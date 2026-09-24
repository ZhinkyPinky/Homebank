package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.CustomerService;
import com.example.Homebank.presentation.dto.composite.CustomerAndTransactionHeadDTO;
import com.example.Homebank.presentation.dto.composite.CustomerAndTransactionHeadsDTO;
import com.example.Homebank.presentation.dto.composite.CustomerWithTransactionHeadAndRowDTO;
import com.example.Homebank.presentation.dto.composite.CustomerWithTransactionHeadAndRowsDTO;
import com.example.Homebank.presentation.dto.composite.CustomersAndTransactionHeadDTO;
import com.example.Homebank.presentation.dto.customer.CustomerDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerControllerGetTests {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void getCustomers_returnsCustomers() throws Exception {
        when(customerService.getCustomers()).thenReturn(List.of(customerDto(1), customerDto(2)));

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Customer 1"))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(customerService).getCustomers();
    }

    @Test
    void getCustomersAndTransactionHead_returnsComposite() throws Exception {
        CustomersAndTransactionHeadDTO dto = new CustomersAndTransactionHeadDTO(
                List.of(customerDto(1)),
                transactionHeadDto(10)
        );
        when(customerService.getCustomersAndTransactionHead(10)).thenReturn(dto);

        mockMvc.perform(get("/customers/transactionHeads/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customers[0].id").value(1))
                .andExpect(jsonPath("$.transactionHead.id").value(10));

        verify(customerService).getCustomersAndTransactionHead(10);
    }

    @Test
    void getCustomer_returnsCustomer() throws Exception {
        when(customerService.getCustomer(5)).thenReturn(customerDto(5));

        mockMvc.perform(get("/customers/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Customer 5"));

        verify(customerService).getCustomer(5);
    }

    @Test
    void getCustomerAndTransactionHeads_returnsComposite() throws Exception {
        CustomerAndTransactionHeadsDTO dto = new CustomerAndTransactionHeadsDTO(
                customerDto(3),
                List.of(transactionHeadDto(30), transactionHeadDto(31))
        );
        when(customerService.getCustomerAndTransactionHeads(3)).thenReturn(dto);

        mockMvc.perform(get("/customers/3/transactionHeads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(3))
                .andExpect(jsonPath("$.transactionHeads[0].id").value(30))
                .andExpect(jsonPath("$.transactionHeads[1].id").value(31));

        verify(customerService).getCustomerAndTransactionHeads(3);
    }

    @Test
    void getCustomerAndTransactionHead_returnsComposite() throws Exception {
        CustomerAndTransactionHeadDTO dto = new CustomerAndTransactionHeadDTO(customerDto(4), transactionHeadDto(40));
        when(customerService.getCustomerAndTransactionHead(4, 40)).thenReturn(dto);

        mockMvc.perform(get("/customers/4/transactionHeads/40"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(4))
                .andExpect(jsonPath("$.transactionHead.id").value(40));

        verify(customerService).getCustomerAndTransactionHead(4, 40);
    }

    @Test
    void getCustomerAndTransactionHeadAndRows_returnsComposite() throws Exception {
        CustomerWithTransactionHeadAndRowsDTO dto = new CustomerWithTransactionHeadAndRowsDTO(
                customerDto(6),
                transactionHeadDto(60),
                List.of(transactionRowDto(600), transactionRowDto(601))
        );
        when(customerService.getCustomerTransactionHeadAndRows(6, 60)).thenReturn(dto);

        mockMvc.perform(get("/customers/6/transactionHeads/60/transactionRows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(6))
                .andExpect(jsonPath("$.transactionHead.id").value(60))
                .andExpect(jsonPath("$.transactionRows[0].id").value(600));

        verify(customerService).getCustomerTransactionHeadAndRows(6, 60);
    }

    @Test
    void getCustomerAndTransactionHeadAndTransactionRow_returnsComposite() throws Exception {
        CustomerWithTransactionHeadAndRowDTO dto = new CustomerWithTransactionHeadAndRowDTO(
                customerDto(7),
                transactionHeadDto(70),
                transactionRowDto(700)
        );
        when(customerService.getCustomerAndTransactionHeadAndTransactionRow(7, 70, 700)).thenReturn(dto);

        mockMvc.perform(get("/customers/7/transactionHeads/70/transactionRows/700"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(7))
                .andExpect(jsonPath("$.transactionHead.id").value(70))
                .andExpect(jsonPath("$.transactionRow.id").value(700));

        verify(customerService).getCustomerAndTransactionHeadAndTransactionRow(7, 70, 700);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-01-01T00:00:00", "2026-01-01T00:00:00.1234567"})
    void deleteCustomer_validQueryVersion_returnsNoContentAndCallsService(String rowVersion) throws Exception {
        mockMvc.perform(delete("/customers/9").param("rowVersion", rowVersion))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(customerService).deleteCustomer(9, LocalDateTime.parse(rowVersion));
    }

    @Test
    void deleteCustomer_missingQueryVersion_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/customers/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        verifyNoInteractions(customerService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "null", "not-a-timestamp", "2026-01-01", "2026-01-01T25:00:00", "2026-02-30T00:00:00"})
    void deleteCustomer_invalidQueryVersion_returnsBadRequest(String rowVersion) throws Exception {
        mockMvc.perform(delete("/customers/9").param("rowVersion", rowVersion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
        verifyNoInteractions(customerService);
    }

    @Test
    void deleteCustomer_staleVersion_returnsConflict() throws Exception {
        LocalDateTime rowVersion = LocalDateTime.parse("2026-01-01T00:00:00");
        doThrow(new ObjectOptimisticLockingFailureException("Customer", 9))
                .when(customerService).deleteCustomer(9, rowVersion);

        mockMvc.perform(delete("/customers/9").param("rowVersion", rowVersion.toString()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ROW_VERSION_MISMATCH"));
        verify(customerService).deleteCustomer(9, rowVersion);
    }

    private CustomerDTO customerDto(int id) {
        return new CustomerDTO(
                id,
                "Customer " + id,
                "Description " + id,
                "KONTO",
                0,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private TransactionHeadDTO transactionHeadDto(int id) {
        return new TransactionHeadDTO(
                id,
                1,
                2,
                "Head " + id,
                "Head desc",
                LocalDate.parse("2026-01-01"),
                null,
                null,
                100,
                "Borrower",
                "Lender",
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }

    private TransactionRowDTO transactionRowDto(int id) {
        return new TransactionRowDTO(
                id,
                10,
                1,
                "DEBIT",
                "Row " + id,
                "Row desc",
                LocalDate.parse("2026-01-15"),
                50,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
