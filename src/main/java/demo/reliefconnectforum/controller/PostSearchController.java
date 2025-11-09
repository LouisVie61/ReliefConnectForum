package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.dto.response.PostSearchResponse;
import demo.reliefconnectforum.service.core.PostSearchService;
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

@RestController
@RequestMapping("/api/search/posts")
@Tag(name = "Post Search", description = "Endpoints for searching posts using Elasticsearch")
@SecurityRequirement(name = "Bearer Authentication")
public class PostSearchController {

    @Autowired
    private PostSearchService postSearchService;

    @GetMapping("/all")
    @Operation(summary = "Search posts by title or content",
               description = "Full-text search across both title and content fields with accent-insensitive matching")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostSearchResponse>> searchAll(
            @Parameter(description = "Search query string", required = true)
            @RequestParam String query,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PostSearchResponse> results = postSearchService.searchAll(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/title")
    @Operation(summary = "Search posts by title",
               description = "Search posts where title contains the query string")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostSearchResponse>> searchByTitle(
            @Parameter(description = "Search query string for title", required = true)
            @RequestParam String query,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PostSearchResponse> results = postSearchService.searchByTitle(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/content")
    @Operation(summary = "Search posts by content",
               description = "Search posts where content contains the query string")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid query parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostSearchResponse>> searchByContent(
            @Parameter(description = "Search query string for content", required = true)
            @RequestParam String query,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PostSearchResponse> results = postSearchService.searchByContent(query, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/type")
    @Operation(summary = "Search posts by post type",
               description = "Filter posts by specific post type (RESCUE, DONATION, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid post type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostSearchResponse>> searchByType(
            @Parameter(description = "Post type to filter by", required = true)
            @RequestParam PostType type,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PostSearchResponse> results = postSearchService.searchByType(type, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/location")
    @Operation(summary = "Search posts by location",
               description = "Search posts where location matches the query string")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved search results"),
            @ApiResponse(responseCode = "400", description = "Invalid location parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<PostSearchResponse>> searchByLocation(
            @Parameter(description = "Location to search for", required = true)
            @RequestParam String location,
            @Parameter(description = "Pagination information")
            @PageableDefault(page = 0, size = 10) Pageable pageable) {
        Page<PostSearchResponse> results = postSearchService.searchByLocation(location, pageable);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/reindex")
    @Operation(summary = "Reindex all posts",
               description = "Reindex all existing posts from PostgreSQL into Elasticsearch")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully reindexed all posts"),
            @ApiResponse(responseCode = "500", description = "Internal server error during reindexing")
    })
    public ResponseEntity<String> reindexAllPosts() {
        String message = postSearchService.reindexAllPosts();
        return ResponseEntity.ok(message);
    }
}