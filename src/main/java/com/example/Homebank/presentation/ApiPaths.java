package com.example.Homebank.presentation;

public class ApiPaths {
    public static final String AUTH = "/auth";
    public static final String LOGIN = "/login";
    public static final String SIGN_OUT = "/logout";
    public static final String REGISTER = "/register";
    public static final String REFRESH = "/refresh";

    public static final String SAVE = "/save";
    public static final String DELETE = "/delete";

    public static final String TRANSACTION_ROWS = "/transactionRows";
    public static final String TRANSACTION_ROW = "/{transactionRowId}";
    public static final String SAVE_TRANSACTION_ROW = TRANSACTION_ROW + SAVE;

    public static final String TRANSACTION_HEADS = "/transactionHeads";
    public static final String TRANSACTION_HEAD = "/{transactionHeadId}";
    public static final String SAVE_TRANSACTION_HEAD = TRANSACTION_HEADS + SAVE;

    public static final String CUSTOMERS = "/customers";
    public static final String CUSTOMER = "/{customerId}";
    public static final String CUSTOMER_WITH_TRANSACTION_HEADS = CUSTOMER + TRANSACTION_HEADS;
    public static final String CUSTOMER_WITH_TRANSACTION_HEAD = CUSTOMER + TRANSACTION_HEADS + TRANSACTION_HEAD;
    public static final String CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROWS = CUSTOMER_WITH_TRANSACTION_HEAD + TRANSACTION_ROWS;
    public static final String CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROW = CUSTOMER_WITH_TRANSACTION_HEAD_AND_ROWS + TRANSACTION_ROW;
}
