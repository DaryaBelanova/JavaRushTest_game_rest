package com.game.controller;

import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PlayerController {
    private PlayerService playerService;

    public PlayerController() {}

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public List<Player> getPlayersList(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false)Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel,
            @RequestParam(value = "order", required = false) PlayerOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        final List<Player> playerList = playerService.getPlayerList(name, title, race, profession,
                after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        final List<Player> sortedPlayers = playerService.sortUsingOrder(playerList, order);

        return playerService.getPage(sortedPlayers, pageNumber, pageSize);
    }

    @RequestMapping(path = "rest/players/count", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public Integer getPlayersCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) Race race,
            @RequestParam(value = "profession", required = false)Profession profession,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "banned", required = false) Boolean banned,
            @RequestParam(value = "minExperience", required = false) Integer minExperience,
            @RequestParam(value = "maxExperience", required = false) Integer maxExperience,
            @RequestParam(value = "minLevel", required = false) Integer minLevel,
            @RequestParam(value = "maxLevel", required = false) Integer maxLevel
    ) {
        return playerService.getPlayersCount(name, title, race, profession, after, before, banned,
                minExperience, maxExperience, minLevel, maxLevel);
    }

    @RequestMapping(path = "/rest/players", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> createPlayer(@RequestBody Player player) {
        if (!playerService.isPlayerValid(player)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (player.getBanned() == null)
            player.setBanned(false);
        player.setLevel(playerService.computePlayerLevel(player.getExperience()));
        player.setUntilNextLevel(playerService.computePlayerUntilNextLevel(player.getLevel(), player.getExperience()));
        final Player savedPlayer = playerService.savePlayer(player);
        return new ResponseEntity<>(savedPlayer, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.GET)
    public ResponseEntity<Player> getPlayer(@PathVariable(value = "id") String idStr) {
        if (!isValidId(idStr))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final Long id = stringToLong(idStr);
        final Player player = playerService.getPlayer(id);
        if (player == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(player, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Player> updatePlayer(
            @PathVariable(value = "id") String idStr,
            @RequestBody Player player
    ) {
        final ResponseEntity<Player> entity = getPlayer(idStr);
        final Player savedPlayer = entity.getBody();
        if (savedPlayer == null) {
            return entity;
        }

        final Player result;
        try {
            result = playerService.updatePlayer(savedPlayer, player);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(path = "/rest/players/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Player> deletePlayer(@PathVariable(value = "id") String idStr) {
        if (!isValidId(idStr))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        final ResponseEntity<Player> entity = getPlayer(idStr);
        final Player savedPlayer = entity.getBody();
        if (savedPlayer == null) {
            return entity;
        }
        playerService.deletePlayer(savedPlayer);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private Long stringToLong(String strId) {
        if (strId == null) {
            return null;
        } else try {
            return Long.parseLong(strId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidId(String idStr) {
        try {
            Long id = Long.parseLong(idStr);
            if (id > 0 && !idStr.contains("."))
                return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return false;
    }
}
