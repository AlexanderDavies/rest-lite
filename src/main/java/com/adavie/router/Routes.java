package com.adavie.router;

import java.util.HashMap;
import java.util.Map;

public class Routes {
  private final Map<String, RouteFunction<?>> routesMap = new HashMap<>();

  public Routes() {
  }

  public void add(String path, RouteFunction<?> routeFunction) {
    if (path == null || path.isEmpty()) {
      throw new IllegalArgumentException("Path cannot be null or empty");
    }

    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException("Path must start with a /");
    }

    if (routeFunction == null) {
      throw new IllegalArgumentException("Route function cannot be null");
    }

    this.routesMap.put(path, routeFunction);
  }

  public RouteFunction<?> get(String path) {
    // TO DO: amend to fetch paths containing path params when implementing the path param feature
    return routesMap.get(path);
  }

}
