package com.dhruv.minid_coat.Repository;

import com.dhruv.minid_coat.Model.Game;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GameRepository extends MongoRepository<Game, ObjectId> {
    Game findByGameId(String gameId);
}
