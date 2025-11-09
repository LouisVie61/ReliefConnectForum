package demo.reliefconnectforum.service.core.impl;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.entity.doc.PostDoc;
import demo.reliefconnectforum.repository.doc.PostDocRepository;
import demo.reliefconnectforum.service.core.PostDocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PostSearchServiceImpl implements PostDocService {

    Autowired
    private PostDocRepository postDocRepository;

    @Transactional
    public void indexPost(Post post) {
        PostDoc postDoc = new PostDoc();
        postDoc.setId(post.getId().toString());
        postDoc.setTitle(post.getTitle());
        postDoc.setContent(post.getContent());
        postDoc.setPostType(post.getPostType());
        postDoc.setLocation(post.getLocation());
        postDoc.setAuthorId(post.getAuthor().getId().toString());
        postDoc.setAuthorUsername(post.getAuthor().getUsername());
        postDoc.setTargetAmount(post.getTargetAmount() != null ? post.getTargetAmount() : BigDecimal.ZERO);
        postDoc.setCurrentAmount(post.getCurrentAmount() != null ? post.getCurrentAmount() : BigDecimal.ZERO);
        postDoc.setCreatedAt(post.getCreatedAt());

        postDocRepository.save(postDoc);
    }

    public void deletePost(String postId) {
        postDocRepository.deleteById(postId);
    }
}