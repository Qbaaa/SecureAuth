package com.qbaaa.secure.auth.auth.infrastructure.entity;

import com.qbaaa.secure.auth.shared.audit.entity.AuditDataEntity;
import com.qbaaa.secure.auth.user.infrastructure.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "session", schema = "secureauth")
@Getter
@Setter
public class SessionEntity extends AuditDataEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UUID sessionToken;

  private String ipAddress;

  private String userAgent;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;
}
