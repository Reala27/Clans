package the_fireplace.clans.raid;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.util.TextStyles;

import java.util.*;

public final class RaidingParties {
	private static HashMap<NewClan, Raid> raids = Maps.newHashMap();
	private static HashMap<UUID, Raid> raidingPlayers = Maps.newHashMap();
	private static ArrayList<NewClan> raidedClans = Lists.newArrayList();
	private static HashMap<NewClan, Raid> activeraids = Maps.newHashMap();
	private static HashMap<NewClan, Integer> bufferTimes = Maps.newHashMap();

	public static HashMap<NewClan, Raid> getRaids() {
		return raids;
	}

	public static Raid getRaid(String name){
		return raids.get(ClanCache.getClanByName(name));
	}

	public static Raid getRaid(NewClan target){
		return raids.get(target);
	}

	public static Raid getRaid(EntityPlayerMP player){
		return raidingPlayers.get(player.getUniqueID());
	}

	public static Set<UUID> getRaidingPlayers() {
		return raidingPlayers.keySet();
	}

	public static boolean hasActiveRaid(NewClan clan){
		return activeraids.containsKey(clan);
	}

	public static Raid getActiveRaid(NewClan clan){
		return activeraids.get(clan);
	}

	public static Collection<Raid> getActiveRaids() {
		return activeraids.values();
	}

	public static boolean isRaidedBy(NewClan c, UUID player) {
		return hasActiveRaid(c) && activeraids.get(c).getMembers().contains(player);
	}

	static void addRaid(NewClan clan, Raid raid){
		raids.put(clan, raid);
		raidedClans.add(raid.getTarget());
	}

	static void removeRaid(Raid raid) {
		raids.remove(raid.getTarget());
		raidedClans.remove(raid.getTarget());
	}

	public static void addRaider(UUID raider, Raid raid){
		raidingPlayers.put(raider, raid);
	}

	public static void removeRaider(UUID raider){
		raidingPlayers.remove(raider);
	}

	public static void decrementBuffers() {
		for(Map.Entry<NewClan, Integer> entry : bufferTimes.entrySet()) {
			if(entry.getValue() <= 1) {
				bufferTimes.remove(entry.getKey());
				activateRaid(entry.getKey());
			} else
				bufferTimes.put(entry.getKey(), entry.getValue() - 1);
		}
	}

	public static boolean isPreparingRaid(NewClan targetClan) {
	    return bufferTimes.containsKey(targetClan);
    }

	public static void initRaid(NewClan raidTarget) {
		bufferTimes.put(raidTarget, Clans.cfg.raidBufferTime);
		for(EntityPlayerMP member: raidTarget.getOnlineMembers().keySet())
			member.sendMessage(new TextComponentTranslation("A raiding party with %s members is preparing to raid %s. The raid will begin in %s seconds.", raids.get(raidTarget).getMemberCount(), raidTarget.getClanName()));
		for(UUID member: getRaids().get(raidTarget).getMembers())
			Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member)).sendMessage(new TextComponentTranslation("Your raiding party, with %s members, is preparing to raid %s. The raid will begin in %s seconds.", raids.get(raidTarget).getMemberCount(), raidTarget.getClanName(), Clans.cfg.raidBufferTime).setStyle(TextStyles.GREEN));
	}

	private static void activateRaid(NewClan raidTarget) {
		Raid startingRaid = raids.remove(raidTarget);
		startingRaid.activate();
		activeraids.put(startingRaid.getTarget(), startingRaid);
	}

	public static void endRaid(NewClan targetClan, boolean raiderVictory) {
		TextComponentTranslation defenderMessage = new TextComponentTranslation("The raid against %s has ended!", targetClan.getClanName());
		if(raiderVictory)
			defenderMessage.appendSibling(new TextComponentString("The raiders were victorious!")).setStyle(TextStyles.YELLOW);
		else
			defenderMessage.appendSibling(new TextComponentTranslation("%s was victorious!", targetClan.getClanName())).setStyle(TextStyles.GREEN);
		for(EntityPlayerMP member: targetClan.getOnlineMembers().keySet())
			member.sendMessage(defenderMessage);

		TextComponentTranslation raiderMessage = new TextComponentTranslation("The raid against %s has ended!", targetClan.getClanName());
		if(raiderVictory)
			raiderMessage.appendSibling(new TextComponentString("The raiders were victorious!")).setStyle(TextStyles.GREEN);
		else
			raiderMessage.appendSibling(new TextComponentTranslation("%s was victorious!", targetClan.getClanName())).setStyle(TextStyles.YELLOW);
		for(UUID member: getActiveRaid(targetClan).getInitMembers()) {
			EntityPlayerMP player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(member);
			if(player != null)
				player.sendMessage(raiderMessage);
		}
		Raid raid = activeraids.remove(targetClan);
		for(UUID player: raid.getMembers())
			removeRaider(player);
		raidedClans.remove(targetClan);
		for(ResourceLocation location: DimensionManager.getRegistry().getKeys()) {//TODO Find out if this works on modded dimensions
			DimensionType dimType = DimensionManager.getRegistry().get(location);
			for (Chunk c : ServerLifecycleHooks.getCurrentServer().getWorld(dimType).getChunkProvider().getLoadedChunks()) {
				NewChunkRestoreData data = NewRaidRestoreDatabase.popChunkRestoreData(dimType.getId(), c);
				if (data != null)
					data.restore(c);
			}
		}
	}

	public static ArrayList<NewClan> getRaidedClans() {
		return raidedClans;
	}
}
