package com.example.Homebank.presentation;

public class ApiPaths {
    public static final String AUTH_BASE_PATH = "/auth";
    public static final String LOGIN_PATH = "/login";
    public static final String REGISTER_PATH = "/register";
    public static final String REFRESH_PATH = "/refresh";

    public static final String SAVE = "/save";
    public static final String DELETE = "/delete";

    public static final String TRANSACTION_ROW_BASE_PATH = "/transactionRows";
    public static final String TRANSACTION_ROW_PATH = "/{transactionRowId}";

    public static final String TRANSACTION_HEADS_BASE_PATH = "/transactionHeads";
    public static final String TRANSACTION_HEAD_PATH = "/{transactionHeadId}";

    public static final String CUSTOMERS_BASE_PATH = "/customers";
    public static final String CUSTOMER_PATH = "/{customerId}";
    public static final String CUSTOMER_AND_TRANSACTION_HEADS_PATH = CUSTOMER_PATH + TRANSACTION_HEADS_BASE_PATH;
    public static final String CUSTOMER_AND_TRANSACTION_HEAD_PATH = CUSTOMER_PATH + TRANSACTION_HEADS_BASE_PATH + TRANSACTION_HEAD_PATH;
    public static final String CUSTOMER_AND_TRANSACTION_HEAD_AND_ROWS_PATH = CUSTOMER_AND_TRANSACTION_HEAD_PATH + TRANSACTION_ROW_BASE_PATH;
    public static final String CUSTOMER_AND_TRANSACTION_HEAD_AND_ROW_PATH = CUSTOMER_AND_TRANSACTION_HEAD_AND_ROWS_PATH + TRANSACTION_ROW_PATH;
}
