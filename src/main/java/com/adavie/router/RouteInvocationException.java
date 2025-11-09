package com.adavie.router;

/**
 * Exception thrown when a route invocation fails due to:
 * - Incorrect number of arguments
 * - Type mismatches
 * - Route not found
 * - Other invocation errors
 */
public class RouteInvocationException extends Exception {
  public RouteInvocationException(String message) {
    super(message);
  }

  public RouteInvocationException(String message, Throwable cause) {
    super(message, cause);
  }
}
