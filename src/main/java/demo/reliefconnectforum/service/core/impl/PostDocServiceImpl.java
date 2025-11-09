package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.entity.doc.PostDoc;
import demo.reliefconnectforum.repository.PostRepository;
import demo.reliefconnectforum.repository.doc.PostDocRepository;
import demo.reliefconnectforum.service.core.PostDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PostDocServiceImpl implements PostDocService {

    @Autowired
    private PostDocRepository postDocRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public void indexPost(Post post) {
        PostDoc postDoc = PostDoc.builder()
                .id(post.getId().toString())
                .title(post.getTitle())
                .content(post.getContent())
                .postType(post.getPostType())
                .location(post.getLocation())
                .authorId(post.getAuthor() != null ? post.getAuthor().getId().toString() : null)
                .authorUsername(post.getAuthor() != null ? post.getAuthor().getUsername() : null)
                .targetAmount(post.getTargetAmount() != null ? post.getTargetAmount() : BigDecimal.ZERO)
                .currentAmount(post.getCurrentAmount() != null ? post.getCurrentAmount() : BigDecimal.ZERO)
                .createdAt(post.getCreatedAt())
                .build();


        postDocRepository.save(postDoc);
        elasticsearchOperations.indexOps(PostDoc.class).refresh();
    }

    @Override
    public void deletePost(String postId) {
        postDocRepository.deleteById(postId);
    }
}