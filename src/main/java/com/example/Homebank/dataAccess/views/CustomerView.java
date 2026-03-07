package com.example.Homebank.dataAccess.views;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a view of customer data, including aggregated information such as the total amount associated with the customer.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "vCustomer", schema = "bank")
public class CustomerView {
    @Id
    @Column(name = "Id")
    private int id;

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
