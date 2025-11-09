package demo.reliefconnectforum.config;

import demo.reliefconnectforum.entity.doc.PostDoc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableElasticsearchRepositories(basePackages = "demo.reliefconnectforum.repository.doc")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUrl;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUrl.replace("http://", ""))
                .withConnectTimeout(Duration.ofSeconds(10))
                .withSocketTimeout(Duration.ofSeconds(30))
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndex(ApplicationReadyEvent event) {
        try {
            ElasticsearchOperations elasticsearchOperations =
                    event.getApplicationContext().getBean(ElasticsearchOperations.class);

            IndexOperations indexOperations = elasticsearchOperations.indexOps(PostDoc.class);

            if (!indexOperations.exists()) {
                indexOperations.create();

                Map<String, Object> settings = new HashMap<>();
                Map<String, Object> analysis = new HashMap<>();
                Map<String, Object> analyzer = new HashMap<>();
                Map<String, Object> folding = new HashMap<>();
                folding.put("tokenizer", "standard");
                folding.put("filter", Arrays.asList("lowercase", "asciifolding"));
                analyzer.put("folding", folding);
                analysis.put("analyzer", analyzer);
                settings.put("analysis", analysis);

                indexOperations.putMapping(indexOperations.createMapping(PostDoc.class));
                indexOperations.refresh();
                System.out.println("Elasticsearch index 'posts' created successfully with custom analyzer.");
            } else {
                System.out.println("Elasticsearch index 'posts' already exists.");
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize Elasticsearch index: " + e.getMessage());
            e.printStackTrace();
        }
    }
}