// java
package demo.reliefconnectforum.service;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository repo;

    public List<Post> getAll() {
        return repo.findAll();
    }

    public Post create(Post post) {
        return repo.save(post);
    }
}