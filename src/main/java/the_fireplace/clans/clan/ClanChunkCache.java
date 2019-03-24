package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.*;
import net.minecraftforge.fml.common.FMLCommonHandler;
import the_fireplace.clans.Clans;
import the_fireplace.clans.compat.dynmap.data.ClanDimInfo;
import the_fireplace.clans.util.ChunkPosition;

import java.io.*;
import java.util.*;

public class ClanChunkCache {

    private static boolean isLoaded = false;
    private static boolean isChanged = false;
    private static HashMap<UUID, Set<ChunkPosition>> claimedChunks = Maps.newHashMap();

    public static Set<ChunkPosition> getChunks(UUID clan) {
        if(!isLoaded)
            load();
        Set<ChunkPosition> claimed = claimedChunks.get(clan);
        return claimed != null ? claimed : Collections.emptySet();
    }

    public static Set<NewClan> clansWithClaims() {
        if(!isLoaded)
            load();
        Set<NewClan> claimClans = Sets.newHashSet();
        for(UUID clanId: claimedChunks.keySet())
            claimClans.add(ClanCache.getClanById(clanId));
        return claimClans;
    }

    public static void addChunk(NewClan clan, int x, int z, int dim) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        claimedChunks.get(clan.getClanId()).add(new ChunkPosition(x, z, dim));
        Clans.getDynmapCompat().queueClaimEventReceived(new ClanDimInfo(clan.getClanId().toString(), dim, clan.getClanName(), clan.getDescription(), clan.getColor()));
        isChanged = true;
    }

    public static void delChunk(NewClan clan, int x, int z, int dim) {
        if(!isLoaded)
            load();
        claimedChunks.putIfAbsent(clan.getClanId(), Sets.newHashSet());
        if(claimedChunks.get(clan.getClanId()).remove(new ChunkPosition(x, z, dim)))
            isChanged = true;
    }

    private static void load() {
        read(getFile());
        isLoaded = true;
    }

    private static File getFile() {
        return new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "chunkclancache.json");
    }

    private static void read(File file) {
        JsonParser jsonParser = new JsonParser();
        try {
            Object obj = jsonParser.parse(new FileReader(file));
            if(obj instanceof JsonObject) {
                JsonObject jsonObject = (JsonObject) obj;
                JsonArray claimedChunkMap = jsonObject.get("claimedChunks").getAsJsonArray();
                for (int i = 0; i < claimedChunkMap.size(); i++) {
                    Set<ChunkPosition> positions = Sets.newHashSet();
                    for (JsonElement element : claimedChunkMap.get(i).getAsJsonObject().get("value").getAsJsonArray())
                        positions.add(new ChunkPosition(element.getAsJsonObject().get("x").getAsInt(), element.getAsJsonObject().get("z").getAsInt(), element.getAsJsonObject().get("d").getAsInt()));
                    claimedChunks.put(UUID.fromString(claimedChunkMap.get(i).getAsJsonObject().get("key").getAsString()), positions);
                }
            } else
                Clans.LOGGER.warn("Claim Cache not found! This is normal when no chunks have been claimed on Clans 1.2.0 and above.");
        } catch (FileNotFoundException e) {
            //do nothing, it just hasn't been created yet
        } catch (Exception e) {
            e.printStackTrace();
        }
        isChanged = false;
    }

    public static void save() {
        write(getFile());
    }

    private static void write(File location) {
        if(!isChanged)
            return;
        JsonObject obj = new JsonObject();
        JsonArray claimedChunkMap = new JsonArray();
        for(Map.Entry<UUID, Set<ChunkPosition>> position : claimedChunks.entrySet()) {
            JsonArray positionArray = new JsonArray();
            for(ChunkPosition pos: position.getValue()) {
                JsonObject chunkPositionObject = new JsonObject();
                chunkPositionObject.addProperty("x", pos.posX);
                chunkPositionObject.addProperty("z", pos.posZ);
                chunkPositionObject.addProperty("d", pos.dim);
                positionArray.add(chunkPositionObject);
            }
            JsonObject entry = new JsonObject();
            entry.addProperty("key", position.getKey().toString());
            entry.add("value", positionArray);
            claimedChunkMap.add(entry);
        }
        obj.add("claimedChunks", claimedChunkMap);
        try {
            FileWriter file = new FileWriter(location);
            String str = obj.toString();
            file.write(str);
            file.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        isChanged = false;
    }
}