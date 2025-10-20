package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.dto.response.DonationResponse;
import demo.reliefconnectforum.dto.response.DonationStatistic;
import demo.reliefconnectforum.service.core.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "Administrative operations for library management")
@SecurityRequirement(name = "Bearer Authentication")
public class AdminController {

    @Autowired
    private AdminService adminService;


    // ---- DONATIONS MANAGEMENT ----
    @GetMapping("/donations/statistics/post/{postId}")
    @Operation(summary = "Get total donations for a specific post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved total donations"),
            @ApiResponse(responseCode = "400", description = "Invalid post ID supplied"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<BigDecimal> getTotalByPost(
            @Parameter(description = "UUID of the post to retrieve total donations for", required = true)
            @PathVariable UUID postId) {
        try {
            return ResponseEntity.ok(adminService.getTotalDonationsByPostId(postId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/statistics/all-posts")
    @Operation(summary = "Get donation statistics for all posts with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved donation statistics"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters supplied")
    })
    public ResponseEntity<Page<DonationStatistic>> getAllPostsStatistics(
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(adminService.getDonationStatisticsByPost(pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/search/location")
    @Operation(summary = "Find donations by location with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved donations"),
            @ApiResponse(responseCode = "400", description = "Invalid location parameter supplied")
    })
    public ResponseEntity<Page<DonationResponse>> findBylocation(
            @Parameter(description = "location to filter donations by", required = true)
            @RequestParam String location,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(adminService.findByLocation(location, pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/search/locations")
    @Operation(summary = "Find donations by multiple locations with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved donations"),
            @ApiResponse(responseCode = "400", description = "Invalid locations parameter supplied")
    })
    public ResponseEntity<Page<DonationResponse>> findBylocations(
            @Parameter(description = "Array of locations to filter donations by", required = true)
            @RequestParam String[] locations,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(adminService.findByLocations(locations, pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/donations/search/min-amount")
    @Operation(summary = "Find donations by minimum amount with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved donations"),
            @ApiResponse(responseCode = "400", description = "Invalid amount parameter supplied")
    })
    public ResponseEntity<Page<DonationResponse>> findByMinAmount(
            @Parameter(description = "Minimum amount to filter donations by", required = true)
            @RequestParam BigDecimal amount,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(adminService.findByMinAmount(amount, pageable));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}