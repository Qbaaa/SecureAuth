package com.qbaaa.secure.auth.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "user",
    schema = "secureauth",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"domain_id", "username"}),
      @UniqueConstraint(columnNames = {"domain_id", "email"})
    })
@Getter
@Setter
public class UserEntity extends AuditDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String email;

  private Boolean isActive;

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

  @ManyToMany(
      cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private List<RoleEntity> roles;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UserEntity that)) return false;
    return isActive == that.isActive
        && Objects.equals(id, that.id)
        && Objects.equals(username, that.username)
        && Objects.equals(email, that.email)
        && Objects.equals(password, that.password)
        && Objects.equals(refreshToken, that.refreshToken)
        && Objects.equals(emailVerificationToken, that.emailVerificationToken)
        && Objects.equals(domain, that.domain)
        && Objects.equals(sessions, that.sessions)
        && Objects.equals(roles, that.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        username,
        email,
        isActive,
        password,
        refreshToken,
        emailVerificationToken,
        domain,
        sessions,
        roles);
  }

  @Override
  public String toString() {
    return "UserEntity{"
        + "id="
        + id
        + ", username='"
        + username
        + '\''
        + ", email='"
        + email
        + '\''
        + ", isActive="
        + isActive
        + ", password="
        + password
        + '}';
  }
}
