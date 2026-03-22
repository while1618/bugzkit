package org.bugzkit.api.shared.generic.crud;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.bugzkit.api.shared.error.ErrorMessage;
import org.bugzkit.api.shared.payload.dto.PageableDTO;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class CrudController<T, U> {
  private final CrudService<T, U> service;

  protected CrudController(CrudService<T, U> service) {
    this.service = service;
  }

  @Operation(summary = "Create a new resource")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Created"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict — resource already exists",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @PostMapping
  public ResponseEntity<T> create(@Valid @RequestBody U saveRequest) {
    return new ResponseEntity<>(service.create(saveRequest), HttpStatus.CREATED);
  }

  @Operation(summary = "List all resources (paginated)")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping
  public ResponseEntity<PageableDTO<T>> findAll(Pageable pageable) {
    return ResponseEntity.ok(service.findAll(pageable));
  }

  @Operation(summary = "Get a resource by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<T> findById(@PathVariable Long id) {
    return ResponseEntity.ok(service.findById(id));
  }

  @Operation(summary = "Replace a resource by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "OK"),
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "409",
        description = "Conflict — username or email already exists",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @PutMapping("/{id}")
  public ResponseEntity<T> update(@PathVariable Long id, @Valid @RequestBody U saveRequest) {
    return ResponseEntity.ok(service.update(id, saveRequest));
  }

  @Operation(summary = "Delete a resource by ID")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Deleted"),
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Forbidden",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
