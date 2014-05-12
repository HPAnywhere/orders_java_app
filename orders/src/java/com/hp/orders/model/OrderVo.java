package com.hp.orders.model;

import java.io.Serializable;


public class OrderVo implements Serializable {

	private static final long serialVersionUID = -3648828071602101290L;
	
	private String id;
	private String amount;
	private String createdDate;
	private String reason;
	private String name;
	private String status;
	private String updatedDate;
	private String deviceInfo;
	private String color;


    public OrderVo() {
    }
    
    public OrderVo(String id, String amount, String createdDate, String reason, String name, String status,String updatedDate,String deviceInfo,String color) {
    	this.id = id;
        this.amount = amount;
        this.createdDate = createdDate;
        this.reason = reason; 
        this.name = name;
        this.status = status;
        this.updatedDate = updatedDate;
        this.deviceInfo = deviceInfo;
        this.color = color;
    }

    public OrderVo(String id) {

        this.id = id;
    }

	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getName() {
		return name;
	}
    
	public void setName(String name) {
		this.name = name;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
    public String getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getDeviceInfo() {
		return deviceInfo;
	}
	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderVo)) return false;

        OrderVo that = (OrderVo) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;

        return result;
    }
}