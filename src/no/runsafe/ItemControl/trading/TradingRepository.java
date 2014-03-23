package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.database.*;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TradingRepository extends Repository
{
	public TradingRepository(IDatabase database, IServer server)
	{
		this.server = server;
		this.database = database;
	}

	public List<TraderData> getTraders()
	{
		List<TraderData> data = new ArrayList<TraderData>(0);
		for (IRow row : database.query("SELECT `inventory`, `world`, `x`, `y`, `z` FROM `traders`"))
		{
			RunsafeInventory inventory = server.createInventory(null, 27);
			inventory.unserialize(row.String("inventory"));
			data.add(new TraderData(row.Location(), inventory));
		}

		return data;
	}

	public void persistTrader(TraderData data)
	{
		ILocation location = data.getLocation();
		database.execute(
				"INSERT INTO `traders` (`inventory`, `world`, `x`, `y`, `z`) VALUES(?, ?, ?, ?, ?)",
				data.getInventory().serialize(),
				location.getWorld().getName(),
				location.getX(),
				location.getY(),
				location.getZ()
		);
	}

	public void updateTrader(TraderData data)
	{
		ILocation location = data.getLocation();
		database.execute(
				"UPDATE `traders` SET `inventory` = ? WHERE `world` = ? AND `x` = ? AND `y` = ? AND `z` = ?",
				data.getInventory().serialize(),
				location.getWorld().getName(),
				Math.floor(location.getBlockX()),
				Math.floor(location.getBlockY()) ,
				Math.floor(location.getBlockZ())
		);
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "traders";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate updates = new SchemaUpdate();

		updates.addQueries(
			"CREATE TABLE `traders` (" +
				"`ID` VARCHAR(50) NOT NULL," +
				"`inventory` LONGTEXT NOT NULL," +
				"PRIMARY KEY (`ID`)" +
			")"
		);

		updates.addQueries(
				"DELETE FROM `traders`",
				"ALTER TABLE `traders`" +
					"ADD COLUMN `world` VARCHAR(30) NOT NULL AFTER `inventory`," +
					"ADD COLUMN `x` DOUBLE NOT NULL AFTER `world`," +
					"ADD COLUMN `y` DOUBLE NOT NULL AFTER `x`," +
					"ADD COLUMN `z` DOUBLE NOT NULL AFTER `y`," +
					"ADD COLUMN `yaw` FLOAT NOT NULL AFTER `z`," +
					"ADD COLUMN `pitch` FLOAT NOT NULL AFTER `yaw`," +
					"DROP COLUMN `ID`"
		);

		updates.addQueries("ALTER TABLE `traders` ADD COLUMN `name` VARCHAR(255) NULL DEFAULT NULL AFTER `inventory`");

		updates.addQueries(
				"DELETE FROM `traders`",
				"ALTER TABLE `traders`" +
						"ALTER `x` DROP DEFAULT," +
						"ALTER `y` DROP DEFAULT," +
						"ALTER `z` DROP DEFAULT",
				"ALTER TABLE `traders`" +
						"CHANGE COLUMN `x` `x` INT NOT NULL AFTER `world`," +
						"CHANGE COLUMN `y` `y` INT NOT NULL AFTER `x`," +
						"CHANGE COLUMN `z` `z` INT NOT NULL AFTER `y`," +
						"DROP COLUMN `yaw`," +
						"DROP COLUMN `pitch`," +
						"DROP COLUMN `name`"
		);

		return updates;
	}

	private final IServer server;
}
