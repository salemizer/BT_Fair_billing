package com.bt.test;

import static org.junit.Assert.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.Ignore;
import org.junit.Test;

import com.bt.ReportGenerator;
import com.bt.domain.Line;
import com.bt.domain.Status;
import com.bt.domain.UserReport;

public class ReportGeneratorTest {

	@Ignore
	@Test
	public void testReadFile_invalidFilePathString() {
		
		String path= "";
		List<Line>lines= new ArrayList<Line>();
		assertEquals(lines, ReportGenerator.readFile(path));
	}
	
	
/*
 * invalid time stamp/irrelevant data
 */
	@Test
	public void testReadFile_invalidInputLines() {
		
		String path= "c:\\mystuff\\bt(2).txt";
		List<Line>lines= new ArrayList<Line>();
		assertEquals (lines, ReportGenerator.readFile(path));
	}
	
	@Ignore
	@Test
	public void testReadFile() {
		
		String path= "c:\\mystuff\\bt(2).txt";
		List<Line>lines= new ArrayList<Line>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		lines.add(new Line("ALICE99", Status.End, Optional.of(LocalTime.parse("14:02:34",formatter))));
		
		assertEquals (lines, ReportGenerator.readFile(path));
	}
	
	
    @Ignore
	@Test
	public void testGenerateUserReports() {
		
		List<UserReport>expectedUserReports= new ArrayList<UserReport>();
		expectedUserReports.add(new UserReport("ALICE99", 1, 2));
		expectedUserReports.add(new UserReport("CHARLIE", 1, 2));
		
		List<Line>lines= new ArrayList<Line>();
		Line line1 =new Line("ALICE99", Status.Start, Optional.of(LocalTime.parse("14:02:03")));
		Line line2= new Line("CHARLIE", Status.End, Optional.of(LocalTime.parse("14:02:05")));
		
		lines.add(line1);
		lines.add(line2);
		
		assertEquals(expectedUserReports, ReportGenerator.generateUserReports(lines));
	}

}
