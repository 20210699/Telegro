package com.telegro.telegro.global.auth.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class JWKSetKeys {

  private List<Key> keys;
  @Getter
  class Key {
    private String alg;
    private String e;
    private String kid;
    private String kty;
    private String n;
    private String use;
  }
}
