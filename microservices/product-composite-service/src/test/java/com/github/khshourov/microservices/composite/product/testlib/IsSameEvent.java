package com.github.khshourov.microservices.composite.product.testlib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khshourov.microservices.api.event.Event;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

public class IsSameEvent extends TypeSafeMatcher<String> {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Event<?, ?> expectedEvent;

  private IsSameEvent(Event<?, ?> expectedEvent) {
    this.expectedEvent = expectedEvent;
  }

  @Override
  protected boolean matchesSafely(String eventAsJson) {
    if (this.expectedEvent == null) {
      return false;
    }

    Map<?, ?> mappedEvent = this.convertJsonToMap(eventAsJson);
    mappedEvent.remove("createdAt");

    Map<?, ?> expectedEventAsMap = this.getMapWithoutCreatedAt(this.expectedEvent);

    return expectedEventAsMap.equals(mappedEvent);
  }

  @Override
  public void describeTo(Description description) {
    String expectedJson = convertObjectToJson(expectedEvent);
    description.appendText("expected to look like " + expectedJson);
  }

  public static IsSameEvent sameEventWithoutCreatedAt(Event<?, ?> expectedEvent) {
    return new IsSameEvent(expectedEvent);
  }

  private Map<?, ?> getMapWithoutCreatedAt(Event<?, ?> event) {
    Map<?, ?> eventAsMap = this.convertObjectToMap(event);
    eventAsMap.remove("createdAt");
    return eventAsMap;
  }

  private Map<?, ?> convertJsonToMap(String eventAsJson) {
    try {
      return this.objectMapper.readValue(eventAsJson, Map.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<?, ?> convertObjectToMap(Object object) {
    return this.objectMapper.convertValue(object, Map.class);
  }

  private String convertObjectToJson(Object object) {
    try {
      return this.objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
