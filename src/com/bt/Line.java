package com.bt;

import java.time.LocalTime;
import java.util.Optional;

public class Line {

	private String username;
	private Status status;
	private Optional<LocalTime> duration;

	public Line() {
		// TODO Auto-generated constructor stub
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		this.status =status;
	}

	public Optional<LocalTime> getDuration() {
		return duration;
	}

	public void setDuration(Optional<LocalTime> duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "Line [username=" + username + ", status=" + status + ", duration=" + duration + "]";
	}

	
}
