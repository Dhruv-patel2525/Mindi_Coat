package com.dhruv.minid_coat.Service;

import com.dhruv.minid_coat.Model.Player;
import com.dhruv.minid_coat.Repository.PlayerRepository;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PlayerService {
    private PlayerRepository playerRepository;
    public PlayerService( PlayerRepository playerRepository)
    {
        this.playerRepository=playerRepository;
    }

    public void createPlayer(Player player) {
        player.setPlayerId(UUID.randomUUID().toString());
        playerRepository.save(player);
    }

    public Player getPlayer(String name) {
        return playerRepository.findByName(name);
    }

    public void updatePlayer(String name, Player player) {
        Player newPlayer=playerRepository.findByName(name);
        newPlayer.setName(player.getName());
        playerRepository.save(newPlayer);
    }
}
