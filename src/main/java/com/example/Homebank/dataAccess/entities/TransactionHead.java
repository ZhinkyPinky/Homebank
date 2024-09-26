package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NamedStoredProcedureQuery(
        name = "SaveTransactionHead",
        procedureName = "bank.TransactionHead_Save",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Lender_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Borrower_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TransactionName", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Description", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_StartDate", type = LocalDate.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_PrelEndDate", type = LocalDate.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_EndDate", type = LocalDate.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_RowVersion", type = LocalDateTime.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_RowVersion", type = LocalDateTime.class)
        }
)
@Entity
@Immutable
@Data
@Table(name = "vTransactionHead", schema = "bank")
public class TransactionHead {
    public TransactionHead(){}

    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "Lender_Id")
    private Long lenderId;

    @Column(name = "Borrower_Id")
    private Long borrowerId;

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

    @Column(name = "Borrower")
    private String borrower;

    @Column(name = "Lender")
    private String lender;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLenderId() {
        return lenderId;
    }

    public void setLenderId(Long lenderId) {
        this.lenderId = lenderId;
    }

    public Long getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(Long borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getTransactionName() {
        return transactionName;
    }

    public void setTransactionName(String transactionName) {
        this.transactionName = transactionName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getPrelEndDate() {
        return prelEndDate;
    }

    public void setPrelEndDate(LocalDate prelEndDate) {
        this.prelEndDate = prelEndDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public LocalDateTime getRowVersion() {
        return rowVersion;
    }

    public void setRowVersion(LocalDateTime rowVersion) {
        this.rowVersion = rowVersion;
    }
}
