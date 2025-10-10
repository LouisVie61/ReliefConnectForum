// java
package demo.reliefconnectforum.service;

import demo.reliefconnectforum.entity.Post;
import demo.reliefconnectforum.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PostService {
    public List<Post> getAll();
    public Post create(Post post);
}