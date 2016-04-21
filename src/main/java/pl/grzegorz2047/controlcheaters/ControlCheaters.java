package pl.grzegorz2047.controlcheaters;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.techcable.npclib.HumanNPC;
import net.techcable.npclib.NPC;
import net.techcable.npclib.NPCLib;
import net.techcable.npclib.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Created by grzegorz2047 on 21.04.2016
 */
public class ControlCheaters extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private final List<HumanNPC> npcs = new ArrayList<HumanNPC>();
    private CheckPlayer checkPlayer;

    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        protocolManager = ProtocolLibrary.getProtocolManager();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(Bukkit.getOnlinePlayers().size() != 0){
                    int index = 0;
                    if(Bukkit.getOnlinePlayers().size() > 1){
                        index = random.nextInt(Bukkit.getOnlinePlayers().size()-1);
                    }
                    Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
                    checkPlayer(players[index]);
                }
            }
        }, 20, 20 * 20);
    }


    private static Random random = new Random();

    public void checkPlayer(Player p) {
        npcs.add(spawnNPCs(p.getLocation().clone().add(0, 2, 0)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(2, 0, 0)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(0, 0, 2)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(2, 0, 2)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(-2, 0, 0)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(0, 0, -2)));
        npcs.add(spawnNPCs(p.getLocation().clone().add(-2, 0, -2)));

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                for (HumanNPC npc : npcs) {
                    npc.despawn();
                }
                npcs.clear();
            }
        }, 10);
    }

    HumanNPC spawnNPCs(Location loc) {
        NPCRegistry registry = NPCLib.getNPCRegistry("ControlCheaters", this);
        HumanNPC npc = registry.createHumanNPC(generateString(random, "123456789qwertyuioplkjhgfdsazxcvbnm", 16));
        npc.setProtected(false); //Makes invincible
        npc.setShowInTabList(false);
        npc.setSkin("Policeman");
        npc.spawn(loc); //Spawns at server spawn
        return npc;
    }

    public static String generateString(Random rng, String characters, int length) {
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
/*
    private void spawnEntity(Player p) {
        WrapperPlayServerNamedEntitySpawn spawned = new WrapperPlayServerNamedEntitySpawn();
        spawned.setEntityID(id++); // Must be a unique ID
        spawned.setPosition(p.getLocation().toVector());
// The rotation of the player's head (in degrees)
        spawned.setYaw(0);
        spawned.setPitch(-45);

// Documentation:
// http://mc.kev009.com/Entities#Entity_Metadata_Format
        WrappedDataWatcher watcher = new WrappedDataWatcher();
        watcher.setObject(0, (byte) 0); // Flags. Must be a byte.
        watcher.setObject(1, (short) 300); // Drowning counter. Must be short.
        watcher.setObject(8, (byte) 0); // Visible potion "bubbles". Zero means none.
        spawned.setMetadata(watcher);

        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(p, spawned.getHandle());
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/

    @EventHandler
    public void onTouch(EntityDamageByEntityEvent event) {
        NPCRegistry registry = NPCLib.getNPCRegistry("ControlCheaters", this);
        if (registry.isNPC(event.getEntity())) {
            System.out.println("!!!!!");
        }
        if (registry.isNPC(event.getDamager())) {
            System.out.println("awd");
        }

    }

    @EventHandler
    public void onTouch(EntityDamageEvent event) {
        //System.out.println("!!!!!  ed");
    }


}
