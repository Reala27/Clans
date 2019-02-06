package the_fireplace.clans.commands.finance;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import the_fireplace.clans.Clans;
import the_fireplace.clans.MinecraftColors;
import the_fireplace.clans.clan.Clan;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.commands.ClanSubCommand;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandSetRent extends ClanSubCommand {
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.LEADER;
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 1;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/clan setrent <amount>";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		Clan playerClan = ClanCache.getPlayerClan(sender.getUniqueID());
		assert playerClan != null;
		int newRent = Integer.valueOf(args[0]);
		if(newRent >= 0) {
			long maxRent = Clans.cfg.maxRent;
			if(Clans.cfg.multiplyMaxRentClaims)
				maxRent *= playerClan.getClaimCount();
			if(maxRent <= 0 || newRent <= maxRent) {
				playerClan.setRent(newRent);
				sender.sendMessage(new TextComponentString(MinecraftColors.GREEN + "Clan rent set!"));
			} else
				sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Cannot set rent above your maximum("+maxRent+' '+Clans.getPaymentHandler().getCurrencyName(maxRent)+")!"));
		} else
			sender.sendMessage(new TextComponentString(MinecraftColors.RED + "Cannot set negative rent!"));
	}
}