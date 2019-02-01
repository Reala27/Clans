package the_fireplace.clans.clan;

import com.google.common.collect.Maps;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

public class Clan implements Serializable {
	private String clanName, clanBanner;
	private HashMap<UUID, EnumRank> members;
	private UUID clanId;
	private float homeX, homeY, homeZ;

	public Clan(String clanName, UUID leader){
		this(clanName, leader, null);
	}

	public Clan(String clanName, UUID leader, @Nullable String banner){
		this.clanName = clanName;
		this.members = Maps.newHashMap();
		this.members.put(leader, EnumRank.LEADER);
		if(banner != null)
			this.clanBanner = banner;
		do{
			this.clanId = UUID.randomUUID();
		} while(!ClanDatabase.addClan(this.clanId, this));
	}

	public HashMap<UUID, EnumRank> getMembers() {
		return members;
	}

	public UUID getClanId() {
		return clanId;
	}

	public String getClanName() {
		return clanName;
	}

	public void setClanName(String clanName) {
		ClanCache.removeName(this.clanName);
		ClanCache.addName(clanName);
		this.clanName = clanName;
	}

	public String getClanBanner() {
		return clanBanner;
	}

	public void setClanBanner(String clanBanner) {
		ClanCache.removeBanner(this.clanBanner);
		ClanCache.addBanner(clanBanner);
		this.clanBanner = clanBanner;
	}

	public void setHome(BlockPos pos) {
		this.homeX = pos.getX();
		this.homeY = pos.getY();
		this.homeZ = pos.getZ();
	}

	public BlockPos getHome() {
		return new BlockPos(homeX, homeY, homeZ);
	}
}
