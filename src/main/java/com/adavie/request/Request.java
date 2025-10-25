package com.adavie.request;

import java.io.IOException;

public interface Request {
  public void read() throws IOException, RestException;
  public void write() throws IOException;
}
