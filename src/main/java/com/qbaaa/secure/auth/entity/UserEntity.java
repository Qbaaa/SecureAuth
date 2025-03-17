package com.qbaaa.secure.auth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "user", schema = "secureauth")
@Getter
@Setter
public class UserEntity extends AuditDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private Boolean isActive;

    private Boolean isVerified;

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private PasswordEntity password;

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private RefreshTokenEntity refreshToken;

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    private EmailVerificationTokenEntity emailVerificationToken;

    @ManyToOne
    @JoinColumn(name = "domain_id", nullable = false)
    private DomainEntity domain;

    @OneToMany(mappedBy = "user")
    private List<SessionEntity> sessions;

    @ManyToMany(mappedBy = "users")
    private List<RoleEntity> roles;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserEntity that)) return false;
        return isActive == that.isActive && isVerified == that.isVerified && Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) && Objects.equals(email, that.email) &&
                Objects.equals(password, that.password) && Objects.equals(refreshToken, that.refreshToken) &&
                Objects.equals(emailVerificationToken, that.emailVerificationToken) && Objects.equals(domain, that.domain) &&
                Objects.equals(sessions, that.sessions) && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, isActive, isVerified, password, refreshToken, emailVerificationToken, domain, sessions, roles);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", isVerified=" + isVerified +
                ", password=" + password +
                '}';
    }
}
