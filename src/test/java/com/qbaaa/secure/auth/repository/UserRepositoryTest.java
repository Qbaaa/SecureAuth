package com.qbaaa.secure.auth.repository;

import com.qbaaa.secure.auth.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepositoryTest extends JpaRepository<UserEntity, Long> {

  @Query(
      """
                select count(u)
                from UserEntity u
                where u.domain.name = :domainName
                """)
  long countByDomainName(String domainName);

  @Query(
      """
                select u
                from UserEntity u
                left join fetch u.roles r
                join fetch u.password
                where u.domain.name = :domainName
                and u.username = :username
                """)
  Optional<UserEntity> findByDomainNameAndUsername(String domainName, String username);
}
