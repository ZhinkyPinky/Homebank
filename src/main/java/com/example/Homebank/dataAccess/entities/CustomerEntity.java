package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Customer", schema = "bank")
public class CustomerEntity {
    @Id
    @Column(name = "Id")
    private int id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "TypeOfCustomer_Code")
    @Enumerated(EnumType.STRING)
    private TypeOfCustomer typeOfCustomerCode;

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

    @ManyToMany(mappedBy = "customers")
    private Set<UserEntity> users;
}
