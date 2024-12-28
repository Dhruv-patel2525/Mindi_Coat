package com.dhruv.minid_coat.Model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@Document(collection = "gamestate")
public class GameState {
    @Id
    private ObjectId id;
    private String gameId;
    private List<String> deck;
    private Map<String, List<String>> teamAHands;
    private Map<String, List<String>> teamBHands;
    private List<String> orderedTeams;
    private int currentTurn;



    // gameId, deck , teamAHAnds , teamBhands, currentTurn , playedCards  , roundScores in Json(Map) ,

}
