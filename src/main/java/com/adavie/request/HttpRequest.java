package com.adavie.request;

import com.adavie.request.model.HttpMethod;
import com.adavie.request.model.HttpStatusCodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

class HttpRequest implements Request {
  private static final Logger LOGGER = Logger.getLogger(HttpRequest.class.getName());
  private static final int BUFFER_SIZE = 8192;
  private static final int MAX_HEADER_SIZE = 16 * 1024;
  private static final String NEW_LINE = "\r\n";
  private static final String HTTP_VERSION_1 = "HTTP/1.0";
  private static final String HTTP_VERSION_11 = "HTTP/1.1";
  private static final String HOST_HEADER = "HOST";
  private static final Pattern HEADER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9!#$%&'*+\\-.^_`|~]+$");
  private static final Pattern HEADER_VALUE_PATTERN = Pattern.compile("^[\\x20-\\x7E\\x09]*$");  // Visible ASCII + space + tab
  private static final int NUM_HEADERS_LIMIT = 200;

  private final InputStream inputStream;
  private final OutputStream outputStream;
  private Map<String, String> headers;
  private HttpMethod method;
  private URI path;
  private String httpVersion;

  public HttpRequest(InputStream inputStream, OutputStream outputStream) {
    if (inputStream == null) {
      throw new IllegalArgumentException("InputStream cannot be null");
    }
    if (outputStream == null) {
      throw new IllegalArgumentException("OutputStream cannot be null");
    }

    this.inputStream = inputStream;
    this.outputStream = outputStream;
    this.headers = new HashMap<>();
  }

  @Override
  public void read() throws IOException, RestException {

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[BUFFER_SIZE];
    int bytesRead;
    int headerEndIndex = -1;

    while ((bytesRead = inputStream.read(chunk)) != -1) {
      if (buffer.size() + bytesRead > MAX_HEADER_SIZE) {
        throw new RestException(HttpStatusCodes.BAD_REQUEST, "Request headers exceed maximum size of " + MAX_HEADER_SIZE + " bytes");
      }

      buffer.write(chunk, 0, bytesRead);

      headerEndIndex = findHeaderEnd(buffer.toByteArray());

      if (headerEndIndex != -1) {
        byte[] headerBytes = new byte[headerEndIndex];
        System.arraycopy(buffer.toByteArray(), 0, headerBytes, 0, headerEndIndex);
        parseHeaders(headerBytes);
        break;
      }

      //TO DO: parse the body
    }
  }


  private int findHeaderEnd(byte[] data) {
    for (int i = 0; i <= data.length - 4; i++) {
      if (data[i] == '\r' &&
        data[i + 1] == '\n' &&
        data[i + 2] == '\r' &&
        data[i + 3] == '\n'
      ) {
        return i;
      }
    }
    return -1;
  }


  private void parseHeaders(byte[] headerBytes) throws RestException {
    String headerString = new String(headerBytes, StandardCharsets.UTF_8);
    String[] headerLines = headerString.split(NEW_LINE);

    if (headerLines.length == 0) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Malformed HTTP request");
    }

    parseRequestLine(headerLines[0]);

    int numHeaders = 0;

    for (int i = 1; i < headerLines.length; i++) {
      if (numHeaders > NUM_HEADERS_LIMIT) {
        throw new RestException(HttpStatusCodes.REQUEST_ENTITY_TOO_LARGE, "Number of headers exceeds 200");
      }

      String line = headerLines[i];
      int colonIndex = line.indexOf(':');

      if (colonIndex > 0) {

        String headerName = line.substring(0, colonIndex).trim();
        validateHeaderName(headerName);
        String headerValue = line.substring(colonIndex + 1).trim();
        validateHeaderValue(headerValue);

        numHeaders++;

        headers.put(headerName, headerValue);
      }
    }

    validateHostHeader();

  }

  private void validateHostHeader() throws RestException {
    if (this.httpVersion.equalsIgnoreCase(HTTP_VERSION_11) && !hasHeaderIgnoreCase(HOST_HEADER)) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Missing Host header");
    }
  }

  private void validateHeaderName(String headerName) throws RestException {
    if (headerName == null || headerName.isEmpty()) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Header name cannot be null");
    }


    if (!HEADER_NAME_PATTERN.matcher(headerName).matches()) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Invalid characters in header value");
    }
  }

  private void validateHeaderValue(String headerValue) throws RestException {
    if (headerValue == null) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Header value cannot be null");
    }

    for (char c : headerValue.toCharArray()) {
      if (c == '\r' || c == '\n') {
        throw new RestException(HttpStatusCodes.BAD_REQUEST, "Invalid characters in header value");
      }
    }

    if (!HEADER_VALUE_PATTERN.matcher(headerValue).matches()) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Invalid characters in header value");
    }
  }

  private boolean hasHeaderIgnoreCase(String headerName) {
    for (String key : headers.keySet()) {
      if (key.equalsIgnoreCase(headerName)) {
        return true;
      }
    }
    return false;
  }

  private void parseRequestLine(String requestLine) throws RestException {
    String[] parts = requestLine.split(" ");

    if (parts.length < 3) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Unable to parse HTTP request");
    }

    setMethod(parts[0].toUpperCase().trim());
    setPath(parts[1].trim());
    setHttpVersion(parts[2].toUpperCase().trim());
  }

  private void setMethod(String method) throws RestException {
    try {
      this.method = HttpMethod.valueOf(method.toUpperCase().trim());
    } catch (IllegalArgumentException e) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Invalid HTTP METHOD: " + method);
    }
  }

  private void setPath(String path) throws RestException {
    if (path == null || path.isEmpty()) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Path cannot be null or empty");
    }

    if (!path.startsWith("/") && !path.equals("*")) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Path must start with /");
    }

    if (path.length() > 2048) {
      throw new RestException(HttpStatusCodes.URI_TOO_LONG, "Path exceeds maximum length of 2048 character");
    }

    try {
      this.path = new URI(path);
    } catch (URISyntaxException e) {
      throw new RestException(HttpStatusCodes.BAD_REQUEST, "Malformed path: " + e.getMessage());
    }
  }

  private void setHttpVersion(String httpVersion) throws RestException {

    if (!httpVersion.equals(HTTP_VERSION_1) && !httpVersion.equals(HTTP_VERSION_11)) {
      throw new RestException(HttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED, "Unsupported HTTP version: " + httpVersion);
    }

    this.httpVersion = httpVersion;
  }

  @Override
  public void write() throws IOException {
    //TO DO: update with proper response;

    //PLACEHOLDER
    String response = "HTTP/1.1 200 OK\r\n" +
      "Content-Type: text/plain\r\n" +
      "Content-Length: 13\r\n" +
      "\r\n" +
      "Hello, World!";

    outputStream.write(response.getBytes());
    outputStream.flush();

    LOGGER.info("Response sent");
  }

}
