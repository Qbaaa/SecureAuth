package com.qbaaa.secure.auth.user.infrastructure.projection;

import java.util.List;

public interface UserProjection {
  String getUsername();

  String getEmail();

  List<String> getRoles();
}
