package demo.reliefconnectforum.repository;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {
    List<Post> findAllByPostType(PostType type);
}
