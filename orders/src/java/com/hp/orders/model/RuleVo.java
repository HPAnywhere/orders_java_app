package com.hp.orders.model;

import java.io.Serializable;

public class RuleVo implements Serializable {
	private static final long serialVersionUID = -1977486983088893521L;
	
	private String id;
	private String name;
	private String lowerLimitValue;	
	private String color;		
	
	public RuleVo(){
		
	}
	
	public RuleVo(String id, String name, String lowerLimitValue, String color){
		this.name = name;
		this.lowerLimitValue = lowerLimitValue;
		this.color = color;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLowerLimitValue() {
		return lowerLimitValue;
	}

	public void setLowerLimitValue(String lowerLimitValue) {
		this.lowerLimitValue = lowerLimitValue;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}