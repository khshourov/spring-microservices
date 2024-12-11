package com.github.khshourov.microservices.util.http;

import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;

public record HttpErrorInfo(ZonedDateTime timestamp, String path, int status, String message) {
  HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
    this(ZonedDateTime.now(), path, httpStatus.value(), message);
  }
}
