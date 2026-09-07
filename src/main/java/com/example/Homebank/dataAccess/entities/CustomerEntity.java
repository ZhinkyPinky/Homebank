package com.example.Homebank.dataAccess.entities;

import com.example.Homebank.dataAccess.connections.UserCustomer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a customer in the banking application.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"owner", "userCustomers"})
@Table(name = "Customer", schema = "bank")
public class CustomerEntity {
    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @Column(name = "Name")
    private String name;

    @Column(name = "Description")
    private String description;

    @Column(name = "TypeOfCustomer_Code")
    @Enumerated(EnumType.STRING)
    private TypeOfCustomer typeOfCustomerCode = TypeOfCustomer.KONTO;

    @ManyToOne
    @JoinColumn(name = "Owner_UserId")
    private UserEntity owner;

    @Column(name = "RowCreatedBy")
    private String rowCreatedBy;

    @Column(name = "RowCreatedDate")
    private LocalDateTime rowCreatedDate = LocalDateTime.now();

    @Column(name = "RowLastEditBy")
    private String rowLastEditBy;

    @Column(name = "RowLastEditDate")
    private LocalDateTime rowLastEditDate = LocalDateTime.now();

    @Version
    @Column(name = "RowVersion", nullable = false)
    private LocalDateTime rowVersion;

    @OneToMany(mappedBy = "customer")
    private Set<UserCustomer> userCustomers = new HashSet<>();
}
