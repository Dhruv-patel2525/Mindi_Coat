package com.dhruv.minid_coat.Service;

import com.dhruv.minid_coat.Model.Game;
import com.dhruv.minid_coat.Model.GameState;
import com.dhruv.minid_coat.Model.PlayeMove;
import com.dhruv.minid_coat.Model.Status;
import com.dhruv.minid_coat.Repository.GameRepository;
import org.apache.coyote.BadRequestException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class GameService {
    private final GameRepository gameRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    private final RedisTemplate<String, Object> redisTemplate;

    public GameService(GameRepository gameRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       SimpMessagingTemplate simpMessagingTemplate)
    {
        this.gameRepository=gameRepository;
        this.redisTemplate=redisTemplate;
        this.simpMessagingTemplate=simpMessagingTemplate;
    }
    public String createGame(List<String> playersName) throws Exception {
        Game game=new Game();
        game.setGameId(UUID.randomUUID().toString());

        if(playersName.size()%2!=0)
        {
            throw new BadRequestException("Player should be even");
        }
        Collections.shuffle(playersName);
        List<String> teamA = playersName.subList(0, playersName.size() / 2);
        List<String> teamB = playersName.subList(playersName.size() / 2, playersName.size());
        game.setTeamA(teamA);
        game.setTeamB(teamB);

        game.setStatus(Status.WAITING);
        game.setCreatedAt(LocalDateTime.now());
        Game save = gameRepository.save(game);
        return save.getGameId();
    }

    public Game getGame(String gameId) {
        Game game = gameRepository.findByGameId(gameId);
        if(game==null)
        {
            throw new NoSuchElementException("Game not found.");
        }
        return game;
    }

    public void startGame(String gameId) {
        Game game = gameRepository.findByGameId(gameId);
        game.setStatus(Status.IN_PROGRESS);
        gameRepository.save(game);
        List<String> orderedTeams=new ArrayList<>();
        for(int i=0;i<game.getTeamA().size();i++)
        {
            orderedTeams.add(game.getTeamA().get(i));
            orderedTeams.add(game.getTeamB().get(i));
        }
        int noOFCards=52/orderedTeams.size();
        noOFCards=5;
        List<String> deck = initializeDeck(noOFCards);
        Collections.shuffle(deck);

        Map<String, List<String>> teamAHands = distributeCards(deck,game.getTeamA(),noOFCards);
        Map<String, List<String>> teamBHands = distributeCards(deck,game.getTeamA(),noOFCards);


        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        gameState.setDeck(deck);
        gameState.setTeamAHands(teamAHands);
        gameState.setTeamBHands(teamBHands);
        gameState.setOrderedTeams(orderedTeams);
        gameState.setCurrentTurn(0);

        redisTemplate.opsForValue().set("game:" + gameId, gameState);

//        Validate that all players have joined the game.
//        Shuffle the deck and distribute cards to players.
//        Save the initial game state to Redis for real-time gameplay handling.

    }

    public void endGame(String gameId) {
        Game byGameId = gameRepository.findByGameId(gameId);
        byGameId.setStatus(Status.COMPLETED);

//        Retrieve the final game state from Redis.
//        Determine the winner based on team scores.
//                Persist the final game results (e.g., scores, winner) in the game's collection.
//        Remove the game state from Redis to free memory.
    }

    private Map<String, List<String>> distributeCards(List<String> deck, List<String> players,int noOfCards) {
        Map<String, List<String>> hands = new HashMap<>();
        Iterator<String> deckIterator = deck.iterator();

        for (String player : players) {
            List<String> playerHand = new ArrayList<>();
            for (int i = 0; i < noOfCards; i++) {
                if (deckIterator.hasNext()) {
                    playerHand.add(deckIterator.next());
                }
            }
            hands.put(player, playerHand);
        }
        return hands;
    }

    private List<String> initializeDeck(int noOfCards) {
        List<String> suits = Arrays.asList("H", "S", "D", "C");
        List<String> ranks = Arrays.asList("2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A");
        List<String> deck = new ArrayList<>();

        for (String suit : suits) {
            for (String rank : ranks) {
                deck.add(rank + suit);  // Example: "2H", "10S", "KD","AC"
            }
        }
        return deck;
    }
    public void playMove(PlayeMove playerMove,String gameId) {
        String player = playerMove.getPlayer();
        String move = playerMove.getMove();
        String message = player+" played "+move;
        simpMessagingTemplate.convertAndSend("/topic/game-updates",message);
    }
}
