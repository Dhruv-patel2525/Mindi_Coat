package com.dhruv.minid_coat.controller;

import com.dhruv.minid_coat.Model.Game;
import com.dhruv.minid_coat.Model.PlayeMove;
import com.dhruv.minid_coat.Model.TeamPlayer;
import com.dhruv.minid_coat.Service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/game")
public class GameController {
    private GameService gameService;
    public GameController(GameService gameService)
    {
        this.gameService=gameService;
    }
    @PostMapping
    public ResponseEntity<?> createGame(@RequestBody TeamPlayer player)
    {
        try{
            String gameID=gameService.createGame(player.getPlayerName());
            return new ResponseEntity<>(String.format("Game created successfully: %s ",gameID),HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Error in creating game.Make sure number of players are even and list is not empty", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<?> getGame(@PathVariable java.lang.String gameId)
    {
        try{
            Game game=gameService.getGame(gameId);
            return new ResponseEntity<>(game,HttpStatus.OK);
        }
        catch(NoSuchElementException e)
        {
            return new ResponseEntity<>("Game ID not found",HttpStatus.NOT_FOUND);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Bad request",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{gameId}/teams")
    public ResponseEntity<?> getGameByTeams(@PathVariable java.lang.String gameId)
    {
        try{
            Game game=gameService.getGame(gameId);
            ArrayList<List<String>> teams=new ArrayList<>();
            teams.add(game.getTeamA());
            teams.add(game.getTeamB());
            return new ResponseEntity<>(teams,HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Game ID not found",HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{gameId}/scores")
    public ResponseEntity<?> getGameByScore(@PathVariable java.lang.String gameId)
    {
        try{
            Game game=gameService.getGame(gameId);

            return new ResponseEntity<>(game.getScores(),HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Game ID not found",HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{gameId}/start")
    public ResponseEntity<?> startTheGame(@PathVariable java.lang.String gameId)
    {
        try{
            gameService.startGame(gameId);
            return new ResponseEntity<>("Game created successfully",HttpStatus.OK);

        }
        catch (Exception e)
        {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{gameId}/end")
    public ResponseEntity<?> endTheGame(@PathVariable String gameId)
    {
        try{
            gameService.endGame(gameId);
            return new ResponseEntity<>("Game ended successfully",HttpStatus.OK);

        }
        catch (Exception e)
        {
            return new ResponseEntity<>("Game ID not found",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/{gameId}/play")
    public ResponseEntity<?> playMove(@RequestBody PlayeMove playerMove,@PathVariable String gameId)
    {
        try{
            gameService.playMove(playerMove,gameId);
            return new ResponseEntity<>("Success",HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }


}
