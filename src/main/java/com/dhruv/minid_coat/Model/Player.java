package com.dhruv.minid_coat.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@Document(collection = "player")
public class Player {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String playerId;
    private String name;
    private String status;

}
