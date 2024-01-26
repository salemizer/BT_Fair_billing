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


public class ReadLog {

	static Optional<LocalTime> earliest = Optional.empty();
	static Optional<LocalTime> latest = Optional.empty();
	
	public ReadLog() {
		// TODO Auto-generated constructor stub
	}

//	14:02:03 ALICE99 Start
//	14:02:05 CHARLIE End
//	14:02:34 ALICE99 End
//	14:02:58 ALICE99 Start
//	14:03:02 CHARLIE Start
//	14:03:33 ALICE99 Start
//	14:03:35 ALICE99 End
//	14:03:37 CHARLIE End
//	14:04:05 ALICE99 End
//	14:04:23 ALICE99 End
//	14:04:41 CHARLIE Start

	private static void updateEarliest(Optional<LocalTime> duration) {
		if(!earliest.isPresent()) {
			earliest = duration;
			return;
		}

	     if(duration.get().compareTo(earliest.get()) < 0) {
	    	 earliest = duration;
	     }
	}
	
	
	private static void updateLatest(Optional<LocalTime> duration) {
		if(!latest.isPresent()) {
			latest = duration;
			return;
		}
			
	     if(duration.get().compareTo(latest.get()) > 0) {
	    	 latest = duration;
	     }
	}
	
	
	static String[] validateLine(String line) {
		
		if(null == line || line.isEmpty())
			return null;
		
		line = line.trim();
		String[]arr = new String[3];
		
		try {
			arr[0] = line.substring(0, line.indexOf(" ")); 
			arr[1] = line.substring(line.indexOf(" ") +1, line.lastIndexOf(" "));
			arr[2] = line.substring(line.lastIndexOf(" ") +1, line.length());
			
			if(arr[0].isEmpty() || arr[1].isEmpty() || arr[2].isEmpty())
				return null;
			
		}catch(IndexOutOfBoundsException outOfBoundsEx) {
			outOfBoundsEx.getMessage();
			return null;
		}
		
		return arr;
	}
	
	static void init(String path) {
		
		try {
			List<Line> list = new ArrayList<Line>();
			Scanner scanner = new Scanner(new File(path));
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] lineSubString = validateLine(line);
				
				if(null != lineSubString) {
					Line newLineObj = new Line();
					newLineObj.setDuration(Optional.ofNullable(LocalTime.parse(lineSubString[0])));
					newLineObj.setUsername(lineSubString[1]);
					newLineObj.setStatus(Status.valueOf(lineSubString[2]));
					
					list.add(newLineObj);
					// update earliest & latest records
					updateEarliest(newLineObj.getDuration());
					updateLatest(newLineObj.getDuration());
				}
			}
			
			if(list.size() > 0) {
				  Map<String, List<Session>> map = read(list);
			      
				  if(map.size() > 0)
				     finalise(map);
			}
			 
		}catch(FileNotFoundException fileNotFountEx) {
			fileNotFountEx.getMessage();
		}
	}
	
	
	static void finalise(Map<String, List<Session>> map) {
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			String username = iterator.next();
			List<Session> sessions= map.get(username);
			long totalDuration = 0;
			for(Session session:sessions) {
				if(session.getStart() == null && session.getEnd() != null )
					session.setStart(earliest);
				else if(session.getStart() != null && session.getEnd() == null)
					session.setEnd(latest);
				
				// find min possible total duration
				totalDuration += session.getStart().get().until(session.getEnd().get(), ChronoUnit.SECONDS);
			}
			System.out.println("username: " + username + ", totalDuration= " + totalDuration);
		}
		
		map.entrySet().stream().forEach(i -> System.out.println(i.toString()));
		map.entrySet().stream().forEach(i -> System.out.println(i.getKey() + " "+ i.getValue().size()));	
	}
	
	static Map<String, List<Session>> read(List<Line> list) {
        
		// read line & ignore invalid line (every line must have username, time, status)
		// extract timestamp, username, status
		// keep note of the earliest & latest timestamps as you read line by line
		Map<String, List<Session>> map = new HashMap<String, List<Session>>();
		
		for(Line line: list) {
		   	// assuming that the line is valid
			Session session = null;
			
			// if username not mapped yet!
			if(!map.containsKey(line.getUsername())){
				
				session = new Session();
				session.setUsername(line.getUsername());
				List<Session> sessions = new ArrayList<Session>();

				// initialise sessions
				switch(line.getStatus()){
				case Start: session.setStart(line.getDuration());
					break;
				
				case End: session.setEnd(line.getDuration());
				break;
			 }
				sessions.add(session);
				map.put(line.getUsername(), sessions);
		   }
			
			
			// if username already exists in the map
			else {
				List<Session> sessions= map.get(line.getUsername());
				session = new Session();
				
				switch(line.getStatus()){
				
				case Start:{
					
					session.setUsername(line.getUsername());
					session.setStart(line.getDuration());
					sessions = map.get(line.getUsername());
					sessions.add(session);
					map.replace(line.getUsername(), sessions);
					break;
				}
				
				
				case End:{
					sessions = map.get(line.getUsername());
					 boolean done=false;
					for(int i=sessions.size()-1; i >= 0; i--) {
						session = sessions.get(i);
						if(session.getStart() != null && session.getEnd() == null ) {
							session.setEnd(line.getDuration());
							done = true;
							break;
						}    	
					}

					if(!done) {
//						session = sessions.getLast();
						
//				        if((session.getStart() == null && session.getEnd() != null) || (session.getStart() != null && session.getEnd() != null)) {
						    session = new Session();
						    session.setUsername(line.getUsername());
						    session.setEnd(line.getDuration());
						    sessions.add(session);
//						}	
					}
				}
			}
		}
	}
		
		
		list.stream().forEach(System.out::println);
		System.out.println("earliest= " + earliest + ", latest= " + latest + "\n");
		
	return map;	
}
		

	public static void main(String... args) {
		try {
			if(null == args[0] || args[0].isEmpty())
				return;
		}catch(ArrayIndexOutOfBoundsException ex) {
			return;
		}
		
		init(args[0]);
//		init("c:\\mystuff\\bt.txt");
	}
}
