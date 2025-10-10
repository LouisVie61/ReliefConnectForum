package demo.reliefconnectforum.controller;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.repository.PostRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Post API", description = "Management of post and crowdfunding")
@Transactional
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Operation(summary = "Get all posts", description = "return list of posts")
    @GetMapping
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Operation(summary = "Get post by its id", description = "search post by UUID")
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Operation(summary = "Create new post", description = "Create new post with JSON data")
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        // Do not set ID manually; let JPA @GeneratedValue(UUID) handle it
        return postRepository.save(post);
    }

    @Operation(summary = "Delete post", description = "Delete post by its UUID")
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable UUID id) {
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }
}