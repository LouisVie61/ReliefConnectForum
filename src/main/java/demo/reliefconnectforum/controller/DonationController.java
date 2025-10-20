package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.dto.request.DonationRequest;
import demo.reliefconnectforum.dto.response.DonationResponse;
import demo.reliefconnectforum.service.core.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
@Tag(name = "Donation", description = "Endpoints for managing donations")
@SecurityRequirement(name = "Bearer Authentication")
public class DonationController {

    @Autowired
    private DonationService donationService;

    @GetMapping
    @Operation(summary = "Get all donations with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved donations"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<Page<DonationResponse>> getAll(
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(donationService.getAll(pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a donation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the donation"),
            @ApiResponse(responseCode = "400", description = "Invalid donation ID supplied"),
            @ApiResponse(responseCode = "404", description = "Donation not found")
    })
    public ResponseEntity<DonationResponse> getById(
            @Parameter(description = "UUID of the donation to retrieve", required = true)
            @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(donationService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new donation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Donation created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid donation data supplied")
    })
    public ResponseEntity<DonationResponse> create(
            @Parameter(description = "Donation data", required = true)
            @RequestBody DonationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(donationService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing donation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Donation updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid donation data supplied"),
            @ApiResponse(responseCode = "404", description = "Donation not found")
    })
    public ResponseEntity<DonationResponse> update(
            @Parameter(description = "UUID of the donation to update", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Updated donation data", required = true)
            @RequestBody DonationRequest request) {
        try {
            return ResponseEntity.ok(donationService.update(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a donation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Donation deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid donation ID supplied"),
            @ApiResponse(responseCode = "404", description = "Donation not found")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "UUID of the donation to delete", required = true)
            @PathVariable UUID id) {
        try {
            donationService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}