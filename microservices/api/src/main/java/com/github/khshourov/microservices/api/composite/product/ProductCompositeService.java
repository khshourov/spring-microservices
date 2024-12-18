package com.github.khshourov.microservices.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "ProductComposite", description = "REST API for composite product information.")
public interface ProductCompositeService {
  @Operation(
      summary = "${api.product-composite.create-composite-product.description}",
      description = "${api.product-composite.create-composite-product.notes}")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "400",
            description = "${api.responseCodes.badRequest.description}"),
        @ApiResponse(
            responseCode = "422",
            description = "${api.responseCodes.unprocessableEntity.description}")
      })
  @PostMapping(
      value = "/composite/product",
      consumes = "application/json",
      produces = "application/json")
  void createProduct(@RequestBody ProductAggregate request);

  @Operation(
      summary = "${api.product-composite.delete-composite-product.description}",
      description = "${api.product-composite.delete-composite-product.notes}")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "400",
            description = "${api.responseCodes.badRequest.description}"),
        @ApiResponse(
            responseCode = "422",
            description = "${api.responseCodes.unprocessableEntity.description}")
      })
  @DeleteMapping(value = "/composite/product/{productId}", produces = "application/json")
  void deleteProduct(@PathVariable int productId);

  @Operation(
      summary = "${api.product-composite.get-composite-product.description}",
      description = "${api.product-composite.get-composite-product.notes}")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
        @ApiResponse(
            responseCode = "400",
            description = "${api.responseCodes.badRequest.description}"),
        @ApiResponse(
            responseCode = "404",
            description = "${api.responseCodes.notFound.description}"),
        @ApiResponse(
            responseCode = "422",
            description = "${api.responseCodes.unprocessableEntity.description}")
      })
  @GetMapping(value = "/composite/product/{productId}", produces = "application/json")
  ProductAggregate getProduct(@PathVariable int productId);
}
