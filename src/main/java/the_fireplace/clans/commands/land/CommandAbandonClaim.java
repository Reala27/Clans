package the_fireplace.clans.commands.land;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.commands.op.OpCommandAbandomClaim;
import the_fireplace.clans.util.ChunkUtils;
import the_fireplace.clans.util.TextStyles;

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

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Chunk c = sender.getEntityWorld().getChunk(sender.getPosition());
		if(c.hasCapability(Clans.CLAIMED_LAND, null)){
			UUID claimFaction = ChunkUtils.getChunkOwner(c);
			if(claimFaction != null) {
				if(claimFaction.equals(selectedClan.getClanId())) {
					if(!Clans.cfg.forceConnectedClaims || !ChunkUtils.hasConnectedClaim(c, selectedClan.getClanId())) {
						OpCommandAbandomClaim.abandonClaim(sender, c, selectedClan);
						ChunkUtils.clearChunkOwner(c);
						sender.sendMessage(new TextComponentString("Claim abandoned!").setStyle(TextStyles.GREEN));
					} else {//We are forcing connected claims and there is a claim connected
						//Prevent creation of disconnected claims
						OpCommandAbandomClaim.abandonClaimWithAdjacencyCheck(sender, c, selectedClan);
					}
				} else
					sender.sendMessage(new TextComponentString("This land does not belong to "+selectedClan.getClanName()).setStyle(TextStyles.RED));
			} else
				sender.sendMessage(new TextComponentString("This land is not claimed.").setStyle(TextStyles.RED));
		} else
			sender.sendMessage(new TextComponentString("Internal error: This chunk doesn't appear to be claimable.").setStyle(TextStyles.RED));
	}
}
