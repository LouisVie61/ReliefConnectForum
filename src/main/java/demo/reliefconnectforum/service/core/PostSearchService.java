package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.dto.response.PostSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface PostSearchService {
    Page<PostSearchResponse> searchAll(String query, Pageable pageable);
    Page<PostSearchResponse> searchByTitle(String query, Pageable pageable);
    Page<PostSearchResponse> searchByContent(String query, Pageable pageable);
    Page<PostSearchResponse> searchByType(PostType type, Pageable pageable);
    Page<PostSearchResponse> searchByLocation(String location, Pageable pageable);
    String reindexAllPosts();
}