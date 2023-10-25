package no.runsafe.ItemControl.trading;

import no.runsafe.framework.api.database.IRow;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemTagIDRepository extends Repository
{
	public ItemTagIDRepository()
	{
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "itemTagIDs";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate updates = new SchemaUpdate();

		updates.addQueries(
			"CREATE TABLE `" + getTableName() + "` (" +
				"`name` VARCHAR(32) NOT NULL," +
				"`ID` int NOT NULL DEFAULT 0," +
				"PRIMARY KEY (`name`)" +
			");"
		);

		return updates;
	}

	public void createNewTag(String tagName)
	{
		database.execute("INSERT INTO `" + getTableName() + "` (`name`) VALUES(?);", tagName);
	}

	public void deleteTag(String tagName)
	{
		database.execute("DELETE FROM `" + getTableName() + "` WHERE `name` = ?;", tagName);
	}

	public int incrementID(String tagName)
	{
		// get current id
		int id = database.queryInteger("SELECT `ID` FROM `" + getTableName() + "` WHERE `name` = ?;", tagName);

		// increment id
		database.execute("UPDATE `" + getTableName() + "` SET `ID` = ? WHERE `name` = ?;", id + 1, tagName);
		return id;
	}

	public List<String> getTags()
	{
		return database.queryStrings("SELECT `name` FROM `" + getTableName() + "`;");
	}

	public Map<String, Integer> getTagInfo()
	{
		HashMap<String, Integer> result = new LinkedHashMap<>();
		for (IRow row : database.query("SELECT `name`, `ID` FROM `" + getTableName() + "`;"))
			result.put(row.String("name"), row.Integer("ID"));

		return result;
	}
}
