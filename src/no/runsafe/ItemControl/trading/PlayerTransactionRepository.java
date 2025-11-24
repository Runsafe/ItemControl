package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

public class  PlayerTransactionRepository extends Repository
{
	public PlayerTransactionRepository(IDatabase database)
	{
		this.database = database;
	}

	public void deleteTagRecords(String tag)
	{
		database.execute("DELETE FROM `" + getTableName() + " WHERE `tag` = ?", tag);
	}

	public void recordPurchase(IPlayer player, String tag)
	{
		database.execute(
			"INSERT INTO `" + getTableName() + "` (`tag`,`name`,`purchases`) VALUES (?, ?, 1) " +
				"ON DUPLICATE KEY UPDATE `purchases`= purchases + 1",
			tag, player
		);
	}

	public Map<IPlayer, Integer> getTopPlayers(String tag)
	{
		Map<IPlayer, Integer> rosters = new LinkedHashMap<>();
		for (IRow row : database.query(
			"SELECT `player`, `purchases` FROM `" + getTableName() + "` WHERE `tag` = ? " +
				"ORDER BY `purchases` DESC", tag)
		)
		{
			IPlayer player = row.Player("player");
			int purchaces = row.Integer("purchases");

			rosters.put(player, purchaces);
		}
		return rosters;
	}

	public int getPlayerPurchaseNumber(String tag, IPlayer player)
	{
		return database.queryInteger(
		"SELECT * FROM `" + getTableName() + "` WHERE `player` = ? AND `tag` = ?",
			player, tag
		);
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "PlayerTransactionRepository";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate updates = new SchemaUpdate();

		updates.addQueries(
			"CREATE TABLE `PlayerTransactionRepository` (" +
				"`tag` VARCHAR(32) NOT NULL, " +
				"`player` varchar(36) NOT NULL, " +
				"`purchases` int NOT NULL DEFAULT 0 " +
				"PRIMARY KEY(`tag`,`player`)" +
			")"
		);
		return updates;
	}
}
