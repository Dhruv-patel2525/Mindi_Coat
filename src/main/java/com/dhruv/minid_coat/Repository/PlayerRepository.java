package com.dhruv.minid_coat.Repository;

import com.dhruv.minid_coat.Model.Player;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerRepository extends MongoRepository<Player, ObjectId> {

    Player findByPlayerId(java.lang.String playerId);
    Player findByName(java.lang.String playerId);
}
