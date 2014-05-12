package com.hp.orders.consts;

public class Consts {

	//the default color of the rule
    public static final String GRAY= "gray";
    //the orange color with rgb format of the rule
    public static final String ORANGE_RGB = "rgb(255, 165, 0)";
    //the red color with rgb format of the rule
    public static final String RED_RGB = "rgb(255, 0, 0)";
    //the yellow color with rgb format of the rule
    public static final String YELLOW_RGB = "rgb(255, 222, 0)";
    
    //the colors which are stored in persistency
    public static final String ORANGE = "orange";
    public static final String RED = "red";
    public static final String YELLOW = "yellow";
    
    // key for orders
    public static final String KEY_PREFIX_ORDER = "MyApprovalsOrders.";
    
    // key for rules
    public static final String KEY_PREFIX_RULES = "MyApprovalsRules.";
    
    //the default of the limitvalue of the rule
    public static final int DEFAULELIMITVALUE = 0;
    //the numbers of the rule
    public static final int NUMBEROFRULES = 3;
    
    
    //public static final String RULE_LIST = "RULE_LIST";
    //the first parameter of the persistency API
	public static final String Order_Admin = "orderAdmin";
	
	//the flags for the first login
	public static final String FIRSTTIMELOGIN = "Y";
	public static final String NOTFIRSTTIMELOGIN = "N";
	
	//the keys which are used to structure the url
	public static final String PROTOCOL_KEY_NAME = "Protocol";
    public static final String HOSTNAME_KEY_NAME = "HostName";
    public static final String PORT_KEY_NAME = "Port";

}
