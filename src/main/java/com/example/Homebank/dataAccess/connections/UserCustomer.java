package com.example.Homebank.dataAccess.connections;

import com.example.Homebank.dataAccess.entities.CustomerEntity;
import com.example.Homebank.dataAccess.entities.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * This class represents the connection between a user and a customer in the database.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"user", "customer"})
@Table(name = "User_Customer", schema = "bank")
public class UserCustomer {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "UserId")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "CustomerId")
    private CustomerEntity customer;

    @Column(name = "RowCreatedBy")
    private String rowCreatedBy;

    @Column(name = "RowCreatedDate")
    private LocalDateTime rowCreatedDate = LocalDateTime.now();

    @Column(name = "RowLastEditBy")
    private String rowLastEditBy;

    @Column(name = "RowLastEditDate")
    private LocalDateTime rowLastEditDate = LocalDateTime.now();

    @Column(name = "RowVersion")
    private LocalDateTime rowVersion;
}
