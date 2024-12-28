package com.dhruv.minid_coat.Model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "card")
public class Card {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private Player cardId;
    private Suit suit;
    private Integer rank;
}
