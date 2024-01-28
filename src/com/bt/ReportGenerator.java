package com.bt;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import com.bt.domain.*;

public class ReportGenerator {

	private static Optional<LocalTime> earliestTime = Optional.empty();
	private static Optional<LocalTime> latestTime = Optional.empty();

	/*
	 *  	
	 */
	/**
	 * Reads data from the given file path
	 * 
	 * @param path String
	 * @return lines List<Line>
	 */
	public static List<Line> readFile(String path) {

		List<Line> lines = new ArrayList<Line>();
		Scanner scanner = null;

		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				Line newLineObj = validateLine(line);

				if (newLineObj != null) {
					setEarliest(newLineObj.getDuration());
					setLatest(newLineObj.getDuration());
					lines.add(newLineObj);
				}
			}
		} catch (FileNotFoundException fileNotFountEx) {
			System.err.println(fileNotFountEx.getMessage());
		}

		return lines;
	}

	/**
	 * Substring & validates timestamp, username, start/stop status
	 * 
	 * @param line String
	 * @return Line
	 */
	private static Line validateLine(String line) {

		if (null == line || line.isEmpty())
			return null;

		line = line.trim();
		Line newLineObj = null;

		String durationStr = null;
		String username = null;
		String statusStr = null;

		try {
			durationStr = line.substring(0, line.indexOf(" "));
			username = line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" "));
			statusStr = line.substring(line.lastIndexOf(" ") + 1, line.length());

			if ((durationStr == null || durationStr.isEmpty()) || (username == null || username.isEmpty())
					|| (statusStr == null || statusStr.isEmpty()))
				return null;

			// Only letters and/or numbers allowed
			username = username.trim();
			if (Utility.StringNotContainsSpecialChar(username))
				return null;

			// validate log time format
			LocalTime duration = Utility.parseStringToLocalTime(durationStr, "HH:mm:ss");

			// Validate against Status enum values (throws IllegalArgumentException)
			Status status = Status.valueOf(statusStr);

			newLineObj = new Line(username, status, Optional.ofNullable(duration));

		} catch (RuntimeException runtimeEx) {
		}

		return newLineObj;
	}

	public static List<UserReport> generateUserReports(List<Line> lines) {

		Map<String, List<Session>> userSessions = mapUserToSession(lines);
		List<UserReport> userReports = null;

		if (userSessions.size() > 0) {
			if (!earliestTime.isPresent() || !latestTime.isPresent())
				updateEarliestLatestTime(lines);

			userReports = finaliseUserReports(userSessions);
		}
		return userReports;
	}

	private static Map<String, List<Session>> mapUserToSession(List<Line> lines) {

		Map<String, List<Session>> userSessions = new HashMap<String, List<Session>>();

		for (Line line : lines) {

			Session session = null;

			// if username not mapped yet!
			if (!userSessions.containsKey(line.getUsername())) {

				session = new Session();
				session.setUsername(line.getUsername());
				List<Session> sessions = new ArrayList<Session>();

				// initialise sessions
				switch (line.getStatus()) {
				case Start:
					session.setStart(line.getDuration());
					break;

				case End:
					session.setEnd(line.getDuration());
					break;
				}
				sessions.add(session);
				userSessions.put(line.getUsername(), sessions);
			}

			// if username already exists in the map
			else {
				List<Session> sessions = userSessions.get(line.getUsername());
				session = new Session();

				switch (line.getStatus()) {

				case Start: {

					session.setUsername(line.getUsername());
					session.setStart(line.getDuration());
					sessions = userSessions.get(line.getUsername());
					sessions.add(session);
					userSessions.replace(line.getUsername(), sessions);
					break;
				}

				case End: {
					sessions = userSessions.get(line.getUsername());
					boolean done = false;
					for (int i = sessions.size() - 1; i >= 0; i--) {
						session = sessions.get(i);
						if (session.getStart() != null && session.getEnd() == null) {
							session.setEnd(line.getDuration());
							done = true;
							break;
						}
					}

					if (!done) {
						session = new Session();
						session.setUsername(line.getUsername());
						session.setEnd(line.getDuration());
						sessions.add(session);
					}
				}
				}
			}
		}
		return userSessions;
	}

	/**
	 * Resolves missing start end times & creates UserReport objects
	 * 
	 * @param userSessions map<String, List<Session>>
	 * @return List<UserReport>
	 */
	private static List<UserReport> finaliseUserReports(Map<String, List<Session>> userSessions) {

		List<UserReport> userReports = new ArrayList<UserReport>();
		Iterator<String> iterator = userSessions.keySet().iterator();

		while (iterator.hasNext()) {

			String username = iterator.next();
			List<Session> sessions = userSessions.get(username);
			int totalSession = 0;
			int totalDuration = 0;

			for (Session session : sessions) {

				if (session.getStart() == null && session.getEnd() != null)
					session.setStart(earliestTime);
				else if (session.getStart() != null && session.getEnd() == null)
					session.setEnd(latestTime);

				// count session per user
				totalSession++;
				// find total duration
				totalDuration += session.getStart().get().until(session.getEnd().get(), ChronoUnit.SECONDS);
			}

			userReports.add(new UserReport(username, totalSession, totalDuration));
		}
//		userSessions.entrySet().stream().forEach(i -> System.out.println(i.toString()));
//		userSessions.entrySet().stream().forEach(i -> System.out.println(i.getKey() + " " + i.getValue().size()));
		return userReports;
	}

	/**
	 * Update/set earliestTime & latestTime
	 * 
	 * @param lines List<Line>
	 */
	private static void updateEarliestLatestTime(List<Line> lines) {
		lines.stream().forEach(line -> {
			setEarliest(line.getDuration());
			setLatest(line.getDuration());
		});
	}

	private static void setEarliest(Optional<LocalTime> duration) {
		if (!earliestTime.isPresent()) {
			earliestTime = duration;
			return;
		}

		if (duration.get().compareTo(earliestTime.get()) < 0) {
			earliestTime = duration;
		}
	}

	public static Optional<LocalTime> getEarliest() {
		return earliestTime;
	}

	private static void setLatest(Optional<LocalTime> duration) {
		if (!latestTime.isPresent()) {
			latestTime = duration;
			return;
		}

		if (duration.get().compareTo(latestTime.get()) > 0) {
			latestTime = duration;
		}
	}

	public static Optional<LocalTime> getLatest() {
		return latestTime;
	}

}