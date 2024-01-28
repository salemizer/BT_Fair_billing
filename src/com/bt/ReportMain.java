package com.bt;

import java.util.List;
import java.util.Map;

import com.bt.domain.Line;
import com.bt.domain.Session;
import com.bt.domain.UserReport;

public class ReportMain {
	
	private static void generateReport(String path) {
		List<Line> lines = ReportGenerator.readFile(path);
		if(lines.size() > 0) {
//			lines.stream().forEach(System.out::println);
//			System.out.println("earliest= " + ReportGenerator.getEarliest() + ", latest= " + ReportGenerator.getLatest() + "\n");
			List<UserReport> userReports = ReportGenerator.generateUserReports(lines);
			if(userReports != null && userReports.size() > 0) {
				userReports.stream().forEach(System.out::println);
			}
		}
	}

	public static void main(String... args) {
		try {
			if(null == args[0] || args[0].isEmpty()) {
				System.err.println("File is either empty or invalid! please verify and try again..");
				return;
			}
		}catch(ArrayIndexOutOfBoundsException ex) {
			ex.getMessage();
			return;
		}
		generateReport(args[0]);
//		generateReport("c:\\mystuff\\bt.txt");
	}
}
