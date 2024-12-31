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
    private Map<String,List<String>>teams;
    private Map<String,List<String>>hands;
//    private Map<String, List<String>> teamAHands;
//    private Map<String, List<String>> teamBHands;
    private List<String> orderedTeams;
    private int currentTurn;
    private int currentRound;
    private int totalRound;
    private Map<String,Integer> scores=Map.of("teamA", 0, "teamB", 0);
    private Map<String,Integer> mindi_scores=Map.of("teamA", 0, "teamB", 0);
    private Map<String,String>round;
    private String trumpCard;



    // gameId, deck , teamAHAnds , teamBhands, currentTurn , playedCards  , roundScores in Json(Map) ,

}
