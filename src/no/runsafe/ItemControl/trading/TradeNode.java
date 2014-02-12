package no.runsafe.ItemControl.trading;

public class TradeNode
{
	public TradeNode(String tradeID)
	{
		testValue= tradeID;
	}

	public String getTestValue()
	{
		return testValue;
	}

	public void setTestValue(String testValue)
	{
		this.testValue = testValue;
	}

	private String testValue;
}
