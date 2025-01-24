package com.example.Homebank.dataAccess.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NamedStoredProcedureQuery(
        name = "SaveTransactionRow",
        procedureName = "bank.TransactionRow_Save",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TransactionHead_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TransactionRowNo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TypeOfTransaction_Code", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Name", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Description", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_PaymentDate", type = LocalDate.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Amount", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_RowVersion", type = LocalDateTime.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_RowVersion", type = LocalDateTime.class)
        }
)
@NamedStoredProcedureQuery(
        name = "DeleteTransactionRow",
        procedureName = "bank.TransactionRow_Delete",
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TransactionHead_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_TransactionRowNo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "p_RowVersion", type = LocalDateTime.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_Id", type = Long.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "p_OUT_RowVersion", type = LocalDateTime.class)
        }
)
@Entity
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vTransactionRow", schema = "bank")
public class TransactionRowEntity {
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
