package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.dto.response.PostSearchResponse;
import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.entity.doc.PostDoc;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.repository.doc.PostDocRepository;
import demo.reliefconnectforum.service.core.PostDocService;
import demo.reliefconnectforum.service.core.PostSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostSearchServiceImpl implements PostSearchService {

    @Autowired
    private PostDocRepository postDocRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostDocService postDocService;

    @Override
    public Page<PostSearchResponse> searchAll(String query, Pageable pageable) {
        Page<PostDoc> results = postDocRepository.findByTitleContainingOrContentContaining(query, query, pageable);
        return results.map(this::mapToResponse);
    }

    @Override
    public Page<PostSearchResponse> searchByTitle(String query, Pageable pageable) {
        Page<PostDoc> results = postDocRepository.findByTitleContaining(query, pageable);
        return results.map(this::mapToResponse);
    }

    @Override
    public Page<PostSearchResponse> searchByContent(String query, Pageable pageable) {
        Page<PostDoc> results = postDocRepository.findByContentContaining(query, pageable);
        return results.map(this::mapToResponse);
    }

    @Override
    public Page<PostSearchResponse> searchByType(PostType type, Pageable pageable) {
        Page<PostDoc> results = postDocRepository.findByPostType(type, pageable);
        return results.map(this::mapToResponse);
    }

    @Override
    public Page<PostSearchResponse> searchByLocation(String location, Pageable pageable) {
        Page<PostDoc> results = postDocRepository.findByLocation(location, pageable);
        return results.map(this::mapToResponse);
    }

    @Override
    public String reindexAllPosts() {
        try {
            List<Post> posts = postRepository.findAll();

            int indexed = 0;
            for (Post post : posts) {
                try {
                    postDocService.indexPost(post);
                    indexed++;
                } catch (Exception e) {
                    System.err.println("Failed to index post " + post.getId() + ": " + e.getMessage());
                }
            }

            return "Reindexed " + indexed + " posts successfully (out of " + posts.size() + " total)";
        } catch (Exception e) {
            System.err.println("Reindex failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to reindex posts: " + e.getMessage(), e);
        }
    }

    private PostSearchResponse mapToResponse(PostDoc postDoc) {
        PostSearchResponse response = new PostSearchResponse();
        response.setId(postDoc.getId());
        response.setTitle(postDoc.getTitle());
        response.setContent(postDoc.getContent());
        response.setPostType(postDoc.getPostType());
        response.setLocation(postDoc.getLocation());
        response.setAuthorId(postDoc.getAuthorId());
        response.setAuthorUsername(postDoc.getAuthorUsername());
        response.setTargetAmount(postDoc.getTargetAmount());
        response.setCurrentAmount(postDoc.getCurrentAmount());
        response.setCreatedAt(postDoc.getCreatedAt());
        return response;
    }
}