package com.game.service;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class PlayerServiceImpl implements PlayerService{
    private PlayerRepository playerRepository;

    public PlayerServiceImpl() {}

    @Autowired
    public PlayerServiceImpl(PlayerRepository playerRepository) {
        super();
        this.playerRepository = playerRepository;
    }

    @Override
    public List<Player> getPlayerList(
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
    ) {
        final Date afterDate = after == null ? null : new Date(after);
        final Date beforeDate = before == null ? null : new Date(before);
        final List<Player> playerList = new ArrayList<>();
        playerRepository.findAll().forEach((player) -> {
            if (name != null && !player.getName().contains(name)) return;
            if (title != null && !player.getTitle().contains(title)) return;
            if (race != null && player.getRace() != race) return;
            if (profession != null && player.getProfession() != profession) return;
            if (afterDate != null && player.getBirthday().before(afterDate)) return;
            if (beforeDate != null && player.getBirthday().after(beforeDate)) return;
            if (banned != null && player.getBanned().booleanValue() != banned.booleanValue()) return;
            if (minExperience != null && player.getExperience().compareTo(minExperience) < 0) return;
            if (maxExperience != null && player.getExperience().compareTo(maxExperience) > 0) return;
            if (minLevel != null && player.getLevel().compareTo(minLevel) < 0) return;
            if (maxLevel != null && player.getLevel().compareTo(maxLevel) > 0) return;
            playerList.add(player);
        });
        return playerList;
    }

    @Override
    public Integer getPlayersCount(
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
    ) {
        List<Player> playerList = getPlayerList(name, title, race, profession, after, before, banned, minExperience, maxExperience, minLevel, maxLevel);
        return playerList.size();
    }

    @Override
    public Player savePlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    public Player getPlayer(Long id) {
        return playerRepository.findById(id).orElse(null);
    }

    @Override
    public Player updatePlayer(Player oldPLayer, Player newPlayer) throws IllegalArgumentException{
        boolean shouldChange = false;

        final String name = newPlayer.getName();
        if (name != null) {
            if( isValid(name, 12)) {
            oldPLayer.setName(name);
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        final String title = newPlayer.getTitle();
        if (title != null) {
            if(isValid(title, 30)) {
            oldPLayer.setTitle(title);
            } else {
            throw new IllegalArgumentException();
            }
        }
        if (newPlayer.getRace() != null) {
            oldPLayer.setRace(newPlayer.getRace());
        }

        if (newPlayer.getProfession() != null) {
            oldPLayer.setProfession(newPlayer.getProfession());
        }

        final Date newBirthdayDate = newPlayer.getBirthday();
        if (newBirthdayDate != null) {
            if (isBirthdayValid(newBirthdayDate)) {
                oldPLayer.setBirthday(newBirthdayDate);
            } else {
                throw new IllegalArgumentException();
            }
        }

        final Boolean banned = newPlayer.getBanned();
        if (banned != null) {
            oldPLayer.setBanned(banned);
        }

        final Integer experience = newPlayer.getExperience();
        if (experience != null) {
            if (experience >= 0 && experience <= 10000000) {
                oldPLayer.setExperience(experience);
                shouldChange = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (shouldChange) {
            final Integer newLevel = computePlayerLevel(newPlayer.getExperience());
            oldPLayer.setLevel(newLevel);
            final Integer newUntilNextLevel = computePlayerUntilNextLevel(newLevel, experience);
            oldPLayer.setUntilNextLevel(newUntilNextLevel);
        }

        playerRepository.save(oldPLayer);
        return oldPLayer;
    }

    @Override
    public void deletePlayer(Player player) {
        playerRepository.delete(player);
    }

    @Override
    public Integer computePlayerLevel(Integer exp) {
        final int lvl = (int)((Math.sqrt(2500 + 200 * exp) - 50) / 100);
        return lvl;
    }

    @Override
    public Integer computePlayerUntilNextLevel(Integer lvl, Integer exp) {
        final Integer untilNextLevel = 50 * (lvl + 1) * (lvl + 2) - exp;
        return untilNextLevel;
    }

    @Override
    public List<Player> sortUsingOrder(List<Player> players, PlayerOrder order) {
        if (order == null) {
            sortUsingOrder(players, PlayerOrder.ID);
        } else {
            players.sort(new Comparator<Player>() {
                @Override
                public int compare(Player o1, Player o2) {
                    switch (order) {
                        case ID: return o1.getId().compareTo(o2.getId());
                        case NAME: return o1.getName().compareTo(o2.getName());
                        case EXPERIENCE: return o1.getExperience().compareTo(o2.getExperience());
                        case BIRTHDAY: return o1.getBirthday().compareTo(o2.getBirthday());
                        case LEVEL: return o1.getLevel().compareTo(o2.getLevel());
                        default: return 0;
                    }
                }
            });
        }
        return players;
    }

    @Override
    public List<Player> getPage(List<Player> players, Integer pageNumber, Integer pageSize) {
        final Integer page = pageNumber == null ? 0 : pageNumber;
        final Integer size = pageSize == null ? 3 : pageSize;
        final int from = page * size;
        int to = from + size;
        if (to > players.size())
            to = players.size();
        return players.subList(from, to);
    }

    @Override
    public boolean isPlayerValid(Player player) {
        if (player == null) {
            return false;
        } else {
            final boolean isValidName = isValid(player.getName(), 12);
            final boolean isValidTitle = isValid(player.getTitle(), 30);
            final boolean isValidBirthday = isBirthdayValid(player.getBirthday());
            final boolean isValidExperience = player.getExperience() != null && player.getExperience() >= 0 && player.getExperience() <= 10000000;
            return isValidName && isValidTitle && isValidBirthday && isValidExperience;
        }
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private boolean isBirthdayValid(Date birthday) {
        final Date min = getDateForYear(2000);
        final Date max = getDateForYear(3000);
        return birthday != null && (birthday.after(min) || birthday.getTime() == min.getTime()) && (birthday.before(max) || birthday.getTime() == max.getTime());
    }

    private boolean isValid(String str, int maxLength) {
        if (str != null) {
            return !str.isEmpty() && str.length() <= maxLength;
        }
        return false;
    }


}
