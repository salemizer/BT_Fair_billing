package com.bt.domain;

public class UserReport {

	private String username;
	private int totalSession;
	private int totalDuration;
	
	public UserReport() {
		// TODO Auto-generated constructor stub
	}

	public UserReport(String username, int totalSessions, int totalDuration) {
		super();
		this.username = username;
		this.totalSession = totalSessions;
		this.totalDuration = totalDuration;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public int getTotalSession() {
		return totalSession;
	}

	public void setTotalSession(int totalSession) {
		this.totalSession = totalSession;
	}

	public int getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(int totalDuration) {
		this.totalDuration = totalDuration;
	}

	@Override
	public String toString() {
		return username + " " + totalSession + " " + totalDuration;
	}

	
}
