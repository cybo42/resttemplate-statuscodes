package com.cybo42;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownHttpStatusCodeException;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * @author jcyboski
 *         Date: 4/15/12
 */
public class SampleClientTest {

    /**
     * Baseline test that everything works as expected
     * @throws Exception
     */
    @Test
    public void testSuccess() throws Exception{
        RestTemplate restTemplate = createMock(200, "OK", "success");
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        System.out.println(response);
    }

    /**
     * Tests that when a standard status code such as 404 occurs that the proper exception is thrown
     * @throws Exception
     */
    @Test(expected = HttpClientErrorException.class)
    public void testNotFound() throws Exception{
        RestTemplate restTemplate = createMock(404, "Not found", "not found");

        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost", String.class);
        System.out.println(response.getBody());
    }

    /**
     * Verifies out of box behaviour when a non-standard HTTP Status code is returned
     * we get a {@see IllegalArgumentException}
     *
     * @throws Exception
     */
    @Test(expected = UnknownHttpStatusCodeException.class)
    public void testCustomResponse() throws Exception{
        RestTemplate restTemplate = createMock(450, "Validation error", "username required");

        ResponseEntity<String> response = null;
        response = restTemplate.getForEntity("http://localhost", String.class);
        System.out.println(response.getBody());
    }

    /**
     * After creating a custom {@see org.springframework.web.client.ResponseErrorHandler} this test should
     * pass by throwing a custom exception. However does not due to a logging issue.
     * @throws Exception
     */
    @Test(expected = CustomClientErrorException.class)
    public void testCustomResponseCustomHandler() throws Exception{
        RestTemplate restTemplate = createMock(450, "Validation error", "username required");
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());

        ResponseEntity<String> response = null;
        response = restTemplate.getForEntity("http://localhost", String.class);
        System.out.println(response.getBody());
    }

    /**
     * Proves that when disabling the logger.warn() in RestTemplate.java:479 our custom ResponseErrorHandler and
     * exceptions work as expected
     * @throws Exception
     */
    @Test(expected = CustomClientErrorException.class)
    public void testCustomResponseCustomHandlerDisabledLogger() throws Exception{
        // Resetting the log level for RestTemplate to disable warning logging.
        Logger logger = Logger.getLogger("org.springframework.web.client.RestTemplate");
        logger.setLevel(Level.SEVERE);

        RestTemplate restTemplate = createMock(450, "Validation error", "username required");
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());

        ResponseEntity<String> response = null;
        response = restTemplate.getForEntity("http://localhost", String.class);
        System.out.println(response.getBody());

    }

    /**
     * Method to created a restTemplate with mocked {@see ClientHttpRequestFactory} to create response objects for
     * above tests
     * @param code
     * @param statusText
     * @param message
     * @return
     * @throws Exception
     */
    private RestTemplate createMock(int code, String statusText, String message) throws Exception{
        // Mock the ClientHttpResponse that RestTemplate uses internally
        ClientHttpResponse successfulResponse = mock(ClientHttpResponse.class);

        when(successfulResponse.getRawStatusCode()).thenReturn(code);
        when(successfulResponse.getStatusText()).thenReturn(statusText);
        when(successfulResponse.getStatusCode()).thenAnswer(new Answer<Object>(){
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable{
                ClientHttpResponse mock = (ClientHttpResponse) invocation.getMock();
                return HttpStatus.valueOf(mock.getRawStatusCode());
            }
        });

        // Simple JSON response message
        String success = "{\"message\":\"" + message + "\"}";
        ByteArrayInputStream bin = new ByteArrayInputStream(success.getBytes());
        when(successfulResponse.getBody()).thenReturn(bin);

        // Minimum headers
        HttpHeaders successfulResponseHeaders = new HttpHeaders();
        successfulResponseHeaders.add("Content-length", String.valueOf(success.getBytes().length));
        when(successfulResponse.getHeaders()).thenReturn(successfulResponseHeaders);

        // Mocking the ClientHttpRequest RestTemplate uses internally
        ClientHttpRequest successfulRequest = mock(ClientHttpRequest.class);
        // Minimum headers specfying JSON response
        HttpHeaders successfulRequestHeaders = new HttpHeaders();
        successfulRequestHeaders.add("Accept", "application/json");
        when(successfulRequest.getHeaders()).thenReturn(successfulRequestHeaders);

        // Mock factory used for any request to return desired status
        ClientHttpRequestFactory mockFactory = mock(ClientHttpRequestFactory.class);
        when(mockFactory.createRequest(any(URI.class), any(HttpMethod.class))).thenReturn(successfulRequest);
        when(successfulRequest.execute()).thenReturn(successfulResponse);

        return new RestTemplate(mockFactory);
    }
}
