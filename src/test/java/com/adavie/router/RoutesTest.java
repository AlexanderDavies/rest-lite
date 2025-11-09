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

public class RoutesTest {

  // ==================== No-Arg Routes ====================

  @Test
  void testAddingNoArgLambdaRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello World");

    assertEquals("Hello World", routes.invoke("/hello"));
  }

  @Test
  void testAddingNoArgLambdaRouteWithTypeToken() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello World", new TypeToken<String>() {});

    String result = routes.invokeTyped("/hello", String.class);
    assertEquals("Hello World", result);
  }

  // ==================== One-Arg Routes ====================

  @Test
  void testAddingOneArgLambdaRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/greet", (String name) -> "Hello, " + name);

    assertEquals("Hello, Alice", routes.invoke("/greet", "Alice"));
  }

  @Test
  void testAddingOneArgLambdaRouteWithExplicitTypes() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/greet",
        (String name) -> "Hello, " + name,
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    String result = routes.invokeTyped("/greet", String.class, "Bob");
    assertEquals("Hello, Bob", result);
  }

  // ==================== Two-Arg Routes ====================

  @Test
  void testAddingTwoArgLambdaRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/add", (Integer a, Integer b) -> a + b);

    assertEquals(8, routes.invoke("/add", 5, 3));
  }

  @Test
  void testAddingTwoArgLambdaRouteWithExplicitTypes() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/concat",
        (String a, String b) -> a + " " + b,
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    String result = routes.invokeTyped("/concat", String.class, "Hello", "World");
    assertEquals("Hello World", result);
  }

  // ==================== Three-Arg Routes ====================

  @Test
  void testAddingThreeArgLambdaRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/fullname",
        (String first, String middle, String last) -> first + " " + middle + " " + last);

    assertEquals("John Q Doe", routes.invoke("/fullname", "John", "Q", "Doe"));
  }

  @Test
  void testAddingThreeArgLambdaRouteWithExplicitTypes() throws RouteInvocationException {
    Routes routes = new Routes();

    TriFunction<String, String, String, String> fullNameFunc =
        (first, middle, last) -> first + " " + middle + " " + last;

    routes.add("/fullname",
        fullNameFunc,
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    String result = routes.invokeTyped("/fullname", String.class, "Jane", "M", "Smith");
    assertEquals("Jane M Smith", result);
  }

  // ==================== Four-Arg Routes ====================

  @Test
  void testAddingFourArgLambdaRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    QuadFunction<Integer, Integer, Integer, Integer, Integer> sumFunc =
        (a, b, c, d) -> a + b + c + d;

    routes.add("/sum4", sumFunc);

    assertEquals(10, routes.invoke("/sum4", 1, 2, 3, 4));
  }

  @Test
  void testAddingFourArgLambdaRouteWithExplicitTypes() throws RouteInvocationException {
    Routes routes = new Routes();

    QuadFunction<String, String, String, String, String> concatFunc =
        (a, b, c, d) -> a + b + c + d;

    routes.add("/concat4",
        concatFunc,
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    String result = routes.invokeTyped("/concat4", String.class, "A", "B", "C", "D");
    assertEquals("ABCD", result);
  }

  // ==================== Method Reference Tests ====================

  @Test
  void testAddingMethodReferenceRoute() throws RouteInvocationException {
    Routes routes = new Routes();

    class TestMethodReference {
      public static String myRoute() {
        return "World";
      }
    }

    routes.add("/hello", TestMethodReference::myRoute);

    assertEquals("World", routes.invoke("/hello"));
  }

  // ==================== Generic Type Tests ====================

  @Test
  void testAddingRouteWithGenericList() throws RouteInvocationException {
    Routes routes = new Routes();

    Supplier<List<String>> listSupplier = () -> Arrays.asList("Alice", "Bob", "Charlie");

    routes.add("/users", listSupplier, new TypeToken<List<String>>() {});

    List<String> result = routes.invokeTyped("/users", new TypeToken<List<String>>() {});
    assertEquals(3, result.size());
    assertEquals("Alice", result.get(0));
  }

  @Test
  void testAddingRouteWithGenericMap() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/config",
        () -> Map.of("version", "1.0", "name", "MyApp"),
        new TypeToken<Map<String, String>>() {});

    @SuppressWarnings("unchecked")
    Map<String, String> result = (Map<String, String>) routes.invoke("/config");
    assertEquals("1.0", result.get("version"));
    assertEquals("MyApp", result.get("name"));
  }

  // ==================== Path Validation Tests ====================

  @Test
  void testPathIsNullOrBlank() {
    Routes routes = new Routes();

    IllegalArgumentException emptyException = assertThrows(
        IllegalArgumentException.class,
        () -> routes.add("", () -> "World")
    );

    assertEquals("Path cannot be null or empty", emptyException.getMessage());

    IllegalArgumentException nullException = assertThrows(
        IllegalArgumentException.class,
        () -> routes.add(null, () -> "World")
    );

    assertEquals("Path cannot be null or empty", nullException.getMessage());
  }

  @Test
  void testPathStartsWithSlash() {
    Routes routes = new Routes();

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> routes.add("fail", () -> "World")
    );

    assertEquals("Path must start with a /", exception.getMessage());
  }

  // ==================== Invocation Error Tests ====================

  @Test
  void testInvokeNonExistentRoute() {
    Routes routes = new Routes();

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> routes.invoke("/nonexistent")
    );

    assertEquals("No route found for path: /nonexistent", exception.getMessage());
  }

  @Test
  void testInvokeWithWrongNumberOfArguments() {
    Routes routes = new Routes();

    routes.add("/greet", (String name) -> "Hello, " + name);

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> routes.invoke("/greet")  // Missing argument
    );

    assertTrue(exception.getMessage().contains("Expected 1 arguments, got 0"));
  }

  @Test
  void testInvokeWithWrongArgumentType() {
    Routes routes = new Routes();

    routes.add("/greet",
        (String name) -> "Hello, " + name,
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> routes.invoke("/greet", 123)  // Integer instead of String
    );

    assertTrue(exception.getMessage().contains("expected type"));
  }

  // ==================== Introspection Tests ====================

  @Test
  void testGetPaths() {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello");
    routes.add("/greet", (String name) -> "Hello, " + name);

    assertEquals(2, routes.getPaths().size());
    assertTrue(routes.getPaths().contains("/hello"));
    assertTrue(routes.getPaths().contains("/greet"));
  }

  @Test
  void testGetRouteSignatures() {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello", new TypeToken<String>() {});
    routes.add("/greet",
        (String name) -> "Hello, " + name,
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    Map<String, String> signatures = routes.getRouteSignatures();
    assertEquals(2, signatures.size());
    assertTrue(signatures.get("/hello").contains("->"));
    assertTrue(signatures.get("/greet").contains("->"));
  }

  @Test
  void testCanInvoke() {
    Routes routes = new Routes();

    routes.add("/greet",
        (String name) -> "Hello, " + name,
        new TypeToken<String>() {},
        new TypeToken<String>() {});

    assertTrue(routes.canInvoke("/greet", String.class));
    assertFalse(routes.canInvoke("/greet", Integer.class));
    assertFalse(routes.canInvoke("/greet", String.class, String.class));
    assertFalse(routes.canInvoke("/nonexistent", String.class));
  }

  @Test
  void testHasRoute() {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello");

    assertTrue(routes.hasRoute("/hello"));
    assertFalse(routes.hasRoute("/nonexistent"));
  }

  // ==================== Type Safety Tests ====================

  @Test
  void testTypeSafeInvocationWithCorrectType() throws RouteInvocationException {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello World", new TypeToken<String>() {});

    String result = routes.invokeTyped("/hello", String.class);
    assertEquals("Hello World", result);
  }

  @Test
  void testTypeSafeInvocationWithWrongType() {
    Routes routes = new Routes();

    routes.add("/hello", () -> "Hello World", new TypeToken<String>() {});

    RouteInvocationException exception = assertThrows(
        RouteInvocationException.class,
        () -> routes.invokeTyped("/hello", Integer.class)
    );

    assertTrue(exception.getMessage().contains("Expected return type"));
  }
}
