package demo.reliefconnectforum.service.core;

import demo.reliefconnectforum.dto.response.PostSearchResponse;
import demo.reliefconnectforum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface PostDocService {
    void indexPost(Post post);
    void deletePost(String postId);
}
