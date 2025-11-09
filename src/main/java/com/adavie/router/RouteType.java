package com.adavie.router;

public enum RouteType {
  NO_ARG(0),
  ONE_ARG(1),
  TWO_ARG(2),
  THREE_ARG(3),
  FOUR_ARG(4);

  private final int paramCount;

  RouteType(int paramCount) {
    this.paramCount = paramCount;
  }

  public int getParamCount() {
    return paramCount;
  }
}
