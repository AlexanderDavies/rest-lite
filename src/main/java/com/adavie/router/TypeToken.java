package com.adavie.router;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {
  private final Type type;
  private final Class<? super T> rawType;

  @SuppressWarnings("unchecked")
  protected TypeToken() {
    Type superclass = getClass().getGenericSuperclass();

    if (superclass instanceof ParameterizedType) {
      this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
      this.rawType = (Class<? super T>) getRawType(this.type);
    } else {
      throw new IllegalStateException("TypeToken must be created with generic type");
    }
  }

  private TypeToken(Type type) {
    this.type = type;
    this.rawType = (Class<? super T>) getRawType(type);
  }

  public static TypeToken<?> of(Type type) {
    return new SimpleTypeToken<>(type);
  }

  public Type getType() {
    return type;
  }

  public Class<? super T> getRawType() {
    return rawType;
  }

  public boolean isAssignableFrom(Type other) {
    return isAssignable(other, this.type);
  }

  public boolean isInstance(Object value) {
    return value != null && rawType.isInstance(value);
  }

  private static Class<?> getRawType(Type type) {
    if (type instanceof Class<?>) {
      return (Class<?>) type;
    } else if (type instanceof ParameterizedType) {
      return (Class<?>) ((ParameterizedType) type).getRawType();
    } else {
      throw new IllegalArgumentException("Cannot extract raw type from: " + type);
    }
  }

  private static boolean isAssignable(Type from, Type to) {
    if (to instanceof Class<?>) {
      return ((Class<?>) to).isAssignableFrom(getRawType(from));
    }

    return from.equals(to);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TypeToken<?> that)) return false;
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

  private static class SimpleTypeToken<T> extends TypeToken<T> {
    SimpleTypeToken(Type type) {
      super(type);
    }
  }
}
