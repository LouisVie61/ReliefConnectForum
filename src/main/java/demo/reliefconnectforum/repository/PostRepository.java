package demo.reliefconnectforum.repository;

import demo.reliefconnectforum.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Query(value = "SELECT p.* FROM posts p LEFT JOIN users u ON p.user_id = u.id", 
           countQuery = "SELECT COUNT(*) FROM posts",
           nativeQuery = true)
    Page<Post> findAllWithUser(Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.id = :id", 
           nativeQuery = true)
    Optional<Post> findByIdWithUser(@Param("id") UUID id);

    @Query(value = "SELECT p.* FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.location = :location", 
           countQuery = "SELECT COUNT(*) FROM posts WHERE location = :location",
           nativeQuery = true)
    Page<Post> findByLocationWithUser(@Param("location") String location, Pageable pageable);

    @Query(value = "SELECT p.* FROM posts p LEFT JOIN users u ON p.user_id = u.id WHERE p.location = ANY(:locations)", 
           countQuery = "SELECT COUNT(*) FROM posts WHERE location = ANY(:locations)",
           nativeQuery = true)
    Page<Post> findByLocationInWithUser(@Param("locations") String[] locations, Pageable pageable);
}