package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "TypeOfUser", schema = "BankLookup")
public class TypeOfUser {
    @Id
    @Column(name = "Code")
    private String code;

    @Column(name = "Value")
    private String value;

    @Column(name = "Description")
    private String description;

    @Column(name = "SortOrder")
    private int sortOrder;

    @Column(name = "RowCreatedBy")
    private String rowCreatedBy;

    @Column(name = "RowCreatedDate")
    private LocalDateTime rowCreatedDate;

    @Column(name = "RowLastEditBy")
    private String rowLastEditBy;

    @Column(name = "RowLastEditDate")
    private LocalDateTime rowLastEditDate;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;
}
