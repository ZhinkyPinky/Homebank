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
}
