package com.adavie.router;

import com.adavie.router.function.QuadFunction;
import com.adavie.router.function.TriFunction;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

  @Test
  void testNoArgRouteCreation() {
    Supplier<String> supplier = () -> "test";
    Route route = Route.of(supplier);

    assertEquals(RouteType.NO_ARG, route.getType());
    assertEquals(0, route.getParamCount());
  }

  @Test
  void testOneArgRouteCreation() {
    Function<String, Integer> function = String::length;
    Route route = Route.of(function);

    assertEquals(RouteType.ONE_ARG, route.getType());
    assertEquals(1, route.getParamCount());
  }

  @Test
  void testTwoArgRouteCreation() {
    BiFunction<Integer, Integer, Integer> biFunction = Integer::sum;
    Route route = Route.of(biFunction);

    assertEquals(RouteType.TWO_ARG, route.getType());
    assertEquals(2, route.getParamCount());
  }

  @Test
  void testThreeArgRouteCreation() {
    TriFunction<String, String, String, String> triFunction =
        (a, b, c) -> a + b + c;
    Route route = Route.of(triFunction);

    assertEquals(RouteType.THREE_ARG, route.getType());
    assertEquals(3, route.getParamCount());
  }

  @Test
  void testFourArgRouteCreation() {
    QuadFunction<Integer, Integer, Integer, Integer, Integer> quadFunction =
        (a, b, c, d) -> a + b + c + d;
    Route route = Route.of(quadFunction);

    assertEquals(RouteType.FOUR_ARG, route.getType());
    assertEquals(4, route.getParamCount());
  }

  @Test
  void testNoArgRouteInvocation() throws RouteInvocationException {
    Supplier<String> supplier = () -> "Hello";
    Route route = Route.of(supplier);

    Object result = route.invoke();
    assertEquals("Hello", result);
  }

  @Test
  void testOneArgRouteInvocation() throws RouteInvocationException {
    Function<String, String> function = s -> "Hello, " + s;
    Route route = Route.of(function);

    Object result = route.invoke("World");
    assertEquals("Hello, World", result);
  }

  @Test
  void testTwoArgRouteInvocation() throws RouteInvocationException {
    BiFunction<Integer, Integer, Integer> biFunction = (a, b) -> a * b;
    Route route = Route.of(biFunction);

    Object result = route.invoke(5, 3);
    assertEquals(15, result);
  }

  @Test
  void testThreeArgRouteInvocation() throws RouteInvocationException {
    TriFunction<String, String, String, String> triFunction =
        (a, b, c) -> a + "-" + b + "-" + c;
    Route route = Route.of(triFunction);

    Object result = route.invoke("A", "B", "C");
    assertEquals("A-B-C", result);
  }

  @Test
  void testFourArgRouteInvocation() throws RouteInvocationException {
    QuadFunction<Integer, Integer, Integer, Integer, Integer> quadFunction =
        (a, b, c, d) -> a + b + c + d;
    Route route = Route.of(quadFunction);

    Object result = route.invoke(1, 2, 3, 4);
    assertEquals(10, result);
  }

  @Test
  void testRouteWithExplicitTypeTokens() throws RouteInvocationException {
    Function<String, Integer> function = String::length;
    Route route = Route.of(function,
        new TypeToken<String>() {},
        new TypeToken<Integer>() {});

    Object result = route.invoke("Hello");
    assertEquals(5, result);
  }

  @Test
  void testTypedInvocationWithClass() throws RouteInvocationException {
    Supplier<String> supplier = () -> "test";
    Route route = Route.of(supplier, new TypeToken<String>() {});

    String result = route.invokeTyped(String.class);
    assertEquals("test", result);
  }

  @Test
  void testTypedInvocationWithTypeToken() throws RouteInvocationException {
    Supplier<List<String>> supplier = () -> Arrays.asList("A", "B", "C");
    Route route = Route.of(supplier, new TypeToken<List<String>>() {});

    List<String> result = route.invokeTyped(new TypeToken<List<String>>() {});
    assertEquals(3, result.size());
    assertEquals("A", result.get(0));
  }

  @Test
  void testInvokeWithWrongArgumentCount() {
    Function<String, String> function = s -> s.toUpperCase();
    Route route = Route.of(function);

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> route.invoke());

    assertTrue(exception.getMessage().contains("Expected 1 arguments, got 0"));
  }

  @Test
  void testInvokeWithTooManyArguments() {
    Function<String, String> function = s -> s.toUpperCase();
    Route route = Route.of(function);

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> route.invoke("test", "extra"));

    assertTrue(exception.getMessage().contains("Expected 1 arguments, got 2"));
  }

  @Test
  void testInvokeWithWrongArgumentType() {
    Function<String, Integer> function = String::length;
    Route route = Route.of(function,
        new TypeToken<String>() {},
        new TypeToken<Integer>() {});

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> route.invoke(123));

    assertTrue(exception.getMessage().contains("expected type"));
  }

  @Test
  void testTypedInvokeWithWrongReturnType() {
    Supplier<String> supplier = () -> "test";
    Route route = Route.of(supplier, new TypeToken<String>() {});

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> route.invokeTyped(Integer.class));

    assertTrue(exception.getMessage().contains("Expected return type"));
  }

  @Test
  void testGetRawParamTypes() {
    BiFunction<String, Integer, Boolean> biFunction = (s, i) -> s.length() == i;
    Route route = Route.of(biFunction,
        new TypeToken<String>() {},
        new TypeToken<Integer>() {},
        new TypeToken<Boolean>() {});

    Class<?>[] paramTypes = route.getRawParamTypes();
    assertEquals(2, paramTypes.length);
    assertEquals(String.class, paramTypes[0]);
    assertEquals(Integer.class, paramTypes[1]);
  }

  @Test
  void testGetRawReturnType() {
    Function<String, Integer> function = String::length;
    Route route = Route.of(function,
        new TypeToken<String>() {},
        new TypeToken<Integer>() {});

    Class<?> returnType = route.getRawReturnType();
    assertEquals(Integer.class, returnType);
  }

  @Test
  void testInvokeReturnsNull() throws RouteInvocationException {
    Supplier<String> supplier = () -> null;
    Route route = Route.of(supplier);

    Object result = route.invoke();
    assertNull(result);
  }

  @Test
  void testTypedInvokeReturnsNull() throws RouteInvocationException {
    Supplier<String> supplier = () -> null;
    Route route = Route.of(supplier, new TypeToken<String>() {});

    String result = route.invokeTyped(String.class);
    assertNull(result);
  }

  @Test
  void testRouteWithGenericMapType() throws RouteInvocationException {
    Supplier<Map<String, Integer>> supplier = () -> Map.of("one", 1, "two", 2);
    Route route = Route.of(supplier, new TypeToken<Map<String, Integer>>() {});

    Map<String, Integer> result = route.invokeTyped(new TypeToken<Map<String, Integer>>() {});
    assertEquals(2, result.size());
    assertEquals(1, result.get("one"));
    assertEquals(2, result.get("two"));
  }

  @Test
  void testNoArgRouteWithNullValidation() throws RouteInvocationException {
    Supplier<String> supplier = () -> "test";
    Route route = Route.of(supplier);

    Object result = route.invoke(new Object[0]);
    assertEquals("test", result);
  }

  @Test
  void testMultipleInvocations() throws RouteInvocationException {
    Function<Integer, Integer> function = x -> x * 2;
    Route route = Route.of(function);

    assertEquals(4, route.invoke(2));
    assertEquals(10, route.invoke(5));
    assertEquals(20, route.invoke(10));
  }
}
