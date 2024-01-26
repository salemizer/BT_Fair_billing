package com.bt;

import java.time.LocalTime;
import java.util.Optional;

public class Session {

	String username;
	Optional<LocalTime> start;
	Optional<LocalTime> end;
	
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Optional<LocalTime> getStart() {
		return start;
	}

	public void setStart(Optional<LocalTime> start) {
		this.start = start;
	}

	public Optional<LocalTime> getEnd() {
		return end;
	}

	public void setEnd(Optional<LocalTime> end) {
		this.end = end;
	}

	@Override
	public String toString() {
		return "Session [username=" + username + ", start=" + start + ", end=" + end + "]";
	}

	
	
}
