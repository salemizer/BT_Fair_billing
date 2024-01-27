package com.bt;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import com.bt.domain.*;

public class SessionLog {

	private static Optional<LocalTime> earliestTime = Optional.empty();
	private static Optional<LocalTime> latestTime = Optional.empty();

	public static List<Line> readFile(String path) {

		List<Line> lines = new ArrayList<Line>();
		Scanner scanner = null;

		try {
			scanner = new Scanner(new File(path));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] lineSubString = validateLine(line);

				// validate log time format
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
				LocalTime time = LocalTime.parse(lineSubString[0], formatter);

				if (null != lineSubString) {
					Line newLineObj = new Line();

					newLineObj.setDuration(Optional.ofNullable(time));
					newLineObj.setUsername(lineSubString[1]);
					newLineObj.setStatus(Status.valueOf(lineSubString[2]));

					lines.add(newLineObj);
					// update earliest & latest records
					setEarliest(newLineObj.getDuration());
					setLatest(newLineObj.getDuration());
				}
			}

		} catch (FileNotFoundException fileNotFountEx) {
			System.err.println(fileNotFountEx.getMessage());
			return null;
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			return null;
		} finally {
			scanner.close();
		}

		return lines;
	}

	private static String[] validateLine(String line) {

		if (null == line || line.isEmpty())
			return null;

		line = line.trim();
		String[] arr = new String[3];

		try {
			arr[0] = line.substring(0, line.indexOf(" "));
			arr[1] = line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" "));
			arr[2] = line.substring(line.lastIndexOf(" ") + 1, line.length());

			if (arr[0].isEmpty() || arr[1].isEmpty() || arr[2].isEmpty())
				return null;

		} catch (IndexOutOfBoundsException outOfBoundsEx) {
			System.err.println(outOfBoundsEx.getMessage());
			return null;
		}

		return arr;
	}

	public static Map<String, List<Session>> mapUserToSession(List<Line> list) {

		// read line & ignore invalid line (every line must have username, time, status)
		// extract timestamp, username, status
		// keep note of the earliest & latest timestamps as you read line by line
		Map<String, List<Session>> userSessions = new HashMap<String, List<Session>>();
		;

		for (Line line : list) {
			// assuming that the line is valid
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

		if (userSessions.size() > 0) {
			userSessions = resolveMissingStartEndTime(userSessions);
		}

		return userSessions;
	}

	private static Map<String, List<Session>> resolveMissingStartEndTime(Map<String, List<Session>> userSessions) {
		Iterator<String> iterator = userSessions.keySet().iterator();
		while (iterator.hasNext()) {
			String username = iterator.next();
			List<Session> sessions = userSessions.get(username);
			long totalDuration = 0;
			for (Session session : sessions) {
				if (session.getStart() == null && session.getEnd() != null)
					session.setStart(earliestTime);
				else if (session.getStart() != null && session.getEnd() == null)
					session.setEnd(latestTime);

				// find min possible total duration
				totalDuration += session.getStart().get().until(session.getEnd().get(), ChronoUnit.SECONDS);
			}
			System.out.println("username: " + username + ", totalDuration= " + totalDuration);
		}

		return userSessions;
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