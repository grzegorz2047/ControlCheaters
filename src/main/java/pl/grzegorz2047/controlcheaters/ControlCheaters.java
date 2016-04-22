package pl.grzegorz2047.controlcheaters;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import net.techcable.npclib.HumanNPC;
import net.techcable.npclib.NPCLib;
import net.techcable.npclib.NPCRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by grzegorz2047 on 21.04.2016
 */
public class ControlCheaters extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;

    private HashMap<String, CheckPlayer> checkedPlayers = new HashMap<String, CheckPlayer>();

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
                if (Bukkit.getOnlinePlayers().size() != 0) {
                    int index = 0;
                    if (Bukkit.getOnlinePlayers().size() > 1) {
                        index = random.nextInt(Bukkit.getOnlinePlayers().size() - 1);
                    }
                    Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
                    Player p = players[index];
                    checkPlayer(p, "");
                }
            }
        }, 20, 20 * 20);
        this.getCommand("sprawdz").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equals("sprawdz")) {
            if (!(sender instanceof Player)) {
                return true;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("lobby.ekipa")) {
                player.sendMessage("§cNie masz do tego permissions!");
                return true;

            }
            if (args.length > 0) {
                String playername = args[0];
                Player review = Bukkit.getPlayer(playername);
                if (review == null) {
                    player.sendMessage("§cGracz nie jest na serwerze!");
                }
                checkPlayer(review, player.getName());
                return true;
            }
        }
        return true;
    }

    private static Random random = new Random();

    public void checkPlayer(Player p, String reviewer) {
        final String playername = p.getName();
        CheckPlayer checkPlayer = new CheckPlayer(p.getName());
        checkPlayer.setReviewer(reviewer);
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(0, 2, 0)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(2, 0, 0)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(0, 0, 2)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(2, 0, 2)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(-2, 0, 0)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(0, 0, -2)));
        checkPlayer.getNpcs().add(spawnNPCs(p, p.getLocation().clone().add(-2, 0, -2)));
        ControlCheaters.this.checkedPlayers.put(playername, checkPlayer);
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                CheckPlayer reviewed = ControlCheaters.this.checkedPlayers.get(playername);
                for (HumanNPC npc : reviewed.getNpcs()) {
                    npc.despawn();
                }
                if (!reviewed.getReviewer().isEmpty()) {
                    Player p = Bukkit.getPlayer(reviewed.getReviewer());
                    if (p != null) {
                        p.sendMessage("§7Gracz §c" + reviewed.getUsername() +
                                "§7 zabil §c" + reviewed.getHitfakes() +
                                "§7 botow na §c" + reviewed.getNpcs().size() + "§7 mozliwych!");
                    }
                }
                System.out.println("Gracz " + reviewed.getUsername() +
                        " zabil " + reviewed.getHitfakes() +
                        " botow na " + reviewed.getNpcs().size() + " mozliwych!");
                //reviewed.getNpcs().clear();
                //ControlCheaters.this.checkedPlayers.remove(playername);
            }
        }, 15);
    }

    HumanNPC spawnNPCs(Player p, Location loc) {
        NPCRegistry registry = NPCLib.getNPCRegistry(p.getName(), this);
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
        NPCRegistry registry = NPCLib.getNPCRegistry(event.getDamager().getName(), this);
        if (registry.isNPC(event.getEntity())) {
            CheckPlayer review = checkedPlayers.get(event.getDamager().getName());
            review.incrementHitFakes();
        }
    }

    @EventHandler
    public void onTouch(EntityDamageEvent event) {
        //System.out.println("!!!!!  ed");
    }


}
