package com.dhruv.minid_coat.Model;


import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "games")
public class Game {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private String gameId;
    private List<String> teamA;
    private List<String> teamB;
    private Status status;
    private String currentTurn;
    private List<Card> remaining;

    private Map<String,Integer> scores=Map.of("teamA", 0, "teamB", 0);;
    private String winner;
    private LocalDateTime createdAt;

}
