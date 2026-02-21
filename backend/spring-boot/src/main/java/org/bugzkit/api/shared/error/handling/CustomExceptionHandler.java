package org.bugzkit.api.shared.error.handling;

import jakarta.annotation.Nonnull;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.error.exception.BadRequestException;
import org.bugzkit.api.shared.error.exception.ConflictException;
import org.bugzkit.api.shared.error.exception.ResourceNotFoundException;
import org.bugzkit.api.shared.error.exception.TooManyRequestsException;
import org.bugzkit.api.shared.error.exception.UnauthorizedException;
import org.bugzkit.api.shared.logger.CustomLogger;
import org.bugzkit.api.shared.message.service.MessageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
  private final MessageService messageService;
  private final CustomLogger customLogger;

  public CustomExceptionHandler(MessageService messageService, CustomLogger customLogger) {
    this.messageService = messageService;
    this.customLogger = customLogger;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      @Nonnull MethodArgumentNotValidException e,
      @Nonnull HttpHeaders headers,
      @Nonnull HttpStatusCode statusCode,
      @Nonnull WebRequest request) {
    customLogger.error("Invalid arguments", e);
    final var status = (HttpStatus) statusCode;
    final var errorMessage = new ErrorMessage(status);
    final var result = e.getBindingResult();
    result.getFieldErrors().forEach(error -> errorMessage.addCode(error.getDefaultMessage()));
    result.getGlobalErrors().forEach(error -> errorMessage.addCode(error.getDefaultMessage()));
    return new ResponseEntity<>(errorMessage, setHeaders(), status);
  }

  private ResponseEntity<Object> createError(HttpStatus status, String code) {
    final var errorMessage = new ErrorMessage(status);
    errorMessage.addCode(code);
    return new ResponseEntity<>(errorMessage, setHeaders(), status);
  }

  private HttpHeaders setHeaders() {
    final var header = new HttpHeaders();
    header.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
    return header;
  }

  @ExceptionHandler({BadRequestException.class})
  public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
    customLogger.error("Bad request", e);
    return createError(e.getStatus(), messageService.getMessage(e.getMessage()));
  }

  @ExceptionHandler({UnauthorizedException.class})
  public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e) {
    customLogger.error("Unauthorized", e);
    return createError(e.getStatus(), messageService.getMessage(e.getMessage()));
  }

  @ExceptionHandler({ResourceNotFoundException.class})
  public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
    customLogger.error("Resource not found", e);
    return createError(e.getStatus(), messageService.getMessage(e.getMessage()));
  }

  @ExceptionHandler({ConflictException.class})
  public ResponseEntity<Object> handleConflictException(ConflictException e) {
    customLogger.error("Conflict", e);
    return createError(e.getStatus(), messageService.getMessage(e.getMessage()));
  }

  @ExceptionHandler({TooManyRequestsException.class})
  public ResponseEntity<Object> handleTooManyRequestsException(TooManyRequestsException e) {
    customLogger.error("Too many requests", e);
    return createError(e.getStatus(), messageService.getMessage(e.getMessage()));
  }

  @ExceptionHandler({AuthenticationException.class})
  public ResponseEntity<Object> handleAuthenticationException(AuthenticationException e) {
    if (e instanceof DisabledException) {
      customLogger.error("User not active", e);
      return createError(HttpStatus.FORBIDDEN, messageService.getMessage("user.notActive"));
    } else if (e instanceof LockedException) {
      customLogger.error("User locked", e);
      return createError(HttpStatus.FORBIDDEN, messageService.getMessage("user.lock"));
    } else {
      customLogger.error("Auth failed", e);
      return createError(HttpStatus.UNAUTHORIZED, messageService.getMessage("auth.unauthorized"));
    }
  }

  @ExceptionHandler({AuthorizationDeniedException.class})
  public ResponseEntity<Object> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
    customLogger.error("Forbidden", e);
    return createError(HttpStatus.FORBIDDEN, messageService.getMessage("auth.forbidden"));
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleGlobalException(Exception e) {
    customLogger.error("Exception", e);
    return createError(
        HttpStatus.INTERNAL_SERVER_ERROR, messageService.getMessage("server.internalError"));
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      @Nonnull MissingServletRequestParameterException e,
      @Nonnull HttpHeaders headers,
      @Nonnull HttpStatusCode statusCode,
      @Nonnull WebRequest request) {
    customLogger.error("Parameter missing", e);
    return createError(
        (HttpStatus) statusCode, messageService.getMessage("request.parameterMissing"));
  }

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      @Nonnull HttpRequestMethodNotSupportedException e,
      @Nonnull HttpHeaders headers,
      @Nonnull HttpStatusCode statusCode,
      @Nonnull WebRequest request) {
    customLogger.error("Method not supported", e);
    return createError(
        (HttpStatus) statusCode, messageService.getMessage("request.methodNotSupported"));
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      @Nonnull HttpMessageNotReadableException e,
      @Nonnull HttpHeaders headers,
      @Nonnull HttpStatusCode statusCode,
      @Nonnull WebRequest request) {
    customLogger.error("Message not readable", e);
    return createError(
        (HttpStatus) statusCode, messageService.getMessage("request.messageNotReadable"));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException e) {
    customLogger.error("Parameter type mismatch", e);
    return createError(
        HttpStatus.BAD_REQUEST, messageService.getMessage("request.parameterTypeMismatch"));
  }
}
