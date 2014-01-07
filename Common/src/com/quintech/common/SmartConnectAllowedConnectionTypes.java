package com.quintech.common;

public class SmartConnectAllowedConnectionTypes 
{
	public enum ConnectionType
	{
		Unknown(0),
		WiFi_Open(603),
		WiFi_Verizon(600),
		WiFi_Corporate(601),
		WiFi_Private(602),
		Cell_HomeNetwork(700),
		Cell_RoamingNetwork(701);
		
		private int value;    
		private ConnectionType(int value) 
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}
	}
	
	
	public int id = 0;
	public int rank = 0;
	
	public SmartConnectAllowedConnectionTypes()
	{
		
	}
	
	public SmartConnectAllowedConnectionTypes(int id, int rank)
	{
		this.id = id;
		this.rank = rank;
	}
}
