package demo.reliefconnectforum.repository.doc;

import demo.reliefconnectforum.Enum.PostType;
import demo.reliefconnectforum.entity.doc.PostDoc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "spring.data.elasticsearch.repositories.enabled", havingValue = "true", matchIfMissing = true)
public interface PostDocRepository extends ElasticsearchRepository<PostDoc, String> {

    @Query("{\"match\": {\"title\": {\"query\": \"?0\", \"analyzer\": \"folding\"}}}")
    Page<PostDoc> findByTitleContaining(String title, Pageable pageable);

    @Query("{\"match\": {\"content\": {\"query\": \"?0\", \"analyzer\": \"folding\"}}}")
    Page<PostDoc> findByContentContaining(String content, Pageable pageable);

    Page<PostDoc> findByPostType(PostType postType, Pageable pageable);

    @Query("{\"match\": {\"location\": {\"query\": \"?0\", \"analyzer\": \"folding\"}}}")
    Page<PostDoc> findByLocation(String location, Pageable pageable);

    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"content\"], \"analyzer\": \"folding\"}}")
    Page<PostDoc> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}