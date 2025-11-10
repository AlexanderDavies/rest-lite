package com.adavie.router;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TypeTokenTest {

  @Test
  void testCreateTypeTokenForString() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertEquals(String.class, token.getRawType());
    assertNotNull(token.getType());
  }

  @Test
  void testCreateTypeTokenForInteger() {
    TypeToken<Integer> token = new TypeToken<Integer>() {};

    assertEquals(Integer.class, token.getRawType());
    assertEquals(Integer.class, token.getType());
  }

  @Test
  void testCreateTypeTokenForListOfStrings() {
    TypeToken<List<String>> token = new TypeToken<List<String>>() {};

    assertEquals(List.class, token.getRawType());
    assertNotNull(token.getType());
    assertTrue(token.getType().toString().contains("List"));
    assertTrue(token.getType().toString().contains("String"));
  }

  @Test
  void testCreateTypeTokenForMapOfStringToInteger() {
    TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {};

    assertEquals(Map.class, token.getRawType());
    assertNotNull(token.getType());
    assertTrue(token.getType().toString().contains("Map"));
  }

  @Test
  void testCreateTypeTokenForSetOfIntegers() {
    TypeToken<Set<Integer>> token = new TypeToken<Set<Integer>>() {};

    assertEquals(Set.class, token.getRawType());
    assertNotNull(token.getType());
  }

  @Test
  void testTypeTokenOfFactoryMethod() {
    Type type = String.class;
    TypeToken<?> token = TypeToken.of(type);

    assertEquals(String.class, token.getRawType());
    assertEquals(String.class, token.getType());
  }

  @Test
  void testIsInstanceWithMatchingType() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertTrue(token.isInstance("Hello"));
    assertFalse(token.isInstance(123));
    assertFalse(token.isInstance(null));
  }

  @Test
  void testIsInstanceWithInteger() {
    TypeToken<Integer> token = new TypeToken<Integer>() {};

    assertTrue(token.isInstance(42));
    assertFalse(token.isInstance("42"));
    assertFalse(token.isInstance(null));
  }

  @Test
  void testIsInstanceWithList() {
    TypeToken<List<String>> token = new TypeToken<List<String>>() {};

    assertTrue(token.isInstance(List.of("a", "b")));
    assertFalse(token.isInstance("not a list"));
    assertFalse(token.isInstance(null));
  }

  @Test
  void testIsInstanceWithMap() {
    TypeToken<Map<String, Integer>> token = new TypeToken<Map<String, Integer>>() {};

    assertTrue(token.isInstance(Map.of("one", 1)));
    assertFalse(token.isInstance(List.of(1, 2, 3)));
    assertFalse(token.isInstance(null));
  }

  @Test
  void testIsAssignableFrom() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertTrue(token.isAssignableFrom(String.class));
    assertFalse(token.isAssignableFrom(Integer.class));
  }

  @Test
  void testIsAssignableFromWithInheritance() {
    TypeToken<Object> token = new TypeToken<Object>() {};

    assertTrue(token.isAssignableFrom(String.class));
    assertTrue(token.isAssignableFrom(Integer.class));
    assertTrue(token.isAssignableFrom(Object.class));
  }

  @Test
  void testGetRawTypeForList() {
    TypeToken<List<String>> token = new TypeToken<List<String>>() {};

    assertEquals(List.class, token.getRawType());
  }

  @Test
  void testEqualsWithSameType() {
    TypeToken<String> token1 = new TypeToken<String>() {};
    TypeToken<String> token2 = new TypeToken<String>() {};

    assertEquals(token1, token2);
  }

  @Test
  void testEqualsWithDifferentType() {
    TypeToken<String> token1 = new TypeToken<String>() {};
    TypeToken<Integer> token2 = new TypeToken<Integer>() {};

    assertNotEquals(token1, token2);
  }

  @Test
  void testEqualsSameInstance() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertEquals(token, token);
  }

  @Test
  void testEqualsWithNull() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertNotEquals(token, null);
  }

  @Test
  void testEqualsWithDifferentClass() {
    TypeToken<String> token = new TypeToken<String>() {};

    assertNotEquals(token, "not a type token");
  }

  @Test
  void testHashCodeConsistency() {
    TypeToken<String> token1 = new TypeToken<String>() {};
    TypeToken<String> token2 = new TypeToken<String>() {};

    assertEquals(token1.hashCode(), token2.hashCode());
  }

  @Test
  void testHashCodeDifferentTypes() {
    TypeToken<String> token1 = new TypeToken<String>() {};
    TypeToken<Integer> token2 = new TypeToken<Integer>() {};

    assertNotEquals(token1.hashCode(), token2.hashCode());
  }

  @Test
  void testToString() {
    TypeToken<String> token = new TypeToken<String>() {};

    String str = token.toString();
    assertNotNull(str);
    assertTrue(str.contains("String") || str.contains("java.lang.String"));
  }

  @Test
  void testToStringForGenericType() {
    TypeToken<List<String>> token = new TypeToken<List<String>>() {};

    String str = token.toString();
    assertNotNull(str);
    assertTrue(str.contains("List"));
  }

  @Test
  void testCreateWithoutGenericType() {
    assertThrows(IllegalStateException.class, () -> {
      new TypeToken() {};
    });
  }

  @Test
  void testGetTypeReturnsCorrectType() {
    TypeToken<String> token = new TypeToken<String>() {};

    Type type = token.getType();
    assertEquals(String.class, type);
  }

  @Test
  void testGetTypeForComplexGeneric() {
    TypeToken<Map<String, List<Integer>>> token = new TypeToken<Map<String, List<Integer>>>() {};

    Type type = token.getType();
    assertNotNull(type);
    assertTrue(type.toString().contains("Map"));
  }

  @Test
  void testGetRawTypeForPrimitiveWrapper() {
    TypeToken<Integer> token = new TypeToken<Integer>() {};

    assertEquals(Integer.class, token.getRawType());
  }

  @Test
  void testIsInstanceWithSubclass() {
    TypeToken<Object> token = new TypeToken<Object>() {};

    assertTrue(token.isInstance("string"));
    assertTrue(token.isInstance(123));
    assertTrue(token.isInstance(List.of(1, 2, 3)));
  }

  @Test
  void testMultipleTypeTokensForSameType() {
    TypeToken<String> token1 = new TypeToken<String>() {};
    TypeToken<String> token2 = new TypeToken<String>() {};
    TypeToken<String> token3 = new TypeToken<String>() {};

    assertEquals(token1, token2);
    assertEquals(token2, token3);
    assertEquals(token1, token3);
  }

  @Test
  void testTypeTokenForNestedGenerics() {
    TypeToken<List<Map<String, Integer>>> token = new TypeToken<List<Map<String, Integer>>>() {};

    assertEquals(List.class, token.getRawType());
    assertNotNull(token.getType());
  }

  @Test
  void testOfFactoryMethodWithComplexType() {
    Type type = Integer.class;
    TypeToken<?> token = TypeToken.of(type);

    assertEquals(Integer.class, token.getRawType());
    assertTrue(token.isInstance(42));
    assertFalse(token.isInstance("not an integer"));
  }
}
