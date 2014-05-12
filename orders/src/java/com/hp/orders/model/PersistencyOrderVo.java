package com.hp.orders.model;

import java.io.Serializable;


public class PersistencyOrderVo implements Serializable {

	private static final long serialVersionUID = -3648828071602101290L;
	
	private String id;
	private String updatedDate;
	private String deviceInfo;


    public PersistencyOrderVo() {
    }
    
    public PersistencyOrderVo(String id,String updatedDate,String deviceInfo) {
    	this.id = id;
        this.updatedDate = updatedDate;
        this.deviceInfo = deviceInfo;
    }

    public PersistencyOrderVo(String id) {

        this.id = id;
    }
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistencyOrderVo)) return false;

        PersistencyOrderVo that = (PersistencyOrderVo) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

	@Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;

        return result;
    }
}