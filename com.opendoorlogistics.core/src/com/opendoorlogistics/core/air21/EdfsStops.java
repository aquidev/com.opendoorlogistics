package com.opendoorlogistics.core.air21;

public class EdfsStops {

	private String id;
	private String jobId;
	private String type;
	private String name;
	private String address;
	private String latitude;
	private String longitude;
	private String serviceDuration;
	private String startTime;
	private String endTime;
	private Integer quantity;
	private Double dimension;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	
	

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getServiceDuration() {
		return serviceDuration;
	}

	public void setServiceDuration(String serviceDuration) {
		this.serviceDuration = serviceDuration;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Double getDimension() {
		return dimension;
	}

	public void setDimension(Double dimension) {
		this.dimension = dimension;
	}

	@Override
	public String toString() {
		return "EdfsStops{" + "id=" + id + ", jobId=" + jobId + ", type=" + type + ", name=" + name + ", address="
				+ address + ", latitude=" + latitude + ", longtitude=" + longitude + ", serviceDuration="
				+ serviceDuration + ", startTime=" + startTime + ", endTime=" + endTime + ", quantity=" + quantity
				+ ", dimension=" + dimension + '}';
	}

}
