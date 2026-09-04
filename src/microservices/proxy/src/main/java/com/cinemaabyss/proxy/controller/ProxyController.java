package com.cinemaabyss.proxy.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Map;
import java.util.Random;

@Controller
public class ProxyController {

    private final RestTemplate restTemplate;
    private final String monolithUrl;
    private final String moviesServiceUrl;
    private final String eventsServiceUrl;
    private final boolean gradualMigration;
    private final int moviesMigrationPercent;
    private final Random random = new Random();

    public ProxyController(
            RestTemplate restTemplate,
            @Value("${proxy.monolith.url}") String monolithUrl,
            @Value("${proxy.movies-service.url}") String moviesServiceUrl,
            @Value("${proxy.events-service.url}") String eventsServiceUrl,
            @Value("${proxy.gradual-migration}") boolean gradualMigration,
            @Value("${proxy.movies-migration-percent}") int moviesMigrationPercent) {
        this.restTemplate = restTemplate;
        this.monolithUrl = monolithUrl;
        this.moviesServiceUrl = moviesServiceUrl;
        this.eventsServiceUrl = eventsServiceUrl;
        this.gradualMigration = gradualMigration;
        this.moviesMigrationPercent = moviesMigrationPercent;
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "Strangler Fig Proxy is healthy";
    }

    @RequestMapping(value = "/api/movies", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyMovies(HttpServletRequest request,
                                              @RequestParam(required = false) Map<String, String> queryParams,
                                              @RequestBody(required = false) String body) {
        String targetUrl = determineMoviesTarget();
        return forwardRequest(request, targetUrl, body, queryParams);
    }

    @RequestMapping(value = "/api/movies/{path:.+}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyMoviesPath(HttpServletRequest request,
                                                  @RequestParam(required = false) Map<String, String> queryParams,
                                                  @RequestBody(required = false) String body) {
        String targetUrl = determineMoviesTarget();
        return forwardRequest(request, targetUrl, body, queryParams);
    }

    @RequestMapping(value = "/api/events", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyEvents(HttpServletRequest request,
                                               @RequestParam(required = false) Map<String, String> queryParams,
                                               @RequestBody(required = false) String body) {
        return forwardRequest(request, eventsServiceUrl, body, queryParams);
    }

    @RequestMapping(value = "/api/events/{path:.+}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyEventsPath(HttpServletRequest request,
                                                   @RequestParam(required = false) Map<String, String> queryParams,
                                                   @RequestBody(required = false) String body) {
        return forwardRequest(request, eventsServiceUrl, body, queryParams);
    }

    @RequestMapping(value = "/api", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyApi(HttpServletRequest request,
                                           @RequestParam(required = false) Map<String, String> queryParams,
                                           @RequestBody(required = false) String body) {
        return forwardRequest(request, monolithUrl, body, queryParams);
    }

    @RequestMapping(value = "/api/{path:.+}", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    @ResponseBody
    public ResponseEntity<byte[]> proxyApiPath(HttpServletRequest request,
                                                @RequestParam(required = false) Map<String, String> queryParams,
                                                @RequestBody(required = false) String body) {
        return forwardRequest(request, monolithUrl, body, queryParams);
    }

    private String determineMoviesTarget() {
        if (!gradualMigration) {
            return monolithUrl;
        }
        int rand = random.nextInt(100);
        return rand < moviesMigrationPercent ? moviesServiceUrl : monolithUrl;
    }

    private ResponseEntity<byte[]> forwardRequest(HttpServletRequest request,
                                                   String baseUrl,
                                                   String body,
                                                   Map<String, String> queryParams) {
        String method = request.getMethod();
        String requestPath = request.getRequestURI();
        String queryString = request.getQueryString();

        String targetPath = requestPath;
        if (queryString != null && !queryString.isEmpty()) {
            targetPath = requestPath + "?" + queryString;
        }

        String targetUrl = baseUrl + targetPath;

        HttpHeaders requestHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (!"host".equalsIgnoreCase(headerName) &&
                    !"content-length".equalsIgnoreCase(headerName) &&
                    !"connection".equalsIgnoreCase(headerName)) {
                Enumeration<String> headerValues = request.getHeaders(headerName);
                while (headerValues.hasMoreElements()) {
                    requestHeaders.add(headerName, headerValues.nextElement());
                }
            }
        }

        String contentType = request.getContentType();
        if (contentType != null) {
            try {
                requestHeaders.setContentType(MediaType.parseMediaType(contentType));
            } catch (Exception e) {
                requestHeaders.setContentType(MediaType.APPLICATION_JSON);
            }
        } else {
            requestHeaders.setContentType(MediaType.APPLICATION_JSON);
        }

        HttpEntity<byte[]> requestEntity;
        if (body != null && !body.isEmpty()) {
            requestEntity = new HttpEntity<>(body.getBytes(StandardCharsets.UTF_8), requestHeaders);
        } else {
            requestEntity = new HttpEntity<>(requestHeaders);
        }

        try {
            ResponseEntity<byte[]> response;
            HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

            if (body != null && !body.isEmpty() && (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH)) {
                response = restTemplate.exchange(
                        targetUrl,
                        httpMethod,
                        requestEntity,
                        byte[].class
                );
            } else {
                HttpHeaders getHeaders = new HttpHeaders();
                requestHeaders.forEach((key, values) -> {
                    if (!"content-length".equalsIgnoreCase(key) && !"content-type".equalsIgnoreCase(key)) {
                        getHeaders.put(key, values);
                    }
                });
                HttpEntity<byte[]> getEntity = new HttpEntity<>(getHeaders);
                response = restTemplate.exchange(
                        targetUrl,
                        httpMethod,
                        getEntity,
                        byte[].class
                );
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            response.getHeaders().forEach((key, values) -> {
                if (!"transfer-encoding".equalsIgnoreCase(key) &&
                        !"connection".equalsIgnoreCase(key) &&
                        !"content-encoding".equalsIgnoreCase(key)) {
                    responseHeaders.put(key, values);
                }
            });

            return new ResponseEntity<>(
                    response.getBody() != null ? response.getBody() : new byte[0],
                    responseHeaders,
                    response.getStatusCode()
            );
        } catch (Exception e) {
            String errorBody = "Bad Gateway: " + e.getMessage();
            return ResponseEntity.status(502)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(errorBody.getBytes(StandardCharsets.UTF_8));
        }
    }
}
