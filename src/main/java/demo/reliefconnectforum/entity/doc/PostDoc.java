package demo.reliefconnectforum.entity.doc;

import demo.reliefconnectforum.Enum.PostType;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(indexName = "posts")
@Setting(settingPath = "/elasticsearch/book-index-settings.json")
@Builder
public class PostDoc {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "folding")
    private String title;

    @Field(type = FieldType.Text, analyzer = "folding")
    private String content;

    @Field(type = FieldType.Keyword)
    private PostType postType;

    @Field(type = FieldType.Text, analyzer = "folding")
    private String location;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text, analyzer = "folding")
    private String authorUsername;

    @Field(type = FieldType.Double)
    private BigDecimal targetAmount;

    @Field(type = FieldType.Double)
    private BigDecimal currentAmount;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}