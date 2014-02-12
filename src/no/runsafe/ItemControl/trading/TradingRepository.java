package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.database.*;
import no.runsafe.framework.minecraft.inventory.RunsafeInventory;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TradingRepository extends Repository
{
	public TradingRepository(IDatabase database)
	{
		this.database = database;
	}

	public HashMap<String, String> getTraders()
	{
		HashMap<String, String> traders = new HashMap<String, String>(0);
		for (IRow row : database.query("SELECT `ID`, `inventory` FROM `traders`"))
			traders.put(row.String("ID"), row.String("inventory"));

		return traders;
	}

	public void persistTraders(HashMap<String, RunsafeInventory> traders)
	{
		database.execute("DELETE FROM `traders`");
		for (Map.Entry<String, RunsafeInventory> trader : traders.entrySet())
			database.execute("INSERT INTO `traders` (`ID`, `inventory`) VALUES(?, ?)", trader.getKey(), trader.getValue().serialize());
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

		return updates;
	}
}
