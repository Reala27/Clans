package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.Clans;
import the_fireplace.clans.util.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandAbandonClaim extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ADMIN;
	}

	@Override
	public int getMinArgs() {
		return 0;
	}

	@Override
	public int getMaxArgs() {
		return 0;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan abandonclaim";
	}

	@SuppressWarnings("Duplicates")
	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = ChunkUtils.getChunkOwner(c);
			if(claimFaction != null) {
				if(claimFaction.equals(playerClan.getClanId())) {
					//Unset clan home if it is in the chunk
					if(playerClan.hasHome()
							&& sender.dimension == playerClan.getHomeDim()
							&& playerClan.getHome().getX() >= c.getPos().getXStart()
							&& playerClan.getHome().getX() <= c.getPos().getXEnd()
							&& playerClan.getHome().getZ() >= c.getPos().getZStart()
							&& playerClan.getHome().getZ() <= c.getPos().getZEnd()){
						playerClan.unsetHome();
					}

					playerClan.subClaimCount();
					Clans.getPaymentHandler().addAmount(Clans.cfg.claimChunkCost, playerClan.getClanId());
					ChunkUtils.clearChunkOwner(c);
					sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Claim abandoned!"));
				} else
					sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land does not belong to you."));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "This land is not claimed."));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Internal error: This chunk doesn't appear to be claimable."));
	}
}
