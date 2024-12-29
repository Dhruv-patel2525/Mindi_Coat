package com.dhruv.minid_coat.Service;

import com.dhruv.minid_coat.Model.*;
import com.dhruv.minid_coat.Repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.BadRequestException;
import org.bson.types.ObjectId;
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
    private ObjectMapper objectMapper;

    public GameService(GameRepository gameRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       SimpMessagingTemplate simpMessagingTemplate,
                        ObjectMapper objectMapper)
    {
        this.gameRepository=gameRepository;
        this.redisTemplate=redisTemplate;
        this.simpMessagingTemplate=simpMessagingTemplate;
        this.objectMapper=objectMapper;
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
        Map<String, List<String>> teamBHands = distributeCards(deck,game.getTeamB(),noOFCards);


        GameState gameState = new GameState();
        gameState.setGameId(gameId);
        //gameState.setDeck(deck);
        gameState.setTeamAHands(teamAHands);
        gameState.setTeamBHands(teamBHands);
        gameState.setOrderedTeams(orderedTeams);
        gameState.setCurrentTurn(0);
        gameState.setCurrentRound(1);
        gameState.setRound(new HashMap<>());
        gameState.setTotalRound(noOFCards);

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
    public void playMove(PlayeMove playerMove,String gameId) throws Exception {
        GameState gameState=getGameState(gameId);

        String player = playerMove.getPlayer();
        String move = playerMove.getMove();

        if(!gameState.getOrderedTeams().get(gameState.getCurrentTurn()).equals(player))
        {
            throw new BadRequestException("It is not "+player+" turn");
        }

        gameState = checkGameStatus(player,move,gameState,gameId);
        redisTemplate.opsForValue().set("game:" + gameId, gameState);
        //check which player turn was it what did player choose for their current round
        //web socket message


        String message = player+" played "+move;
        ChatMessage chatMessage=new ChatMessage("MOVE",message,player);
        simpMessagingTemplate.convertAndSend("/topic/game-updates",chatMessage);
    }

    private GameState checkGameStatus(String player, String move, GameState gameState,String gameId)
    {
        int totalPlayer=gameState.getOrderedTeams().size();
        Map<String,String> round=gameState.getRound();
        round.put(player,move);

        //Round completes logic
        if(round.size()==totalPlayer)
        {
            int playerturn=(gameState.getCurrentTurn()+1)%totalPlayer;
            String roundWinner=whoWonRound(round,gameState.getOrderedTeams().get(playerturn),gameState.getTrumpCard());             //check who won
            Map<String,Integer>scores = gameState.getScores();
            if(gameState.getTeamAHands().containsKey(roundWinner))
            {
                scores.put("teamA", scores.getOrDefault("teamA",0)+totalPlayer);
            }
            else
            {
                scores.put("teamB", scores.getOrDefault("teamB",0)+totalPlayer);

            }
            gameState.setScores(scores);            //update the score
            gameState.setRound(new HashMap<>());
            gameState.setCurrentRound(gameState.getCurrentRound()+1);
            gameState.setCurrentTurn(gameState.getOrderedTeams().indexOf(roundWinner));  //set current turn based on who won
        }
        else
        {
            gameState.setRound(round);
            gameState.setCurrentTurn((gameState.getCurrentTurn()+1)%totalPlayer);
        }

        //game finished logic
        if(gameState.getCurrentRound()>gameState.getTotalRound())
        {
            String message=(gameState.getScores().get("teamA")>gameState.getScores().get("teamB"))?"Team A ":"Team B ";
            ChatMessage chatMessage=new ChatMessage("WINNER",message+"is Winner",player);
            simpMessagingTemplate.convertAndSend("/topic/game-updates",chatMessage);
            //Game finished change the status and save in database
        }
       return gameState;
    }


//    {
//         ObjectId id;
//         String gameId;
//         List<String> deck;
//         Map<String, List<String>> teamAHands;
//         Map<String, List<String>> teamBHands;
//         List<String> orderedTeams;
//         int currentTurn;
//         int currentRound;
//         int totalRound
//         List<String>round;
//         String trumpCard
//         scores
//    }

    private String whoWonRound(Map<String, String> round, String firstTurnPlayer, String trumpCard)
    {

        //all cards color is same
        //different cards color with null trump card
        //different cards color with non-null trump card
        return null;
    }
    public GameState getGameState(String gameId)
    {
        Object gameStateObj = redisTemplate.opsForValue().get("game:" + gameId);
        if (gameStateObj instanceof Map) {
            try {
                GameState gameState = objectMapper.convertValue(gameStateObj, GameState.class);
                return gameState;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
        return null;
    }
}
