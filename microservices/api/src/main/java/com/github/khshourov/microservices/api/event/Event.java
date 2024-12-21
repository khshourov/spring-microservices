package com.github.khshourov.microservices.api.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import java.time.ZonedDateTime;

public class Event<K, D> {
  public enum Type {
    CREATE,
    DELETE
  }

  private final Type eventType;
  private final K key;
  private final D data;
  private final ZonedDateTime createdAt;

  public Event(Type eventType, K key, D data) {
    this.eventType = eventType;
    this.key = key;
    this.data = data;
    this.createdAt = ZonedDateTime.now();
  }

  public Type getEventType() {
    return eventType;
  }

  public K getKey() {
    return key;
  }

  public D getData() {
    return data;
  }

  @JsonSerialize(using = ZonedDateTimeSerializer.class)
  public ZonedDateTime getCreatedAt() {
    return createdAt;
  }
}
