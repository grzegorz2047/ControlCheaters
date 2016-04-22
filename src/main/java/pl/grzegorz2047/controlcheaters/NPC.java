package pl.grzegorz2047.controlcheaters;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;
import net.md_5.bungee.api.ChatColor;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NPC {

    private DataWatcher watcher;
    private GameProfile profile;
    private Material chestplate;
    private Material leggings;
    private Location location;
    private Material inHand;
    private Material helmet;
    private Material boots;
    private String tablist;
    private int entityID;
    private String name;

   /*
    * NPC, a class for spawning fake players in the 1.8
   Copyright (C) [Summerfeeling]
   Dieses Programm ist freie Software. Sie kĂÂśnnen es unter den Bedingungen der GNU General Public License, wie von der Free Software Foundation verĂÂśffentlicht, weitergeben und/oder modifizieren, entweder gemĂÂ¤ĂĹ¸ Version 3 der Lizenz oder (nach Ihrer Option) jeder spĂÂ¤teren Version.
   Die VerĂÂśffentlichung dieses Programms erfolgt in der Hoffnung, daĂĹ¸ es Ihnen von Nutzen sein wird, aber OHNE IRGENDEINE GARANTIE, sogar ohne die implizite Garantie der MARKTREIFE oder der VERWENDBARKEIT FĂĹR EINEN BESTIMMTEN ZWECK. Details finden Sie in der GNU General Public License.
   Sie sollten ein Exemplar der GNU General Public License zusammen mit diesem Programm erhalten haben. Falls nicht, siehe <http://www.gnu.org/licenses/>.
    */

    public NPC(String name, String tablist, int entityID, Location location, Material inHand) {
        this.location = location;
        this.tablist = ChatColor.translateAlternateColorCodes('&', tablist);
        this.name = ChatColor.translateAlternateColorCodes('&', name);
        this.entityID = entityID;
        this.inHand = inHand;
        this.watcher = new DataWatcher(null);
        watcher.a(6, (float) 20);
    }

    public NPC(String name, Location location) {
        this(name, name, new Random().nextInt(10000), location, Material.AIR);
    }

    public NPC(String name, Location location, Material inHand) {
        this(name, name, new Random().nextInt(10000), location, inHand);
    }

    public NPC(String name, String tablist, Location location) {
        this(name, tablist, new Random().nextInt(10000), location, Material.AIR);
    }

    public NPC(String name, String tablist, Location location, Material inHand) {
        this(name, tablist, new Random().nextInt(10000), location, inHand);
    }

    @SuppressWarnings("deprecation")
    public void spawn() {
        try {
            PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
            addToTablist();

            setValue(packet, "a", entityID);
            setValue(packet, "b", this.profile.getId());
            setValue(packet, "c", toFixedPoint(location.getX()));
            setValue(packet, "d", toFixedPoint(location.getY()));
            setValue(packet, "e", toFixedPoint(location.getZ()));
            setValue(packet, "f", toPackedByte(location.getYaw()));
            setValue(packet, "g", toPackedByte(location.getPitch()));
            setValue(packet, "h", inHand == null ? 0 : inHand.getId());
            setValue(packet, "i", watcher);

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void despawn() {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(new int[]{this.entityID});
        this.removeFromTablist();
        for (Player online : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
        }
    }

    @SuppressWarnings("deprecation")
    public void changePlayerlistName(String name) {
        try {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME);
            PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.profile, 0, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(name)[0]);
            @SuppressWarnings("unchecked") List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) this.getValue(packet, "b");
            players.add(data);

            this.setValue(packet, "b", players);
            this.tablist = name;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void addToTablist() {
        try {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo();
            GameProfile profile = this.profile = this.getProfile();
            PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(profile, 1, WorldSettings.EnumGamemode.NOT_SET, CraftChatMessage.fromString(tablist)[0]);
            @SuppressWarnings("unchecked") List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) getValue(packet, "b");
            players.add(data);

            setValue(packet, "a", PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
            setValue(packet, "b", players);

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    private void removeFromTablist() {
        try {
            PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
            PacketPlayOutPlayerInfo.PlayerInfoData data = packet.new PlayerInfoData(this.profile, -1, null, null);
            @SuppressWarnings("unchecked") List<PacketPlayOutPlayerInfo.PlayerInfoData> players = (List<PacketPlayOutPlayerInfo.PlayerInfoData>) this.getValue(packet, "b");
            players.add(data);

            this.setValue(packet, "b", players);

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void teleport(Location location) {
        try {
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", this.toFixedPoint(location.getX()));
            this.setValue(packet, "c", this.toFixedPoint(location.getY()));
            this.setValue(packet, "d", this.toFixedPoint(location.getZ()));
            this.setValue(packet, "e", this.toPackedByte(location.getYaw()));
            this.setValue(packet, "f", this.toPackedByte(location.getPitch()));
            this.setValue(packet, "g", this.location.getBlock().getType() != Material.AIR);
            this.location = location;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public void setItemInHand(Material material) {
        try {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", 0);
            this.setValue(packet, "c", material == Material.AIR || material == null ? CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)) : CraftItemStack.asNMSCopy(new ItemStack(material)));
            this.inHand = material;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Material getItemInHand() {
        return this.inHand;
    }

    @SuppressWarnings("deprecation")
    public void setHelmet(Material material) {
        try {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", 4);
            this.setValue(packet, "c", material == Material.AIR || material == null ? CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)) : CraftItemStack.asNMSCopy(new ItemStack(material)));
            this.helmet = material;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Material getHelmet() {
        return this.helmet;
    }

    @SuppressWarnings("deprecation")
    public void setChestplate(Material material) {
        try {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", 3);
            this.setValue(packet, "c", material == Material.AIR || material == null ? CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)) : CraftItemStack.asNMSCopy(new ItemStack(material)));
            this.chestplate = material;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Material getChestplate() {
        return this.chestplate;
    }

    @SuppressWarnings("deprecation")
    public void setLeggings(Material material) {
        try {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", 2);
            this.setValue(packet, "c", material == Material.AIR || material == null ? CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)) : CraftItemStack.asNMSCopy(new ItemStack(material)));
            this.leggings = material;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Material getLeggings() {
        return this.leggings;
    }

    @SuppressWarnings("deprecation")
    public void setBoots(Material material) {
        try {
            PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment();

            this.setValue(packet, "a", this.entityID);
            this.setValue(packet, "b", 1);
            this.setValue(packet, "c", material == Material.AIR || material == null ? CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)) : CraftItemStack.asNMSCopy(new ItemStack(material)));
            this.boots = material;

            for (Player online : Bukkit.getOnlinePlayers()) {
                ((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Material getBoots() {
        return this.boots;
    }

    public int getEntityID() {
        return this.entityID;
    }

    public UUID getUUID() {
        return this.profile.getId();
    }

    public Location getLocation() {
        return this.location;
    }

    public String getName() {
        return this.name;
    }

    public String getPlayerlistName() {
        return this.tablist;
    }

    private void setValue(Object instance, String field, Object value) throws Exception {
        Field f = instance.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(instance, value);
    }

    private Object getValue(Object instance, String field) throws Exception {
        Field f = instance.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(instance);
    }

    private int toFixedPoint(double d) {
        return (int) (d * 32.0);
    }

    private byte toPackedByte(float f) {
        return (byte) ((int) (f * 256.0F / 360.0F));
    }

    private GameProfile getProfile() {
        try {
            GameProfile profile = GameProfileBuilder.fetch(UUIDFetcher.getUUID(ChatColor.stripColor(this.name)));
            Field name = profile.getClass().getDeclaredField("name");
            name.setAccessible(true);
            name.set(profile, this.name);
            return profile;
        } catch (Exception e) {
            return new GameProfile(UUID.randomUUID(), this.name);
        }
    }

    public static class GameProfileBuilder {

        private static final String SERVICE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false";
        private static final String JSON_SKIN = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}";
        private static final String JSON_CAPE = "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"},\"CAPE\":{\"url\":\"%s\"}}}";

        private static Gson gson = new GsonBuilder().disableHtmlEscaping().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).registerTypeAdapter(GameProfile.class, new GameProfileSerializer()).registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create();

        private static HashMap<UUID, CachedProfile> cache = new HashMap<UUID, CachedProfile>();

        private static long cacheTime = -1;

        /**
         * Don't run in main thread!
         * <p>
         * Fetches the GameProfile from the Mojang servers
         *
         * @param uuid The player uuid
         * @return The GameProfile
         * @throws IOException If something wents wrong while fetching
         * @see GameProfile
         */
        public static GameProfile fetch(UUID uuid) throws IOException {
            return fetch(uuid, false);
        }

        /**
         * Don't run in main thread!
         * <p>
         * Fetches the GameProfile from the Mojang servers
         *
         * @param uuid     The player uuid
         * @param forceNew If true the cache is ignored
         * @return The GameProfile
         * @throws IOException If something wents wrong while fetching
         * @see GameProfile
         */
        public static GameProfile fetch(UUID uuid, boolean forceNew) throws IOException {
            if (!forceNew && cache.containsKey(uuid) && cache.get(uuid).isValid()) {
                return cache.get(uuid).profile;
            } else {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(SERVICE_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String json = new BufferedReader(new InputStreamReader(connection.getInputStream())).readLine();

                    GameProfile result = gson.fromJson(json, GameProfile.class);
                    cache.put(uuid, new CachedProfile(result));
                    return result;
                } else {
                    if (!forceNew && cache.containsKey(uuid)) {
                        return cache.get(uuid).profile;
                    }
                    JsonObject error = (JsonObject) new JsonParser().parse(new BufferedReader(new InputStreamReader(connection.getErrorStream())).readLine());
                    throw new IOException(error.get("error").getAsString() + ": " + error.get("errorMessage").getAsString());
                }
            }
        }

        /**
         * Builds a GameProfile for the specified args
         *
         * @param uuid The uuid
         * @param name The name
         * @param skin The url from the skin image
         * @return A GameProfile built from the arguments
         * @see GameProfile
         */
        public static GameProfile getProfile(UUID uuid, String name, String skin) {
            return getProfile(uuid, name, skin, null);
        }

        /**
         * Builds a GameProfile for the specified args
         *
         * @param uuid    The uuid
         * @param name    The name
         * @param skinUrl Url from the skin image
         * @param capeUrl Url from the cape image
         * @return A GameProfile built from the arguments
         * @see GameProfile
         */
        public static GameProfile getProfile(UUID uuid, String name, String skinUrl, String capeUrl) {
            GameProfile profile = new GameProfile(uuid, name);
            boolean cape = capeUrl != null && !capeUrl.isEmpty();

            List<Object> args = new ArrayList<Object>();
            args.add(System.currentTimeMillis());
            args.add(UUIDTypeAdapter.fromUUID(uuid));
            args.add(name);
            args.add(skinUrl);
            if (cape) args.add(capeUrl);

            profile.getProperties().put("textures", new Property("textures", Base64Coder.encodeString(String.format(cape ? JSON_CAPE : JSON_SKIN, args.toArray(new Object[args.size()])))));
            return profile;
        }

        /**
         * Sets the time as long as you want to keep the gameprofiles in cache (-1 = never remove it)
         *
         * @param time cache time (default = -1)
         */
        public static void setCacheTime(long time) {
            cacheTime = time;
        }

        private static class GameProfileSerializer implements JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {

            public GameProfile deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = (JsonObject) json;
                UUID id = object.has("id") ? (UUID) context.deserialize(object.get("id"), UUID.class) : null;
                String name = object.has("name") ? object.getAsJsonPrimitive("name").getAsString() : null;
                GameProfile profile = new GameProfile(id, name);

                if (object.has("properties")) {
                    for (Map.Entry<String, Property> prop : ((PropertyMap) context.deserialize(object.get("properties"), PropertyMap.class)).entries()) {
                        profile.getProperties().put(prop.getKey(), prop.getValue());
                    }
                }
                return profile;
            }

            public JsonElement serialize(GameProfile profile, Type type, JsonSerializationContext context) {
                JsonObject result = new JsonObject();
                if (profile.getId() != null)
                    result.add("id", context.serialize(profile.getId()));
                if (profile.getName() != null)
                    result.addProperty("name", profile.getName());
                if (!profile.getProperties().isEmpty())
                    result.add("properties", context.serialize(profile.getProperties()));
                return result;
            }
        }

        private static class CachedProfile {

            private long timestamp = System.currentTimeMillis();
            private GameProfile profile;

            public CachedProfile(GameProfile profile) {
                this.profile = profile;
            }

            public boolean isValid() {
                return cacheTime < 0 || (System.currentTimeMillis() - timestamp) < cacheTime;
            }
        }
    }

    public static class UUIDFetcher {

        /**
         * Date when name changes were introduced
         *
         * @see UUIDFetcher#getUUIDAt(String, long)
         */
        public static final long FEBRUARY_2015 = 1422748800000L;


        private static Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

        private static final String UUID_URL = "https://api.mojang.com/users/profiles/minecraft/%s?at=%d";
        private static final String NAME_URL = "https://api.mojang.com/user/profiles/%s/names";

        private static Map<String, UUID> uuidCache = new HashMap<String, UUID>();
        private static Map<UUID, String> nameCache = new HashMap<UUID, String>();

        private static ExecutorService pool = Executors.newCachedThreadPool();

        private String name;
        private UUID id;

        /**
         * Fetches the uuid asynchronously and passes it to the consumer
         *
         * @param name   The name
         * @param action Do what you want to do with the uuid her
         */
        public static void getUUID(final String name, Consumer<UUID> action) {
            pool.execute(new Acceptor<UUID>(action) {

                @Override
                public UUID getValue() {
                    return getUUID(name);
                }
            });
        }

        /**
         * Fetches the uuid synchronously and returns it
         *
         * @param name The name
         * @return The uuid
         */
        public static UUID getUUID(String name) {
            return getUUIDAt(name, System.currentTimeMillis());
        }

        /**
         * Fetches the uuid synchronously for a specified name and time and passes the result to the consumer
         *
         * @param name      The name
         * @param timestamp Time when the player had this name in milliseconds
         * @param action    Do what you want to do with the uuid her
         */
        public static void getUUIDAt(final String name, final long timestamp, Consumer<UUID> action) {
            pool.execute(new Acceptor<UUID>(action) {

                @Override
                public UUID getValue() {
                    return getUUIDAt(name, timestamp);
                }

            });
        }

        /**
         * Fetches the uuid synchronously for a specified name and time
         *
         * @param name      The name
         * @param timestamp Time when the player had this name in milliseconds
         * @see UUIDFetcher#FEBRUARY_2015
         */
        public static UUID getUUIDAt(String name, long timestamp) {
            name = name.toLowerCase();
            if (uuidCache.containsKey(name)) {
                return uuidCache.get(name);
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(UUID_URL, name, timestamp / 1000)).openConnection();
                connection.setReadTimeout(5000);
                UUIDFetcher data = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher.class);

                uuidCache.put(name, data.id);
                nameCache.put(data.id, data.name);

                return data.id;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * Fetches the name asynchronously and passes it to the consumer
         *
         * @param uuid   The uuid
         * @param action Do what you want to do with the name her
         */
        public static void getName(final UUID uuid, Consumer<String> action) {
            pool.execute(new Acceptor<String>(action) {

                @Override
                public String getValue() {
                    return getName(uuid);
                }

            });
        }

        /**
         * Fetches the name synchronously and returns it
         *
         * @param uuid The uuid
         * @return The name
         */
        public static String getName(UUID uuid) {
            if (nameCache.containsKey(uuid)) {
                return nameCache.get(uuid);
            }
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(String.format(NAME_URL, UUIDTypeAdapter.fromUUID(uuid))).openConnection();
                connection.setReadTimeout(5000);
                UUIDFetcher[] nameHistory = gson.fromJson(new BufferedReader(new InputStreamReader(connection.getInputStream())), UUIDFetcher[].class);
                UUIDFetcher currentNameData = nameHistory[nameHistory.length - 1];

                uuidCache.put(currentNameData.name.toLowerCase(), uuid);
                nameCache.put(uuid, currentNameData.name);

                return currentNameData.name;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        public static interface Consumer<T> {
            void accept(T t);
        }

        public static abstract class Acceptor<T> implements Runnable {

            private Consumer<T> consumer;

            public Acceptor(Consumer<T> consumer) {
                this.consumer = consumer;
            }

            public abstract T getValue();

            @Override
            public void run() {
                consumer.accept(getValue());
            }

        }
    }

}