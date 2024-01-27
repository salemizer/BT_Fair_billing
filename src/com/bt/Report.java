package com.bt;

import java.util.List;
import java.util.Map;

import com.bt.domain.Line;
import com.bt.domain.Session;

public class Report {
	
	private static void generateReport(String path) {
		List<Line> lines = SessionLog.readFile("c:\\mystuff\\bt.txt");
		if(lines != null && lines.size() > 0) {
			lines.stream().forEach(System.out::println);
			System.out.println("earliest= " + SessionLog.getEarliest() + ", latest= " + SessionLog.getLatest() + "\n");
			Map<String, List<Session>> userSessions = SessionLog.mapUserToSession(lines);
			if(userSessions.size() > 0) {
				userSessions.entrySet().stream().forEach(i -> System.out.println(i.toString()));
				userSessions.entrySet().stream().forEach(i -> System.out.println(i.getKey() + " "+ i.getValue().size()));
			}
		}
	}

	public static void main(String... args) {
//		try {
//			if(null == args[0] || args[0].isEmpty()) {
//				System.err.println("File is either empty or invalid! please verify and try again..");
//				return;
//			}
//		}catch(ArrayIndexOutOfBoundsException ex) {
//			ex.getMessage();
//			return;
//		}
		
//		generateReport.generateReport(args[0]);
		generateReport("c:\\mystuff\\bt.txt");
	}
}
