package the_fireplace.clans.commands.op;

import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.OpClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OpCommandSetShield extends OpClanSubCommand {
	@Override
	public int getMinArgs() {
		return 2;
	}

	@Override
	public int getMaxArgs() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/opclan setshield <clan> <duration>";
	}

	@Override
	protected void runFromAnywhere(MinecraftServer server, ICommandSender sender, String[] args) {
		String clan = args[0];
		NewClan c = ClanCache.getClanByName(clan);
		if(c != null) {
			long duration;
			try {
				duration = Long.valueOf(args[1]);
				if(duration < 0)
					duration = 0;
			} catch(NumberFormatException e) {
				sender.sendMessage(new TextComponentString("Improperly formatted shield duration.").setStyle(TextStyles.RED));
				return;
			}
			c.setShield(duration);
			sender.sendMessage(new TextComponentTranslation("Clan shield for %s set to %s minutes!", c.getClanName(), duration).setStyle(TextStyles.GREEN));
		} else
			sender.sendMessage(new TextComponentString("Clan not found.").setStyle(TextStyles.RED));
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		return args.length == 1 ? Lists.newArrayList(ClanCache.getClanNames().keySet()) : Collections.emptyList();
	}
}
