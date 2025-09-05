package com.qbaaa.secure.auth.domain.infrastructure.entity;

import com.qbaaa.secure.auth.shared.audit.entity.AuditDataEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "key", schema = "secureauth")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeyEntity extends AuditDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String algorithm;

  @Column(nullable = false)
  private String publicKey;

  @Column(nullable = false)
  private String privateKey;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "domain_id", nullable = false)
  private DomainEntity domain;

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof KeyEntity keyEntity)) {
      return false;
    }
    return Objects.equals(id, keyEntity.id)
        && Objects.equals(algorithm, keyEntity.algorithm)
        && Objects.equals(publicKey, keyEntity.publicKey)
        && Objects.equals(privateKey, keyEntity.privateKey)
        && Objects.equals(domain, keyEntity.domain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, algorithm, publicKey, privateKey, domain);
  }

  @Override
  public String toString() {
    return "KeyEntity{"
        + "privateKey='"
        + privateKey
        + '\''
        + ", publicKey='"
        + publicKey
        + '\''
        + ", algorithm='"
        + algorithm
        + '\''
        + ", id="
        + id
        + '}';
  }
}
