package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.dto.request.PostRequest;
import demo.reliefconnectforum.dto.response.PostResponse;
import demo.reliefconnectforum.service.core.PostService;
import demo.reliefconnectforum.service.event.AIJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "Endpoints for managing posts")
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private AIJobService aiJobService;

    @GetMapping
    @Operation(summary = "Get all posts with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostResponse>> getAll(
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(postService.getAll(pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a post by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the post"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PostResponse> getById(
            @Parameter(description = "ID of the post to retrieve")
            @PathVariable UUID id) {
        try {
            return ResponseEntity.ok(postService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @Operation(summary = "Create a new post with AI classification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid post data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> create(
            @Parameter(description = "Post data for the new post")
            @RequestBody PostRequest request) {
        try {
            PostResponse response = postService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "post", response,
                            "message", "Post created successfully. AI classification is processing in the background."
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create post: " + e.getMessage()));
        }
    }

    @PostMapping("/no-ai")
    @Operation(summary = "Create a new post without AI classification")
    public ResponseEntity<PostResponse> createNoAI(
            @Parameter(description = "Post data for the new post")
            @RequestBody PostRequest request) {
        PostResponse response = postService.createNoAI(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid post data"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PostResponse> update(
            @Parameter(description = "ID of the post to update")
            @PathVariable UUID id,
            @Parameter(description = "Updated post data")
            @RequestBody PostRequest request) {
        return ResponseEntity.ok(postService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the post to delete")
            @PathVariable UUID id) {
        try {
            postService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search/place")
    @Operation(summary = "Find posts by a specific place")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostResponse>> findByPlace(
            @Parameter(description = "Place to filter posts by")
            @RequestParam String place,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(postService.findByPlace(place, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/places")
    @Operation(summary = "Find posts by multiple places")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved posts"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostResponse>> findByPlaces(
            @Parameter(description = "Array of places to filter posts by")
            @RequestParam String[] places,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        try {
            return ResponseEntity.ok(postService.findByPlaces(places, pageable));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{postId}/reclassify")
    public ResponseEntity<?> reclassifyPost(@PathVariable UUID postId) {

        boolean jobSubmitted = aiJobService.submitJob(postId);

        if (jobSubmitted) {
            return ResponseEntity.ok("Successfully classifying");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("This service is being disable at the moment. Please try again");
        }
    }

    @GetMapping("/title/postgres")
    @Operation(summary = "Search posts by title using PostgreSQL")
    public ResponseEntity<Page<PostResponse>> searchTitlePostgres(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(postService.searchByTitleNoElasticsearch(query, pageable));
    }

    @GetMapping("/content/postgres")
    @Operation(summary = "Search posts by content using PostgreSQL")
    public ResponseEntity<Page<PostResponse>> searchContentPostgres(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(postService.searchByContentNoElasticsearch(query, pageable));
    }
}