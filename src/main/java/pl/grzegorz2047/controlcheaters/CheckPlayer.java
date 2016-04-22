package pl.grzegorz2047.controlcheaters;

import net.techcable.npclib.HumanNPC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by grzegorz2047 on 22.04.2016
 */
public class CheckPlayer {

    private final List<HumanNPC> npcs = new ArrayList<HumanNPC>();
    private String username;
    private int hitfakes = 0;
    private String reviewer = "";
    public CheckPlayer(String name) {
        this.username = name;
    }

    public void incrementHitFakes() {
        hitfakes++;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getHitfakes() {
        return hitfakes;
    }

    public void setHitfakes(int hitfakes) {
        this.hitfakes = hitfakes;
    }

    public List<HumanNPC> getNpcs() {
        return npcs;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }
    public Player getPlayer(){
        return Bukkit.getPlayer(username);
    }
}
