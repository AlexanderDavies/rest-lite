package com.adavie.router;

import com.adavie.router.function.QuadFunction;
import com.adavie.router.function.TriFunction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registry for routes that maps paths to route handlers.
 * Supports handlers with 0-4 parameters and full type preservation via TypeToken.
 */
public class Routes {
  private final Map<String, Route> routesMap = new HashMap<>();

  public Routes() {
  }

  // ==================== Add Methods with TypeTokens ====================

  /**
   * Register a no-arg route with explicit return type
   */
  public <R> void add(String path,
                      Supplier<R> supplier,
                      TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(supplier, returnType));
  }

  /**
   * Register a one-arg route with explicit types
   */
  public <T, R> void add(String path,
                         Function<T, R> function,
                         TypeToken<T> paramType,
                         TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(function, paramType, returnType));
  }

  /**
   * Register a two-arg route with explicit types
   */
  public <T1, T2, R> void add(String path,
                              BiFunction<T1, T2, R> biFunction,
                              TypeToken<T1> param1Type,
                              TypeToken<T2> param2Type,
                              TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(biFunction, param1Type, param2Type, returnType));
  }

  /**
   * Register a three-arg route with explicit types
   */
  public <T1, T2, T3, R> void add(String path,
                                   TriFunction<T1, T2, T3, R> triFunction,
                                   TypeToken<T1> param1Type,
                                   TypeToken<T2> param2Type,
                                   TypeToken<T3> param3Type,
                                   TypeToken<R> returnType) {
    validatePath(path);
    routesMap.put(path, Route.of(triFunction, param1Type, param2Type, param3Type, returnType));
  }

  /**
   * Register a four-arg route with explicit types
   */
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

  // ==================== Simplified Add Methods (Types inferred as Object) ====================

  /**
   * Register a no-arg route (return type inferred as Object)
   */
  public <R> void add(String path, Supplier<R> supplier) {
    validatePath(path);
    routesMap.put(path, Route.of(supplier));
  }

  /**
   * Register a one-arg route (types inferred as Object)
   */
  public <T, R> void add(String path, Function<T, R> function) {
    validatePath(path);
    routesMap.put(path, Route.of(function));
  }

  /**
   * Register a two-arg route (types inferred as Object)
   */
  public <T1, T2, R> void add(String path, BiFunction<T1, T2, R> biFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(biFunction));
  }

  /**
   * Register a three-arg route (types inferred as Object)
   */
  public <T1, T2, T3, R> void add(String path, TriFunction<T1, T2, T3, R> triFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(triFunction));
  }

  /**
   * Register a four-arg route (types inferred as Object)
   */
  public <T1, T2, T3, T4, R> void add(String path, QuadFunction<T1, T2, T3, T4, R> quadFunction) {
    validatePath(path);
    routesMap.put(path, Route.of(quadFunction));
  }

  // ==================== Retrieval and Invocation ====================

  /**
   * Get a route by path
   */
  public Route get(String path) {
    return routesMap.get(path);
  }

  /**
   * Invoke a route with the given arguments
   */
  public Object invoke(String path, Object... args) throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invoke(args);
  }

  /**
   * Invoke a route with type-safe return value
   */
  public <R> R invokeTyped(String path, Class<R> returnType, Object... args)
      throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invokeTyped(returnType, args);
  }

  /**
   * Invoke a route with TypeToken for generic return types
   */
  public <R> R invokeTyped(String path, TypeToken<R> returnType, Object... args)
      throws RouteInvocationException {
    Route route = routesMap.get(path);
    if (route == null) {
      throw new RouteInvocationException("No route found for path: " + path);
    }
    return route.invokeTyped(returnType, args);
  }

  // ==================== Introspection ====================

  /**
   * Get all registered paths
   */
  public Set<String> getPaths() {
    return new HashSet<>(routesMap.keySet());
  }

  /**
   * Get route metadata for debugging/documentation
   */
  public Map<String, String> getRouteSignatures() {
    Map<String, String> signatures = new HashMap<>();
    routesMap.forEach((path, route) ->
        signatures.put(path, route.getSignature()));
    return signatures;
  }

  /**
   * Check if a path exists and accepts given argument types
   */
  public boolean canInvoke(String path, Class<?>... argTypes) {
    Route route = routesMap.get(path);
    if (route == null) {
      return false;
    }

    if (route.getParamCount() != argTypes.length) {
      return false;
    }

    Class<?>[] expectedTypes = route.getRawParamTypes();
    for (int i = 0; i < argTypes.length; i++) {
      if (!expectedTypes[i].isAssignableFrom(argTypes[i])) {
        return false;
      }
    }

    return true;
  }

  /**
   * Check if a route exists at the given path
   */
  public boolean hasRoute(String path) {
    return routesMap.containsKey(path);
  }

  // ==================== Validation ====================

  private void validatePath(String path) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Path cannot be null or empty");
    }

    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with a /");
    }
  }
}
