package com.dhruv.minid_coat.controller;

import com.dhruv.minid_coat.Model.Player;
import com.dhruv.minid_coat.Service.PlayerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/player")
public class PlayerController {

    private PlayerService playerService;
    public PlayerController( PlayerService playerService)
    {
        this.playerService=playerService;
    }
    @PostMapping
    public ResponseEntity<?> createPlayer(@RequestBody Player player)
    {
        try{
            playerService.createPlayer(player);
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Error in creation of player.Ensure Unique Player Id ",HttpStatus.BAD_GATEWAY);
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getPlayer(@PathVariable String name)
    {
        try{
            Player player=playerService.getPlayer(name);
            return new ResponseEntity<>(player,HttpStatus.OK);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Player not found",HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{name}")
    public ResponseEntity<?> updatePlayer(@PathVariable String name, @PathVariable Player player)
    {
        try{
            playerService.updatePlayer(name,player);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch(Exception e)
        {
            return new ResponseEntity<>("Player not found",HttpStatus.BAD_REQUEST);
        }
    }

}
