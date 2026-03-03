package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TransactionHead", schema = "bank")
public class TransactionHeadEntity {
    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "Lender_Id")
    private int lenderId;

    @Column(name = "Borrower_Id")
    private int borrowerId;

    @Column(name = "TransactionName")
    private String transactionName;

    @Column(name = "Description")
    private String description;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "PrelEndDate")
    private LocalDate prelEndDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

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

    @OneToMany(mappedBy = "transactionHead")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<TransactionRowEntity> transactionRows;
}
