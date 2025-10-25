package com.adavie.request.model;

public enum HttpStatusCodes {
  CONTINUE(100),
  OK(200),
  CREATED(201),
  PERMANENT_REDIRECT(300),
  BAD_REQUEST(400),
  UNAUTHORIZED(401),
  NOT_FOUND(404),
  REQUEST_ENTITY_TOO_LARGE(413),
  URI_TOO_LONG(414),
  INTERNAL_SERVER_ERROR(500),
  HTTP_VERSION_NOT_SUPPORTED(505);

  private final int code;

  private HttpStatusCodes(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
