package com.ecommerce.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Entity
@Table(name = "roles")
public class Role {
    @Getter
    @Setter
    @jakarta.persistence.Id
    private Long id;

    @Column(name = "role_name")
    private String roleName;

    public String getName() {
        return roleName;
    }
}