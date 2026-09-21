package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionHeadService;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import com.example.Homebank.presentation.dto.transactionhead.DeleteTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.TransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.CreateTransactionHeadDTO;
import com.example.Homebank.presentation.dto.transactionhead.UpdateTransactionHeadDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionHeadControllerTests {

    private MockMvc mockMvc;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Mock
    private TransactionHeadService transactionHeadService;

    @InjectMocks
    private TransactionHeadController transactionHeadController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(transactionHeadController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getTransactionHead_validId_returnsOkAndCallsService() throws Exception {
        int transactionHeadId = 10;
        TransactionHeadDTO transactionHead = validTransactionHeadDto(transactionHeadId);
        when(transactionHeadService.getTransactionHead(transactionHeadId)).thenReturn(transactionHead);

        String expectedJson = """
                {
                  "id": 10,
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "Loan",
                  "description": "Head desc",
                  "startDate": "2026-01-01",
                  "prelEndDate": null,
                  "endDate": null,
                  "amount": 100,
                  "borrower": "Borrower",
                  "lender": "Lender",
                  "rowVersion": "2026-01-01T00:00:00"
                }
                """;

        mockMvc.perform(get("/transactionHeads/" + transactionHeadId))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));

        verify(transactionHeadService).getTransactionHead(transactionHeadId);
    }

    @Test
    void getTransactionHead_invalidId_returnsBadRequestAndDoesNotCallService() throws Exception {
        String invalidId = "abc";

        mockMvc.perform(get("/transactionHeads/" + invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'transactionHeadId'."))
                .andExpect(jsonPath("$.path").value("/transactionHeads/abc"));

        verify(transactionHeadService, never()).getTransactionHead(anyInt());
    }

    @Test
    void getTransactionHead_nonexistentId_returnsNotFound() throws Exception {
        int nonexistentId = 999;

        ResourceNotFoundException exception = new ResourceNotFoundException("TRANSACTION_HEAD", nonexistentId, "Transaction head could not be found.");
        doThrow(exception).when(transactionHeadService).getTransactionHead(nonexistentId);

        mockMvc.perform(get("/transactionHeads/" + nonexistentId))
                .andExpect(status().isNotFound());

        verify(transactionHeadService).getTransactionHead(nonexistentId);
    }

    @Test
    void getTransactionHead_deniedAccess_returnsForbidden() throws Exception {
        int transactionHeadId = 10;
        int lenderId = 1;
        int borrowerId = 2;

        ResourceAccessDeniedException exception = new ResourceAccessDeniedException(
                "TRANSACTION_HEAD",
                transactionHeadId,
                "read",
                "You do not have permission to access this transaction head.",
                Map.of("lenderId", lenderId, "borrowerId", borrowerId)
        );

        doThrow(exception).when(transactionHeadService).getTransactionHead(transactionHeadId);

        mockMvc.perform(get("/transactionHeads/" + transactionHeadId))
                .andExpect(status().isForbidden());

        verify(transactionHeadService).getTransactionHead(transactionHeadId);
    }

    @Test
    void createTransactionHead_withoutIdOrVersion_returnsOkAndCallsService() throws Exception {
        CreateTransactionHeadDTO request = new CreateTransactionHeadDTO(1, 2, "Loan", "Head desc", LocalDate.parse("2026-01-01"), null, null);

        String json = """
                {
                  "lenderId": 1,
                  "borrowerId": 2,
                  "transactionName": "Loan",
                  "description": "Head desc",
                  "startDate": "2026-01-01",
                  "prelEndDate": null,
                  "endDate": null
                }
                """;

        mockMvc.perform(post("/transactionHeads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction head created."));

        verify(transactionHeadService).createTransactionHead(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {"lenderId", "borrowerId", "transactionName", "startDate"})
    void createTransactionHead_missingRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("lenderId", 1)
                .put("borrowerId", 2)
                .put("transactionName", "Loan")
                .put("startDate", "2026-01-01");

        payload.remove(field);

        mockMvc.perform(post("/transactionHeads").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value(
                        field.equals("transactionName") ? "NOT_BLANK" : "NOT_NULL"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"lenderId", "borrowerId", "transactionName", "startDate"})
    void createTransactionHead_nullRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("lenderId", 1)
                .put("borrowerId", 2)
                .put("transactionName", "Loan")
                .put("startDate", "2026-01-01");

        payload.putNull(field);

        mockMvc.perform(post("/transactionHeads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value(
                        field.equals("transactionName") ? "NOT_BLANK" : "NOT_NULL"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void createTransactionHead_blankName_returnsValidationError(String transactionName) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("lenderId", 1)
                .put("borrowerId", 2)
                .put("transactionName", transactionName)
                .put("startDate", "2026-01-01");

        mockMvc.perform(post("/transactionHeads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value("transactionName"))
                .andExpect(jsonPath("$.details[0].code").value("NOT_BLANK"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest(name = "create rejects {0} = {1}")
    @CsvSource({
            "startDate, not-a-date",
            "startDate, null",
            "startDate, 2026-13-01",
            "prelEndDate, not-a-date",
            "endDate, not-a-date"
    })
    void createTransactionHead_malformedDate_returnsBadRequest(String field, String value) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("lenderId", 1)
                .put("borrowerId", 2)
                .put("transactionName", "Loan")
                .put("startDate", "2026-01-01");

        // These are invalid date strings, including the literal string "null".
        payload.put(field, value);

        var result = mockMvc.perform(post("/transactionHeads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(payload)));

        verifyNoInteractions(transactionHeadService);
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/transactionHeads"));
    }

    @Test
    void updateTransactionHead_withoutIdOrParticipants_usesPathId() throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", "Updated")
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        mockMvc.perform(put("/transactionHeads/12").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction head updated"));

        verify(transactionHeadService).updateTransactionHead(12, new UpdateTransactionHeadDTO("Updated", null, LocalDate.parse("2026-01-01"), null, null, LocalDateTime.parse("2026-01-01T00:00:00")));
    }

    @ParameterizedTest(name = "update returns {1} {2}")
    @MethodSource("updateErrors")
    void updateTransactionHead_serviceError_returnsApiError(
            RuntimeException exception, int expectedStatus, String expectedCode
    ) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", "Updated")
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        UpdateTransactionHeadDTO request = new UpdateTransactionHeadDTO(
                "Updated", null, LocalDate.parse("2026-01-01"), null, null,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );

        doThrow(exception).when(transactionHeadService).updateTransactionHead(12, request);

        mockMvc.perform(put("/transactionHeads/12").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(payload)))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.path").value("/transactionHeads/12"));

        verify(transactionHeadService).updateTransactionHead(12, request);
        verifyNoMoreInteractions(transactionHeadService);
    }

    static Stream<Arguments> updateErrors() {
        return Stream.of(
                Arguments.of(new ResourceAccessDeniedException(
                        "TRANSACTION_HEAD", 12, "update", "Access denied."
                ), 403, "RESOURCE_ACCESS_DENIED"),
                Arguments.of(new ResourceNotFoundException(
                        "TRANSACTION_HEAD", 12, "Transaction head could not be found."
                ), 404, "RESOURCE_NOT_FOUND"),
                Arguments.of(new ObjectOptimisticLockingFailureException(
                        TransactionHeadDTO.class, 12
                ), 409, "ROW_VERSION_MISMATCH")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionName", "startDate", "rowVersion"})
    void updateTransactionHead_missingRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", "Updated")
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        payload.remove(field);

        mockMvc.perform(put("/transactionHeads/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value(
                        field.equals("transactionName") ? "NOT_BLANK" : "NOT_NULL"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionName", "startDate", "rowVersion"})
    void updateTransactionHead_nullRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", "Updated")
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        payload.putNull(field);

        mockMvc.perform(put("/transactionHeads/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value(
                        field.equals("transactionName") ? "NOT_BLANK" : "NOT_NULL"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void updateTransactionHead_blankName_returnsValidationError(String transactionName) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", transactionName)
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        mockMvc.perform(put("/transactionHeads/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value("transactionName"))
                .andExpect(jsonPath("$.details[0].code").value("NOT_BLANK"));

        verifyNoInteractions(transactionHeadService);
    }

    @ParameterizedTest(name = "update rejects {0} = {1}")
    @CsvSource({
            "startDate, not-a-date",
            "startDate, null",
            "startDate, 2026-13-01",
            "prelEndDate, not-a-date",
            "endDate, not-a-date",
            "rowVersion, not-a-timestamp",
            "rowVersion, null",
            "rowVersion, 2026-01-01T25:00:00"
    })
    void updateTransactionHead_malformedDate_returnsBadRequest(String field, String value) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("transactionName", "Updated")
                .put("startDate", "2026-01-01")
                .put("rowVersion", "2026-01-01T00:00:00");

        payload.put(field, value);

        var result = mockMvc.perform(put("/transactionHeads/12")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(payload)));

        verifyNoInteractions(transactionHeadService);
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/transactionHeads/12"));
    }

    @Test
    void deleteTransactionHead_validPayload_returnsOkAndCallsService() throws Exception {
        DeleteTransactionHeadDTO request = validDeleteTransactionHeadDto(22);
        ObjectNode payload = JSON.createObjectNode()
                .put("id", 22)
                .put("rowVersion", "2026-01-01T00:00:00");

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction head deleted"));

        verify(transactionHeadService).deleteTransactionHead(request);
    }

    @ParameterizedTest(name = "delete returns {1} {2}")
    @MethodSource("deleteErrors")
    void deleteTransactionHead_serviceError_returnsApiError(
            RuntimeException exception, int expectedStatus, String expectedCode
    ) throws Exception {
        DeleteTransactionHeadDTO request = validDeleteTransactionHeadDto(22);
        ObjectNode payload = JSON.createObjectNode()
                .put("id", 22)
                .put("rowVersion", "2026-01-01T00:00:00");

        doThrow(exception).when(transactionHeadService).deleteTransactionHead(request);

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andExpect(jsonPath("$.path").value("/transactionHeads/delete"));

        verify(transactionHeadService).deleteTransactionHead(request);
        verifyNoMoreInteractions(transactionHeadService);
    }

    static Stream<Arguments> deleteErrors() {
        return Stream.of(
                Arguments.of(new ResourceAccessDeniedException(
                        "TRANSACTION_HEAD", 22, "delete", "Access denied."
                ), 403, "RESOURCE_ACCESS_DENIED"),
                Arguments.of(new ResourceNotFoundException(
                        "TRANSACTION_HEAD", 22, "Transaction head could not be found."
                ), 404, "RESOURCE_NOT_FOUND"),
                Arguments.of(new ObjectOptimisticLockingFailureException(
                        TransactionHeadDTO.class, 22
                ), 409, "ROW_VERSION_MISMATCH")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "rowVersion"})
    void deleteTransactionHead_missingRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("id", 22)
                .put("rowVersion", "2026-01-01T00:00:00");

        payload.remove(field);

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value("NOT_NULL"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"id", "rowVersion"})
    void deleteTransactionHead_nullRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("id", 22)
                .put("rowVersion", "2026-01-01T00:00:00");

        payload.putNull(field);

        mockMvc.perform(post("/transactionHeads/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value("NOT_NULL"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-timestamp", "null", "2026-01-01T25:00:00"})
    void deleteTransactionHead_malformedRowVersion_returnsBadRequest(String rowVersion) throws Exception {
        ObjectNode payload = JSON.createObjectNode()
                .put("id", 22)
                .put("rowVersion", rowVersion);

        var result = mockMvc.perform(post("/transactionHeads/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.writeValueAsString(payload)));

        verifyNoInteractions(transactionHeadService);
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/transactionHeads/delete"));
    }

    private TransactionHeadDTO validTransactionHeadDto(int id) {
        return new TransactionHeadDTO(
                id,
                1,
                2,
                id == 10 ? "Loan" : "Delete Head",
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

    private DeleteTransactionHeadDTO validDeleteTransactionHeadDto(int id) {
        return new DeleteTransactionHeadDTO(
                id,
                LocalDateTime.parse("2026-01-01T00:00:00")
        );
    }
}
