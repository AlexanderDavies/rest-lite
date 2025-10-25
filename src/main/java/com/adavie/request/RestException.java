package com.adavie.request;

import com.adavie.request.model.HttpStatusCodes;

public class RestException extends Exception {

  private final String message;
  private final HttpStatusCodes statusCode;

  public RestException(HttpStatusCodes statusCode, String message) {
    super(message);
    this.statusCode = statusCode;
    this.message = message;
  }

  public RestException(HttpStatusCodes statusCode, String message, Throwable cause) {
    super(message, cause);
    this.statusCode = statusCode;
    this.message = message;
  }

  public RestException(Exception exception) {
    super(exception);
    this.statusCode = HttpStatusCodes.INTERNAL_SERVER_ERROR;
    this.message = exception.getMessage();
  }

  @Override
  public String getMessage() {
    return message;
  }

  public HttpStatusCodes getStatusCode() {
    return statusCode;
  }

  public int getStatusCodeValue() {
    return statusCode.getCode();
  }

}
