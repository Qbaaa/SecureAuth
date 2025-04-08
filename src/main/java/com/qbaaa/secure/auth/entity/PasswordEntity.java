package com.qbaaa.secure.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "password", schema = "secureauth")
@Getter
@Setter
public class PasswordEntity extends AuditDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String password;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PasswordEntity that)) {
      return false;
    }
    return Objects.equals(id, that.id)
        && Objects.equals(password, that.password)
        && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, user);
  }

  @Override
  public String toString() {
    return "PasswordEntity{" + "id=" + id + ", password='" + password + '\'' + '}';
  }
}
