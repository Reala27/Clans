package the_fireplace.clans.commands.land;

import com.google.common.collect.Maps;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import the_fireplace.clans.Clans;
import the_fireplace.clans.clan.ClanCache;
import the_fireplace.clans.clan.ClanChunkCache;
import the_fireplace.clans.clan.EnumRank;
import the_fireplace.clans.clan.NewClan;
import the_fireplace.clans.commands.ClanSubCommand;
import the_fireplace.clans.util.TextStyles;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CommandFancyMap extends ClanSubCommand {
	private static final char[] mapchars = {'#', '&', '@', '*', '+', '<', '>', '~', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7', '8', '9', 'w', 'm'};
	@Override
	public EnumRank getRequiredClanRank() {
		return EnumRank.ANY;
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
		return "/clan fancymap";
	}

	@Override
	public void run(@Nullable MinecraftServer server, EntityPlayerMP sender, String[] args) {
		assert server != null;
		World w = sender.getEntityWorld();
		Chunk center = w.getChunk(sender.getPosition());

		Map<UUID, Character> symbolMap = Maps.newHashMap();
		sender.sendMessage(new TextComponentString("=====================================================").setStyle(TextStyles.GREEN));
		new Thread(() -> {
			for(int z=center.z-26; z <= center.z + 26; z++) {
				StringBuilder row = new StringBuilder();
				for (int x = center.x - 26; x <= center.x + 26; x++) {
					String wildernessColor = center.z == z && center.x == x ? "§9" : Clans.cfg.protectWilderness ? "§e" : "§2";
					NewClan clan = ClanChunkCache.getChunkClan(x, z, sender.getServerWorld().provider.getDimension());
					if(clan == null)
						row.append(wildernessColor).append('-');
					else {
						if (!symbolMap.containsKey(clan.getClanId()))
							symbolMap.put(clan.getClanId(), mapchars[symbolMap.size() % mapchars.length]);
						row.append(center.z == z && center.x == x ? "§9": '§'+Integer.toHexString(clan.getTextColor().getColorIndex())).append(symbolMap.get(clan.getClanId()));
					}
				}
				sender.sendMessage(new TextComponentString(row.toString()));
			}
			sender.sendMessage(new TextComponentString("=====================================================").setStyle(TextStyles.GREEN));
			for(Map.Entry<UUID, Character> symbol: symbolMap.entrySet()) {
				NewClan c = ClanCache.getClanById(symbol.getKey());
				sender.sendMessage(new TextComponentString(symbol.getValue() + ": " +(c != null ? c.getClanName() : "Wilderness")).setStyle(new Style().setColor(c != null ? c.getTextColor() : TextFormatting.YELLOW)));
			}
		}).start();
	}
}
