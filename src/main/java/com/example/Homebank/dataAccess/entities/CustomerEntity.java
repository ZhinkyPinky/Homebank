package com.example.Homebank.dataAccess.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Customer", schema = "bank")
public class CustomerEntity {
    @Id
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "TypeOfCustomer_Code")
    private String typeOfCustomerCode;

    @Column(name = "CustomerAmount")
    private int customerAmount;

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;
}
