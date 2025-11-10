package com.adavie.router;

import com.adavie.router.function.QuadFunction;
import com.adavie.router.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Route {
  private final Object handler;
  private final RouteType type;
  private final TypeToken<?>[] paramTypeTokens;
  private final TypeToken<?> returnTypeToken;
  private final Class<?>[] rawParamTypes;
  private final Class<?> rawReturnType;

  private Route(Object handler,
                RouteType type,
                TypeToken<?>[] paramTypeTokens,
                TypeToken<?> returnTypeToken) {
    this.handler = handler;
    this.type = type;
    this.paramTypeTokens = paramTypeTokens;
    this.returnTypeToken = returnTypeToken;

    this.rawParamTypes = new Class<?>[paramTypeTokens.length];
    for (int i = 0; i < paramTypeTokens.length; i++) {
      this.rawParamTypes[i] = paramTypeTokens[i].getRawType();
    }
    this.rawReturnType = returnTypeToken.getRawType();
  }

  public static <R> Route of(Supplier<R> supplier, TypeToken<R> returnType) {
    return new Route(
        supplier,
        RouteType.NO_ARG,
        new TypeToken<?>[0],
        returnType
    );
  }

  public static <T, R> Route of(Function<T, R> function,
                                 TypeToken<T> paramType,
                                 TypeToken<R> returnType) {
    return new Route(
        function,
        RouteType.ONE_ARG,
        new TypeToken<?>[]{paramType},
        returnType
    );
  }

  public static <T1, T2, R> Route of(BiFunction<T1, T2, R> biFunction,
                                      TypeToken<T1> param1Type,
                                      TypeToken<T2> param2Type,
                                      TypeToken<R> returnType) {
    return new Route(
        biFunction,
        RouteType.TWO_ARG,
        new TypeToken<?>[]{param1Type, param2Type},
        returnType
    );
  }

  public static <T1, T2, T3, R> Route of(TriFunction<T1, T2, T3, R> triFunction,
                                          TypeToken<T1> param1Type,
                                          TypeToken<T2> param2Type,
                                          TypeToken<T3> param3Type,
                                          TypeToken<R> returnType) {
    return new Route(
        triFunction,
        RouteType.THREE_ARG,
        new TypeToken<?>[]{param1Type, param2Type, param3Type},
        returnType
    );
  }

  public static <T1, T2, T3, T4, R> Route of(QuadFunction<T1, T2, T3, T4, R> quadFunction,
                                              TypeToken<T1> param1Type,
                                              TypeToken<T2> param2Type,
                                              TypeToken<T3> param3Type,
                                              TypeToken<T4> param4Type,
                                              TypeToken<R> returnType) {
    return new Route(
        quadFunction,
        RouteType.FOUR_ARG,
        new TypeToken<?>[]{param1Type, param2Type, param3Type, param4Type},
        returnType
    );
  }

  @SuppressWarnings("unchecked")
  public static <R> Route of(Supplier<R> supplier) {
    TypeToken<Object> objectToken = new TypeToken<Object>() {};
    return new Route(
        supplier,
        RouteType.NO_ARG,
        new TypeToken<?>[0],
        objectToken
    );
  }

  @SuppressWarnings("unchecked")
  public static <T, R> Route of(Function<T, R> function) {
    TypeToken<Object> objectToken = new TypeToken<Object>() {};
    return new Route(
        function,
        RouteType.ONE_ARG,
        new TypeToken<?>[]{objectToken},
        objectToken
    );
  }

  @SuppressWarnings("unchecked")
  public static <T1, T2, R> Route of(BiFunction<T1, T2, R> biFunction) {
    TypeToken<Object> objectToken = new TypeToken<Object>() {};
    return new Route(
        biFunction,
        RouteType.TWO_ARG,
        new TypeToken<?>[]{objectToken, objectToken},
        objectToken
    );
  }

  @SuppressWarnings("unchecked")
  public static <T1, T2, T3, R> Route of(TriFunction<T1, T2, T3, R> triFunction) {
    TypeToken<Object> objectToken = new TypeToken<Object>() {};
    return new Route(
        triFunction,
        RouteType.THREE_ARG,
        new TypeToken<?>[]{objectToken, objectToken, objectToken},
        objectToken
    );
  }

  @SuppressWarnings("unchecked")
  public static <T1, T2, T3, T4, R> Route of(QuadFunction<T1, T2, T3, T4, R> quadFunction) {
    TypeToken<Object> objectToken = new TypeToken<Object>() {};
    return new Route(
        quadFunction,
        RouteType.FOUR_ARG,
        new TypeToken<?>[]{objectToken, objectToken, objectToken, objectToken},
        objectToken
    );
  }

  public Object invoke(Object... args) throws RouteInvocationException {
    validateArgumentCount(args);
    validateArgumentTypes(args);

    try {
      return invokeHandler(args);
    } catch (ClassCastException e) {
      throw new RouteInvocationException(
          "Type mismatch during invocation: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  public <R> R invokeTyped(Class<R> expectedReturnType, Object... args)
      throws RouteInvocationException {
    Object result = invoke(args);

    if (result == null) {
      return null;
    }

    if (!expectedReturnType.isInstance(result)) {
      throw new RouteInvocationException(
          "Expected return type " + expectedReturnType.getName() +
              " but got " + result.getClass().getName());
    }

    return (R) result;
  }

  @SuppressWarnings("unchecked")
  public <R> R invokeTyped(TypeToken<R> expectedReturnType, Object... args)
      throws RouteInvocationException {
    Object result = invoke(args);

    if (result == null) {
      return null;
    }

    if (!expectedReturnType.isInstance(result)) {
      throw new RouteInvocationException(
          "Expected return type " + expectedReturnType +
              " but got " + result.getClass().getName());
    }

    return (R) result;
  }

  private void validateArgumentCount(Object[] args) throws RouteInvocationException {
    if (args.length != type.getParamCount()) {
      throw new RouteInvocationException(
          "Expected " + type.getParamCount() + " arguments, got " + args.length);
    }
  }

  private void validateArgumentTypes(Object[] args) throws RouteInvocationException {
    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        // Null is valid for non-primitive types
        if (rawParamTypes[i].isPrimitive()) {
          throw new RouteInvocationException(
              "Parameter " + i + " is primitive type " +
                  rawParamTypes[i].getName() + " and cannot be null");
        }
        continue;
      }

      if (!paramTypeTokens[i].isInstance(args[i])) {
        throw new RouteInvocationException(
            "Parameter " + i + " expected type " + paramTypeTokens[i] +
                " but got " + args[i].getClass().getName());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Object invokeHandler(Object[] args) {
    switch (type) {
      case NO_ARG:
        return ((Supplier<?>) handler).get();
      case ONE_ARG:
        return ((Function<Object, ?>) handler).apply(args[0]);
      case TWO_ARG:
        return ((BiFunction<Object, Object, ?>) handler).apply(args[0], args[1]);
      case THREE_ARG:
        return ((TriFunction<Object, Object, Object, ?>) handler)
            .apply(args[0], args[1], args[2]);
      case FOUR_ARG:
        return ((QuadFunction<Object, Object, Object, Object, ?>) handler)
            .apply(args[0], args[1], args[2], args[3]);
      default:
        throw new IllegalStateException("Unsupported route type: " + type);
    }
  }

  public RouteType getType() {
    return type;
  }

  public int getParamCount() {
    return type.getParamCount();
  }

  public TypeToken<?>[] getParamTypeTokens() {
    return paramTypeTokens;
  }

  public TypeToken<?> getReturnTypeToken() {
    return returnTypeToken;
  }

  public Class<?>[] getRawParamTypes() {
    return rawParamTypes;
  }

  public Class<?> getRawReturnType() {
    return rawReturnType;
  }
}
