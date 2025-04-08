package com.qbaaa.secure.auth.projection;

import java.util.List;

public interface UserProjection {
  String getUsername();

  String getEmail();

  List<String> getRoles();
}
