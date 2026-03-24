package org.bugzkit.api.shared.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class MailpitClient {
  private static final ObjectMapper mapper = new ObjectMapper();
  private final HttpClient http = HttpClient.newHttpClient();
  private final String baseUrl;

  public MailpitClient(String host, int port) {
    this.baseUrl = "http://" + host + ":" + port;
  }

  public int getMessageCount() throws Exception {
    final var response =
        http.send(
            HttpRequest.newBuilder().GET().uri(URI.create(baseUrl + "/api/v1/messages")).build(),
            HttpResponse.BodyHandlers.ofString());
    return mapper.readTree(response.body()).get("total").intValue();
  }

  public JsonNode getMessages() throws Exception {
    final var response =
        http.send(
            HttpRequest.newBuilder().GET().uri(URI.create(baseUrl + "/api/v1/messages")).build(),
            HttpResponse.BodyHandlers.ofString());
    return mapper.readTree(response.body()).get("messages");
  }

  public void clearMessages() throws Exception {
    http.send(
        HttpRequest.newBuilder().DELETE().uri(URI.create(baseUrl + "/api/v1/messages")).build(),
        HttpResponse.BodyHandlers.discarding());
  }
}
