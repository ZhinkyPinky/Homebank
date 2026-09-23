package com.example.Homebank.presentation.controllers;

import com.example.Homebank.businessLogic.services.TransactionRowService;
import com.example.Homebank.presentation.dto.transactionrow.CreateTransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.UpdateTransactionRowDTO;
import com.example.Homebank.presentation.dto.transactionrow.TransactionRowDTO;
import com.example.Homebank.exceptions.authorization.ResourceAccessDeniedException;
import com.example.Homebank.exceptions.notfound.ResourceNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@ExtendWith(MockitoExtension.class)
class TransactionRowControllerTests {
    @Mock
    private TransactionRowService transactionRowService;

    @InjectMocks
    private TransactionRowController controller;

    private MockMvc mockMvc;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final String CREATE_JSON = """
            {"transactionHeadId":9,"transactionRowNo":1,
             "typeOfTransactionCode":"DEBIT","name":"Payment",
             "paymentDate":"2026-01-15","amount":50}
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void get_existingRow_returnsTransactionRow() throws Exception {
        TransactionRowDTO row = new TransactionRowDTO(
                90, 9, 1, "DEBIT", "Payment", "Monthly payment",
                LocalDate.parse("2026-01-15"), 50,
                LocalDateTime.parse("2026-01-01T00:00:00.1234567"));
        when(transactionRowService.getTransactionRowById(90)).thenReturn(row);

        mockMvc.perform(get("/transactionRows/90"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(90))
                .andExpect(jsonPath("$.transactionHeadId").value(9))
                .andExpect(jsonPath("$.transactionRowNo").value(1))
                .andExpect(jsonPath("$.typeOfTransactionCode").value("DEBIT"))
                .andExpect(jsonPath("$.name").value("Payment"))
                .andExpect(jsonPath("$.description").value("Monthly payment"))
                .andExpect(jsonPath("$.paymentDate").value("2026-01-15"))
                .andExpect(jsonPath("$.amount").value(50))
                .andExpect(jsonPath("$.rowVersion").value("2026-01-01T00:00:00.1234567"));

        verify(transactionRowService).getTransactionRowById(90);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void postTransactionRow_validRequest_returnsSuccess() throws Exception {
        mockMvc.perform(post("/transactionRows").contentType(MediaType.APPLICATION_JSON).content(CREATE_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction row created."));

        verify(transactionRowService).createTransactionRow(new CreateTransactionRowDTO(
                9, 1, "DEBIT", "Payment", null,
                LocalDate.parse("2026-01-15"), 50
        ));
        verifyNoMoreInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void updateTransactionRow_validRequest_usesPathIdAndExcludesParent(boolean includeLegacyFields) throws Exception {
        ObjectNode payload = updatePayload();
        if (includeLegacyFields) {
            payload.put("id", 999).put("transactionHeadId", 999);
        }

        mockMvc.perform(put("/transactionRows/90").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string("Transaction row updated."));

        verify(transactionRowService).updateTransactionRow(90, new UpdateTransactionRowDTO(
                1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50,
                LocalDateTime.parse("2026-01-01T00:00:00")));
        verifyNoMoreInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-01-01T00:00:00", "2026-01-01T00:00:00.1234567"})
    void deleteTransactionRow_validQueryVersion_usesPathIdAndClientVersion(String rowVersion) throws Exception {
        mockMvc.perform(delete("/transactionRows/90").param("rowVersion", rowVersion))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(transactionRowService).deleteTransactionRow(90,
                LocalDateTime.parse(rowVersion));
        verifyNoMoreInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionHeadId", "transactionRowNo", "typeOfTransactionCode", "name", "paymentDate", "amount"})
    void postTransactionRow_missingRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = (ObjectNode) JSON.readTree(CREATE_JSON);
        payload.remove(field);

        mockMvc.perform(post("/transactionRows").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field));
        verifyNoInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionRowNo", "typeOfTransactionCode", "name", "paymentDate", "amount", "rowVersion"})
    void updateTransactionRow_missingRequiredField_returnsValidationError(String field) throws Exception {
        ObjectNode payload = updatePayload();
        payload.remove(field);

        mockMvc.perform(put("/transactionRows/90").contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field));
        verifyNoInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void deleteTransactionRow_missingQueryVersion_returnsBadRequest(boolean includeLegacyBody) throws Exception {
        var request = delete("/transactionRows/90");
        if (includeLegacyBody) {
            request.contentType(MediaType.APPLICATION_JSON).content("{\"rowVersion\":\"2026-01-01T00:00:00\"}");
        }
        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/transactionRows/90"));
        verifyNoInteractions(transactionRowService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "null", "not-a-timestamp", "2026-01-01", "2026-01-01T25:00:00", "2026-02-30T00:00:00"})
    void deleteTransactionRow_invalidQueryVersion_returnsBadRequest(String rowVersion) throws Exception {
        mockMvc.perform(delete("/transactionRows/90").param("rowVersion", rowVersion))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/transactionRows/90"));
        verifyNoInteractions(transactionRowService);
    }

    @Test
    void get_accessDenied_returnsForbidden() throws Exception {
        doThrow(new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied")).when(transactionRowService).getTransactionRowById(90);

        assertApiError(get("/transactionRows/90"), 403, "RESOURCE_ACCESS_DENIED");

        verify(transactionRowService).getTransactionRowById(90);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void get_resourceMissing_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("TRANSACTION_ROW", 90, "Missing")).when(transactionRowService).getTransactionRowById(90);

        assertApiError(get("/transactionRows/90"), 404, "RESOURCE_NOT_FOUND");

        verify(transactionRowService).getTransactionRowById(90);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void postTransactionRow_accessDenied_returnsForbidden() throws Exception {
        CreateTransactionRowDTO request = new CreateTransactionRowDTO(
                9, 1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50);
        doThrow(new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied")).when(transactionRowService).createTransactionRow(request);

        assertApiError(post("/transactionRows").contentType(MediaType.APPLICATION_JSON).content(CREATE_JSON), 403, "RESOURCE_ACCESS_DENIED");

        verify(transactionRowService).createTransactionRow(request);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void postTransactionRow_resourceMissing_returnsNotFound() throws Exception {
        CreateTransactionRowDTO request = new CreateTransactionRowDTO(
                9, 1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50);
        doThrow(new ResourceNotFoundException("TRANSACTION_HEAD", 9, "Missing")).when(transactionRowService).createTransactionRow(request);

        assertApiError(post("/transactionRows").contentType(MediaType.APPLICATION_JSON).content(CREATE_JSON), 404, "RESOURCE_NOT_FOUND");

        verify(transactionRowService).createTransactionRow(request);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void updateTransactionRow_accessDenied_returnsForbidden() throws Exception {
        UpdateTransactionRowDTO request = new UpdateTransactionRowDTO(
                1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50,
                LocalDateTime.parse("2026-01-01T00:00:00"));
        doThrow(new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied")).when(transactionRowService).updateTransactionRow(90, request);

        assertApiError(put("/transactionRows/90").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(updatePayload())), 403, "RESOURCE_ACCESS_DENIED");

        verify(transactionRowService).updateTransactionRow(90, request);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void updateTransactionRow_resourceMissing_returnsNotFound() throws Exception {
        UpdateTransactionRowDTO request = new UpdateTransactionRowDTO(
                1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50,
                LocalDateTime.parse("2026-01-01T00:00:00"));
        doThrow(new ResourceNotFoundException("TRANSACTION_ROW", 90, "Missing")).when(transactionRowService).updateTransactionRow(90, request);

        assertApiError(put("/transactionRows/90").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(updatePayload())), 404, "RESOURCE_NOT_FOUND");

        verify(transactionRowService).updateTransactionRow(90, request);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void updateTransactionRow_staleVersion_returnsConflict() throws Exception {
        UpdateTransactionRowDTO request = new UpdateTransactionRowDTO(
                1, "DEBIT", "Payment", null, LocalDate.parse("2026-01-15"), 50,
                LocalDateTime.parse("2026-01-01T00:00:00"));
        doThrow(new ObjectOptimisticLockingFailureException("TransactionRow", 90)).when(transactionRowService).updateTransactionRow(90, request);

        assertApiError(put("/transactionRows/90").contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(updatePayload())), 409, "ROW_VERSION_MISMATCH");

        verify(transactionRowService).updateTransactionRow(90, request);
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void deleteTransactionRow_accessDenied_returnsForbidden() throws Exception {
        doThrow(new ResourceAccessDeniedException("TRANSACTION_HEAD", 9, "read", "Denied")).when(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));

        assertApiError(delete("/transactionRows/90").param("rowVersion", "2026-01-01T00:00:00"), 403, "RESOURCE_ACCESS_DENIED");

        verify(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void deleteTransactionRow_resourceMissing_returnsNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("TRANSACTION_ROW", 90, "Missing")).when(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));

        assertApiError(delete("/transactionRows/90").param("rowVersion", "2026-01-01T00:00:00"), 404, "RESOURCE_NOT_FOUND");

        verify(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));
        verifyNoMoreInteractions(transactionRowService);
    }

    @Test
    void deleteTransactionRow_staleVersion_returnsConflict() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException("TransactionRow", 90)).when(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));

        assertApiError(delete("/transactionRows/90").param("rowVersion", "2026-01-01T00:00:00"), 409, "ROW_VERSION_MISMATCH");

        verify(transactionRowService).deleteTransactionRow(90, LocalDateTime.parse("2026-01-01T00:00:00"));
        verifyNoMoreInteractions(transactionRowService);
    }

    @ParameterizedTest
    @MethodSource("blankFields")
    void postTransactionRow_blankField_returnsValidationError(String field, String value) throws Exception {
        assertValidationError(post("/transactionRows"), createPayload().put(field, value), field, "NOT_BLANK");
    }

    @Test
    void postTransactionRow_negativeAmount_returnsValidationError() throws Exception {
        assertValidationError(post("/transactionRows"), createPayload().put("amount", -1), "amount", "POSITIVE_OR_ZERO");
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionHeadId", "transactionRowNo", "typeOfTransactionCode", "name", "paymentDate", "amount"})
    void postTransactionRow_nullField_returnsValidationError(String field) throws Exception {
        assertValidationError(post("/transactionRows"), createPayload().putNull(field), field,
                field.equals("name") || field.equals("typeOfTransactionCode") ? "NOT_BLANK" : "NOT_NULL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-date", "null", "2026-13-01"})
    void postTransactionRow_malformedPaymentDate_returnsBadRequest(String value) throws Exception {
        assertBadRequest(post("/transactionRows"), JSON.writeValueAsString(createPayload().put("paymentDate", value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{broken"})
    void postTransactionRow_missingOrMalformedBody_returnsBadRequest(String body) throws Exception {
        assertBadRequest(post("/transactionRows"), body);
    }

    @ParameterizedTest
    @MethodSource("blankFields")
    void updateTransactionRow_blankField_returnsValidationError(String field, String value) throws Exception {
        assertValidationError(put("/transactionRows/90"), updatePayload().put(field, value), field, "NOT_BLANK");
    }

    @Test
    void updateTransactionRow_negativeAmount_returnsValidationError() throws Exception {
        assertValidationError(put("/transactionRows/90"), updatePayload().put("amount", -1), "amount", "POSITIVE_OR_ZERO");
    }

    @ParameterizedTest
    @ValueSource(strings = {"transactionRowNo", "typeOfTransactionCode", "name", "paymentDate", "amount", "rowVersion"})
    void updateTransactionRow_nullField_returnsValidationError(String field) throws Exception {
        assertValidationError(put("/transactionRows/90"), updatePayload().putNull(field), field,
                field.equals("name") || field.equals("typeOfTransactionCode") ? "NOT_BLANK" : "NOT_NULL");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-date", "null", "2026-13-01"})
    void updateTransactionRow_malformedPaymentDate_returnsBadRequest(String value) throws Exception {
        assertBadRequest(put("/transactionRows/90"), JSON.writeValueAsString(updatePayload().put("paymentDate", value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "{broken"})
    void updateTransactionRow_missingOrMalformedBody_returnsBadRequest(String body) throws Exception {
        assertBadRequest(put("/transactionRows/90"), body);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-a-timestamp", "null", "2026-01-01T25:00:00"})
    void updateTransactionRow_malformedRowVersion_returnsBadRequest(String value) throws Exception {
        assertBadRequest(put("/transactionRows/90"),
                JSON.writeValueAsString(updatePayload().put("rowVersion", value)));
    }

    static Stream<Arguments> blankFields() {
        return Stream.of("name", "typeOfTransactionCode").flatMap(field ->
                Stream.of("", "   ").map(value -> Arguments.of(field, value)));
    }

    private void assertValidationError(MockHttpServletRequestBuilder request, ObjectNode payload, String field, String constraint) throws Exception {
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.details.length()").value(1))
                .andExpect(jsonPath("$.details[0].field").value(field))
                .andExpect(jsonPath("$.details[0].code").value(constraint));
        verifyNoInteractions(transactionRowService);
    }

    private void assertBadRequest(MockHttpServletRequestBuilder request, String body) throws Exception {
        assertApiError(request.contentType(MediaType.APPLICATION_JSON).content(body), 400, "BAD_REQUEST");
        verifyNoInteractions(transactionRowService);
    }

    private void assertApiError(MockHttpServletRequestBuilder request, int expectedStatus, String expectedCode) throws Exception {
        var result = mockMvc.perform(request)
                .andExpect(status().is(expectedStatus))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.code").value(expectedCode))
                .andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(result.getRequest().getRequestURI(),
                JSON.readTree(result.getResponse().getContentAsString()).get("path").asText());
    }

    private ObjectNode createPayload() {
        return (ObjectNode) JSON.readTree(CREATE_JSON);
    }

    private ObjectNode updatePayload() {
        ObjectNode payload = (ObjectNode) JSON.readTree(CREATE_JSON);
        payload.remove("transactionHeadId");
        return payload.put("rowVersion", "2026-01-01T00:00:00");
    }

    @Test
    void postTransactionRow_legacySavePath_returnsMethodNotAllowed() throws Exception {
        var result = mockMvc.perform(post("/transactionRows/save")
                        .contentType(MediaType.APPLICATION_JSON).content(CREATE_JSON))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"))
                .andExpect(jsonPath("$.path").value("/transactionRows/save"))
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(header().string("Allow", containsString("PUT")))
                .andExpect(header().string("Allow", containsString("DELETE")))
                .andReturn();

        assertNull(result.getHandler());
        verifyNoInteractions(transactionRowService);
    }
}
