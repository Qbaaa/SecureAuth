package com.qbaaa.secure.auth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "domain", schema = "secureauth")
@Getter
@Setter
public class DomainEntity extends AuditDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private Integer accessTokenValidity;

    private Integer refreshTokenValidity;

    private Integer emailTokenValidity;

    private Integer sessionValidity;

    @OneToMany(mappedBy = "domain", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<UserEntity> users = new ArrayList<>();

    @OneToMany(mappedBy = "domain", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<RoleEntity> roles = new ArrayList<>();

    @OneToOne(mappedBy = "domain", orphanRemoval = true, cascade = CascadeType.ALL)
    private KeyEntity key;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DomainEntity that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(accessTokenValidity, that.accessTokenValidity) &&
                Objects.equals(refreshTokenValidity, that.refreshTokenValidity) && Objects.equals(emailTokenValidity, that.emailTokenValidity) &&
                Objects.equals(sessionValidity, that.sessionValidity) && Objects.equals(users, that.users) &&
                Objects.equals(roles, that.roles) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, accessTokenValidity, refreshTokenValidity, emailTokenValidity, sessionValidity, users, roles, key);
    }

    @Override
    public String toString() {
        return "DomainEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accessTokenValidity=" + accessTokenValidity +
                ", refreshTokenValidity=" + refreshTokenValidity +
                ", emailTokenValidity=" + emailTokenValidity +
                ", sessionValidity=" + sessionValidity +
                '}';
    }
}
