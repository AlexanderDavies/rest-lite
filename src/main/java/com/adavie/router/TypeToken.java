package com.adavie.router;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Captures and preserves generic type information at runtime.
 * Usage: new TypeToken<List<String>>() {}
 */
public abstract class TypeToken<T> {
  private final Type type;
  private final Class<? super T> rawType;

  @SuppressWarnings("unchecked")
  protected TypeToken() {
    // Capture the actual type argument at runtime
    Type superclass = getClass().getGenericSuperclass();

    if (superclass instanceof ParameterizedType) {
      this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
      this.rawType = (Class<? super T>) getRawType(this.type);
    } else {
      throw new IllegalStateException("TypeToken must be created with generic type");
    }
  }

  // Direct constructor for cases where you already have the Type
  private TypeToken(Type type) {
    this.type = type;
    this.rawType = (Class<? super T>) getRawType(type);
  }

  /**
   * Factory method for creating TypeToken from a Type
   */
  public static TypeToken<?> of(Type type) {
    return new SimpleTypeToken<>(type);
  }

  /**
   * Get the actual Type (preserves generics)
   */
  public Type getType() {
    return type;
  }

  /**
   * Get the raw class (e.g., List.class for List<String>)
   */
  public Class<? super T> getRawType() {
    return rawType;
  }

  /**
   * Check if this type is assignable from another type
   */
  public boolean isAssignableFrom(Type other) {
    return isAssignable(other, this.type);
  }

  /**
   * Check if a value is an instance of this type
   */
  public boolean isInstance(Object value) {
    return value != null && rawType.isInstance(value);
  }

  /**
   * Extract raw class from a Type
   */
  private static Class<?> getRawType(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    } else {
      throw new IllegalArgumentException("Cannot extract raw type from: " + type);
    }
  }

  /**
   * Check if one type is assignable to another
   */
  private static boolean isAssignable(Type from, Type to) {
    if (to instanceof Class<?>) {
      return ((Class<?>) to).isAssignableFrom(getRawType(from));
    }
    // For more complex generic type checking, could expand this
    return from.equals(to);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypeToken)) return false;
    TypeToken<?> that = (TypeToken<?>) o;
    return type.equals(that.type);
  }

  @Override
  public int hashCode() {
    return type.hashCode();
  }

  @Override
  public String toString() {
    return type.toString();
  }

  /**
   * Simple implementation for factory method
   */
  private static class SimpleTypeToken<T> extends TypeToken<T> {
    SimpleTypeToken(Type type) {
      super(type);
    }
  }
}
