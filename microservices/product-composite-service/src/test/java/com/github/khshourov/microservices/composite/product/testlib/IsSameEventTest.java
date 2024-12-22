package com.github.khshourov.microservices.composite.product.testlib;

import static com.github.khshourov.microservices.composite.product.testlib.IsSameEvent.sameEventWithoutCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.khshourov.microservices.api.core.product.Product;
import com.github.khshourov.microservices.api.event.Event;
import org.junit.jupiter.api.Test;

public class IsSameEventTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testIsSameEvent() throws JsonProcessingException {
    Event<Integer, Product> sameEvent1 =
        new Event<>(Event.Type.CREATE, 1, new Product(1, "p1", 1, "sa"));
    Event<Integer, Product> sameEvent2 =
        new Event<>(Event.Type.CREATE, 1, new Product(1, "p1", 1, "sa"));
    Event<Integer, Product> differentEvent1 =
        new Event<>(Event.Type.CREATE, 2, new Product(2, "p2", 2, "sa"));
    Event<Integer, Product> differentEvent2 = new Event<>(Event.Type.DELETE, 1, null);

    String eventAsJson = objectMapper.writeValueAsString(sameEvent1);

    assertThat(eventAsJson, is(sameEventWithoutCreatedAt(sameEvent2)));
    assertThat(eventAsJson, not(sameEventWithoutCreatedAt(differentEvent1)));
    assertThat(eventAsJson, not(sameEventWithoutCreatedAt(differentEvent2)));
  }
}
