package com.adavie.router;

import com.adavie.router.function.QuadFunction;
import com.adavie.router.function.TriFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class Routes {
  private final Map<String, Route> routesMap = new HashMap<>();

  public Routes() {
  }

  public <R> void add(String path,
                      Supplier<R> supplier,
                      TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(supplier, returnType));
  }

  public <T, R> void add(String path,
                         Function<T, R> function,
                         TypeToken<T> paramType,
                         TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(function, paramType, returnType));
  }

  public <T1, T2, R> void add(String path,
                              BiFunction<T1, T2, R> biFunction,
                              TypeToken<T1> param1Type,
                              TypeToken<T2> param2Type,
                              TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(biFunction, param1Type, param2Type, returnType));
  }


  public <T1, T2, T3, R> void add(String path,
                                   TriFunction<T1, T2, T3, R> triFunction,
                                   TypeToken<T1> param1Type,
                                   TypeToken<T2> param2Type,
                                   TypeToken<T3> param3Type,
                                   TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(triFunction, param1Type, param2Type, param3Type, returnType));
  }

  public <T1, T2, T3, T4, R> void add(String path,
                                       QuadFunction<T1, T2, T3, T4, R> quadFunction,
                                       TypeToken<T1> param1Type,
                                       TypeToken<T2> param2Type,
                                       TypeToken<T3> param3Type,
                                       TypeToken<T4> param4Type,
                                       TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(quadFunction, param1Type, param2Type, param3Type, param4Type, returnType));
  }

  public <R> void add(String path, Supplier<R> supplier) {
    validatePath(path);
    routesMap.put(path, Route.of(supplier));
  }

  public <T, R> void add(String path, Function<T, R> function) {
    validatePath(path);
    routesMap.put(path, Route.of(function));
  }

  public <T1, T2, R> void add(String path, BiFunction<T1, T2, R> biFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(biFunction));
  }

  public <T1, T2, T3, R> void add(String path, TriFunction<T1, T2, T3, R> triFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(triFunction));
  }

  public <T1, T2, T3, T4, R> void add(String path, QuadFunction<T1, T2, T3, T4, R> quadFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(quadFunction));
  }


  public Route get(String path) {
    return routesMap.get(path);
  }

  public Object invoke(String path, Object... args) throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invoke(args);
  }

  public <R> R invokeTyped(String path, Class<R> returnType, Object... args)
      throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invokeTyped(returnType, args);
  }

  public <R> R invokeTyped(String path, TypeToken<R> returnType, Object... args)
      throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invokeTyped(returnType, args);
  }

  private void validatePath(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Path cannot be null or empty");
    }

    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with a /");
    }
  }
}
