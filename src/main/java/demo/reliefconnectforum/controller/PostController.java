package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.dto.request.PostRequest;
import demo.reliefconnectforum.dto.response.PostResponse;
import demo.reliefconnectforum.service.core.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "Endpoints for managing posts")
public class PostController {

    @Autowired
    private PostService postService;

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
    @Operation(summary = "Create a new post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid post data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PostResponse> create(
            @Parameter(description = "Post data for the new post")
            @RequestBody PostRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(postService.create(request));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
}