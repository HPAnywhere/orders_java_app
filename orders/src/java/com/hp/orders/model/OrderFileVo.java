package com.hp.orders.model;

import java.io.Serializable;

public class OrderFileVo {
	
	private static final long serialVersionUID = -3648828071602101290L;

	private String id;
	private String amount;
	private String createdDate;
	private String reason;
	private String name;
	private String status;

	public OrderFileVo() {
	}

	public OrderFileVo(String id, String amount, String createdDate,
			String reason, String name, String status) {
		this.id = id;
		this.amount = amount;
		this.createdDate = createdDate;
		this.reason = reason;
		this.name = name;
		this.status = status;
	}

	public OrderFileVo(String id) {

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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof OrderFileVo))
			return false;

		OrderFileVo that = (OrderFileVo) o;

		if (id != null ? !id.equals(that.id) : that.id != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = id != null ? id.hashCode() : 0;

		return result;
	}

}
