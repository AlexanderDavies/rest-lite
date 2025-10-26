package com.adavie.router;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoutesTest {

  @Test
  void testAddingBasicLambdaRoute() {
    Routes routes = new Routes();

    routes.add("hello", (arg) -> "World");

    assertEquals(routes.get("hello").apply(), "World");

  }

  @Test
  void testAddingBasicMethodReferenceRoute() {
    Routes routes = new Routes();

    class testMethodReference {

      public static String myRoute(Object... objects) {
        return "World";
      }
    }

    routes.add("hello", testMethodReference::myRoute);

    assertEquals(routes.get("hello").apply(), "World");
  }

  @Test
  void testPathIsNullOrBlank() {
    Routes routes = new Routes();

    IllegalArgumentException emptyException = assertThrows(
      IllegalArgumentException.class,
      () -> routes.add("", (arg) -> "World")
    );

    assertEquals("Path cannot be null or empty", emptyException.getMessage());


    IllegalArgumentException nullException = assertThrows(
      IllegalArgumentException.class,
      () -> routes.add(null, (arg) -> "World")
    );

    assertEquals("Path cannot be null or empty", emptyException.getMessage());
  }

  @Test
  void testPathStartsWithSlash() {

    Routes routes = new Routes();

    IllegalArgumentException exception = assertThrows(
      IllegalArgumentException.class,
      () -> routes.add("fail", (arg) -> "World")
    );

    assertEquals("Path must start with a /", exception.getMessage());
  }


  @Test
  void testRouteFunctionIsNull() {
    Routes routes = new Routes();

    IllegalArgumentException emptyException = assertThrows(
      IllegalArgumentException.class,
      () -> routes.add("/", null)
    );

    assertEquals("Route function cannot be null", emptyException.getMessage());

  }
}


