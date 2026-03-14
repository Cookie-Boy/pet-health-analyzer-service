package ru.sibsutis.pet_health_analyzer.core.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "owners")
@Data
public class Owner {

    @Id
    private String id;

    @Indexed(unique = true)
    private String tgChatId;

    private String firstName;
    private String lastName;
    private String phone;
}