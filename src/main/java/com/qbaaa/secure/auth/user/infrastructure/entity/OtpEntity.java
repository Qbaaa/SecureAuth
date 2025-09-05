package com.qbaaa.secure.auth.user.infrastructure.entity;

import com.qbaaa.secure.auth.user.domain.enums.OperationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "otp", schema = "secureauth")
@Getter
@Setter
public class OtpEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(nullable = false)
  private Long id;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false)
  private OperationType operationType;

  @Column(nullable = false)
  private String secret;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;
}
