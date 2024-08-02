package store.ggun.alarm.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "posts")
public class PostModel {
    @Id
    private String id;
    private String title;
    private String content;
    private String authorId;
}
