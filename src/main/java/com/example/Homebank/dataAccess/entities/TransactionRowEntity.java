package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TransactionRow", schema = "bank")
public class TransactionRowEntity {
    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "TransactionRowNo")
    private int transactionRowNo;

    @Column(name = "TypeOfTransaction_Code")
    @Enumerated(EnumType.STRING)
    private TypeOfTransaction typeOfTransactionCode;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "PaymentDate")
    private LocalDate paymentDate;

    @Column(name = "Amount")
    private int amount;

    @Column(name = "DeleteDate")
    private LocalDate deleteDate;

    @Column(name = "RowCreatedBy")
    private String rowCreatedBy;

    @Column(name = "RowCreatedDate")
    private LocalDateTime rowCreateDate;

    @Column(name = "RowLastEditBy")
    private String rowLastEditBy;

    @Column(name = "RowLastEditDate")
    private LocalDateTime rowLastEditDate;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TransactionHead_Id", referencedColumnName = "Id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TransactionHeadEntity transactionHead;

    @Transient
    public int getTransactionHeadId() {
        return transactionHead != null ? transactionHead.getId() : 0;
    }
}
