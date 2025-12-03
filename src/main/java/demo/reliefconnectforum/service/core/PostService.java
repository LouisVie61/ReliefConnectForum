package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.dto.request.PostRequest;
import demo.reliefconnectforum.dto.response.PostResponse;
import demo.reliefconnectforum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PostService {
    Page<PostResponse> getAll(Pageable pageable);
    PostResponse getById(UUID id);
    PostResponse create(PostRequest request);
    PostResponse update(UUID id, PostRequest request);
    void delete(UUID id);

    // Advanced queries with join tables
    Page<PostResponse> findByPlace(String place, Pageable pageable);
    Page<PostResponse> findByPlaces(String[] places, Pageable pageable);

    // Add these for PostgreSQL LIKE comparison
    Page<PostResponse> searchByTitleNoElasticsearch(String query, Pageable pageable);
    Page<PostResponse> searchByContentNoElasticsearch(String query, Pageable pageable);
    Page<PostResponse> searchByLocationNoElasticsearch(String location, Pageable pageable);
}