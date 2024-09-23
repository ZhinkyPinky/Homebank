package com.example.Homebank.dataAccess.entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Immutable
@Data
@Table(name = "vTransactionRow", schema = "bank")
public class TransactionRow {
    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "TransactionHead_Id")
    private long transactionHeadId;

    @Column(name = "TransactionRowNo")
    private int transactionRowNo;

    @Column(name = "TypeOfTransaction_Code")
    private String typeOfTransactionCode;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "PaymentDate")
    private LocalDate paymentDate;

    @Column(name = "Amount")
    private int amount;

    @Column(name = "TransactionName")
    private String transactionName;

    @Column(name = "TypeOfTransaction")
    private String typeOfTransaction;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;
}
