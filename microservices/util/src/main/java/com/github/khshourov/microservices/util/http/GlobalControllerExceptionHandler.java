package com.github.khshourov.microservices.util.http;

import com.github.khshourov.microservices.api.exceptions.InvalidInputException;
import com.github.khshourov.microservices.api.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(NotFoundException.class)
  public @ResponseBody HttpErrorInfo handleNotFoundException(
      ServerHttpRequest request, NotFoundException exception) {
    return this.createHttpErrorInfo(HttpStatus.NOT_FOUND, request, exception);
  }

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(InvalidInputException.class)
  public @ResponseBody HttpErrorInfo handleInvalidInputException(
      ServerHttpRequest request, InvalidInputException exception) {
    return this.createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY, request, exception);
  }

  private HttpErrorInfo createHttpErrorInfo(
      HttpStatus httpStatus, ServerHttpRequest request, Exception exception) {
    final String path = request.getPath().pathWithinApplication().value();
    final String message = exception.getMessage();

    log.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);

    return new HttpErrorInfo(httpStatus, path, message);
  }
}
