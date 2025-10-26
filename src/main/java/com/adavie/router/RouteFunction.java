package com.adavie.router;

@FunctionalInterface
public interface RouteFunction<R> {
  R apply(Object... args);
}
