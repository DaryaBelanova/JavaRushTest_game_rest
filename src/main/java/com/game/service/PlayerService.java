package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;

import java.util.Date;
import java.util.List;

public interface PlayerService {
    List<Player> getPlayerList(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );

    Integer getPlayersCount(
            String name,
            String title,
            Race race,
            Profession profession,
            Long after,
            Long before,
            Boolean banned,
            Integer minExperience,
            Integer maxExperience,
            Integer minLevel,
            Integer maxLevel
    );

    Player savePlayer(Player player);

    Integer computePlayerLevel(Integer exp);

    Integer computePlayerUntilNextLevel(Integer lvl, Integer exp);

    Player getPlayer(Long id);

    Player updatePlayer(Player oldPlayer, Player newPlayer);

    void deletePlayer(Player player);

    List<Player> sortUsingOrder(List<Player> players, PlayerOrder order);

    boolean isPlayerValid(Player player);

    List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize);
}
