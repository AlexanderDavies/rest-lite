package com.adavie.request;

import com.adavie.request.model.HttpStatusCodes;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    private String createValidRequest() {
        return "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "User-Agent: test\r\n" +
                "\r\n";
    }

    private HttpRequest createHttpRequest(String requestString) {
        InputStream inputStream = new ByteArrayInputStream(requestString.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        return new HttpRequest(inputStream, outputStream);
    }

    @Test
    void testConstructorWithValidStreams() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        OutputStream outputStream = new ByteArrayOutputStream();

        assertDoesNotThrow(() -> new HttpRequest(inputStream, outputStream));
    }

    @Test
    void testConstructorWithNullInputStream() {
        OutputStream outputStream = new ByteArrayOutputStream();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new HttpRequest(null, outputStream)
        );
        assertEquals("InputStream cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithNullOutputStream() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new HttpRequest(inputStream, null)
        );
        assertEquals("OutputStream cannot be null", exception.getMessage());
    }

    @Test
    void testConstructorWithBothStreamsNull() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> new HttpRequest(null, null)
        );
        assertEquals("InputStream cannot be null", exception.getMessage());
    }

    @Test
    void testReadValidGetRequest() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadValidPostRequest() {
        String request = "POST /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Type: application/json\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadValidPutRequest() {
        String request = "PUT /api/users/123 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadValidDeleteRequest() {
        String request = "DELETE /api/users/123 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadValidOptionsRequest() {
        String request = "OPTIONS * HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadValidHttp10Request() {
        String request = "GET /index.html HTTP/1.0\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithEmptyRequestLine() {
        String request = "\r\n\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testReadRequestWithOnlyOnePart() {
        String request = "GET\r\n\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unable to parse"));
    }

    @Test
    void testReadRequestWithOnlyTwoParts() {
        String request = "GET /api/users\r\n\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unable to parse"));
    }

    @Test
    void testReadRequestWithExtraSpaces() {
        String request = "GET  /api/users  HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Path cannot be null or empty"));
    }

    @Test
    void testReadRequestWithInvalidMethod() {
        String request = "INVALID /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid HTTP METHOD"));
    }

    @Test
    void testReadRequestWithLowercaseMethod() {
        String request = "get /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithPathNotStartingWithSlash() {
        String request = "GET api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Path must start with /"));
    }

    @Test
    void testReadRequestWithEmptyPath() {
        String request = "GET  HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testReadRequestWithPathContainingSpaces() {
        String request = "GET /api/user name HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unsupported HTTP version"));
    }

    @Test
    void testReadRequestWithPathContainingNewline() {
        String request = "GET /api/users\nmalicious HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testReadRequestWithValidEncodedPath() {
        String request = "GET /api/users%20list HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithQueryString() {
        String request = "GET /api/users?id=123&name=test HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithInvalidHttpVersion() {
        String request = "GET /api/users HTTP/2.0\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unsupported HTTP version"));
    }

    @Test
    void testReadRequestWithMalformedHttpVersion() {
        String request = "GET /api/users HTTPX\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Unsupported HTTP version"));
    }

    @Test
    void testReadRequestWithLowercaseHttpVersion() {
        String request = "GET /api/users http/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithMultipleHeaders() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "Accept: application/json\r\n" +
                "Content-Type: text/plain\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderWithSpacesAroundColon() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host : localhost\r\n" +
                "User-Agent: Mozilla/5.0\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderWithLeadingSpaces() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host:    localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderWithTrailingSpaces() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost    \r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderWithoutColon() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "InvalidHeader\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithEmptyHeaderValue() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host:\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderColonAtStart() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                ":value\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithHeaderValueContainingColon() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Authorization: Bearer: token123\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestExceedingMaxHeaderSize() {
        StringBuilder largeHeader = new StringBuilder("GET /api/users HTTP/1.1\r\n");
        largeHeader.append("Host: localhost\r\n");
        largeHeader.append("Large-Header: ");
        largeHeader.append("a".repeat(17 * 1024));
        largeHeader.append("\r\n\r\n");

        HttpRequest httpRequest = createHttpRequest(largeHeader.toString());

        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("exceed maximum size"));
    }

    @Test
    void testReadRequestJustUnderMaxHeaderSize() {
        StringBuilder largeHeader = new StringBuilder("GET /api/users HTTP/1.1\r\n");
        largeHeader.append("Host: localhost\r\n");
        largeHeader.append("Large-Header: ");
        largeHeader.append("a".repeat(16 * 1024 - 100));
        largeHeader.append("\r\n\r\n");

        HttpRequest httpRequest = createHttpRequest(largeHeader.toString());
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithOnlyRequestLine() {
        String request = "GET /api/users HTTP/1.1\r\n\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Missing Host header"));
    }

    @Test
    void testReadEmptyStream() {
        String request = "";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithOnlyCRLF() {
        String request = "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithMixedLineEndings() {
        String request = "GET /api/users HTTP/1.1\n" +
                "Host: localhost\n" +
                "\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testReadRequestWithTripleNewline() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testWriteResponse() throws IOException {
        String request = createValidRequest();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(request.getBytes());

        HttpRequest httpRequest = new HttpRequest(inputStream, outputStream);

        assertDoesNotThrow(() -> httpRequest.write());

        String response = outputStream.toString();
        assertTrue(response.contains("HTTP/1.1 200 OK"));
        assertTrue(response.contains("Content-Type: text/plain"));
        assertTrue(response.contains("Hello, World!"));
    }

    @Test
    void testWriteWithoutRead() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        HttpRequest httpRequest = new HttpRequest(inputStream, outputStream);

        assertDoesNotThrow(() -> httpRequest.write());
    }

    @Test
    void testHeaderInjectionAttack() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "X-Injected: value\r\nInjected-Header: malicious\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testRequestLineInjection() {
        String request = "GET /api/users\r\nInjected: header HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void testNullByteInPath() {
        String request = "GET /api/users\0malicious HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Malformed path"));
    }

    @Test
    void testMissingHostHeaderHttp11() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "User-Agent: test\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Missing Host header"));
    }

    @Test
    void testMissingHostHeaderHttp10() {
        String request = "GET /api/users HTTP/1.0\r\n" +
                "User-Agent: test\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testPathTooLong() {
        String longPath = "/" + "a".repeat(2048);
        String request = "GET " + longPath + " HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.URI_TOO_LONG, exception.getStatusCode());
    }

    @Test
    void testPathExactly2048Characters() {
        String longPath = "/" + "a".repeat(2047);
        String request = "GET " + longPath + " HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testInvalidHeaderNameWithSpace() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Invalid Header: value\r\n" +
                "Host: localhost\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid characters in header"));
    }

    @Test
    void testHeaderValueWithNewline() {
        String request = "GET /api/users HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "X-Test: value\ninjection\r\n" +
                "\r\n";

        HttpRequest httpRequest = createHttpRequest(request);
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("Invalid characters in header value"));
    }

    @Test
    void testAllSupportedHttpMethods() {
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "TRACE"};

        for (String method : methods) {
            String request = method + " /test HTTP/1.1\r\n" +
                    "Host: localhost\r\n" +
                    "\r\n";
            HttpRequest httpRequest = createHttpRequest(request);
            assertDoesNotThrow(() -> httpRequest.read(),
                "Method " + method + " should be valid");
        }
    }

    @Test
    void testManyHeadersWithinLimit() {
        StringBuilder request = new StringBuilder("GET /api/users HTTP/1.1\r\n");
        request.append("Host: localhost\r\n");

        for (int i = 0; i < 100; i++) {
            request.append("X-Custom-Header-").append(i).append(": value").append(i).append("\r\n");
        }
        request.append("\r\n");

        HttpRequest httpRequest = createHttpRequest(request.toString());
        assertDoesNotThrow(() -> httpRequest.read());
    }

    @Test
    void testExcessiveNumberOfHeaders() {
        StringBuilder request = new StringBuilder("GET /api/users HTTP/1.1\r\n");
        request.append("Host: localhost\r\n");

        for (int i = 0; i < 1000; i++) {
            request.append("X-Custom-Header-").append(i).append(": value").append(i).append("\r\n");
        }
        request.append("\r\n");

        HttpRequest httpRequest = createHttpRequest(request.toString());
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("exceed maximum size"));
    }

    @Test
    void testTooManySmallHeaders() {
        StringBuilder request = new StringBuilder("GET /api/users HTTP/1.1\r\n");
        request.append("Host: localhost\r\n");

        for (int i = 0; i < 5000; i++) {
            request.append("X-H-").append(i).append(": v\r\n");
        }
        request.append("\r\n");

        HttpRequest httpRequest = createHttpRequest(request.toString());
        RestException exception = assertThrows(
            RestException.class,
            () -> httpRequest.read()
        );
        assertEquals(HttpStatusCodes.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getMessage().contains("exceed maximum size"));
    }
}
