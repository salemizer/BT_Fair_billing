package com.bt;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;


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
	
	
	static void init() {
		
		Line record1 = new Line();
		record1.setUsername("ALICE99");
		record1.setStatus(Status.start);
		record1.setDuration(Optional.ofNullable( LocalTime.of(14, 02, 03)));
		
		// update earliest & latest records
		updateEarliest(record1.getDuration());
		updateLatest(record1.getDuration());

		
		Line record2 = new Line();
		record2.setUsername("CHARLIE");
		record2.setStatus(Status.end);
		record2.setDuration(Optional.ofNullable(LocalTime.of(14, 02, 05)));

		updateEarliest(record2.getDuration());
		updateLatest(record2.getDuration());
		
		Line record3 = new Line();
		record3.setUsername("ALICE99");
		record3.setStatus(Status.end);
		record3.setDuration(Optional.ofNullable(LocalTime.of(14, 02, 34)));
		
		updateEarliest(record3.getDuration());
		updateLatest(record3.getDuration());
		
		Line record4 = new Line();
		record4.setUsername("ALICE99");
		record4.setStatus(Status.start);
		record4.setDuration(Optional.ofNullable(LocalTime.of(14, 02, 58)));
		
		updateEarliest(record4.getDuration());
		updateLatest(record4.getDuration());
		
		Line record5 = new Line();
		record5.setUsername("CHARLIE");
		record5.setStatus(Status.start);
		record5.setDuration(Optional.ofNullable(LocalTime.of(14, 03, 02)));
		
		updateEarliest(record5.getDuration());
		updateLatest(record5.getDuration());
		
		Line record6 = new Line();
		record6.setUsername("ALICE99");
		record6.setStatus(Status.start);
		record6.setDuration(Optional.ofNullable(LocalTime.of(14, 03, 33)));
		
		updateEarliest(record6.getDuration());
		updateLatest(record6.getDuration());
		
		Line record7 = new Line();
		record7.setUsername("ALICE99");
		record7.setStatus(Status.end);
		record7.setDuration(Optional.ofNullable(LocalTime.of(14, 03, 35)));
		
		updateEarliest(record7.getDuration());
		updateLatest(record7.getDuration());
		
		Line record8 = new Line();
		record8.setUsername("CHARLIE");
		record8.setStatus(Status.end);
		record8.setDuration(Optional.ofNullable(LocalTime.of(14, 03, 37)));
		
		updateEarliest(record8.getDuration());
		updateLatest(record8.getDuration());
		
		Line record9 = new Line();
		record9.setUsername("ALICE99");
		record9.setStatus(Status.end);
		record9.setDuration(Optional.ofNullable(LocalTime.of(14, 04, 05)));
		
		updateEarliest(record9.getDuration());
		updateLatest(record9.getDuration());
		
		Line record10 = new Line();
		record10.setUsername("ALICE99");
		record10.setStatus(Status.end);
		record10.setDuration(Optional.ofNullable(LocalTime.of(14, 04, 23)));
		
		updateEarliest(record10.getDuration());
		updateLatest(record10.getDuration());
		
		Line record11 = new Line();
		record11.setUsername("CHARLIE");
		record11.setStatus(Status.start);
		record11.setDuration(Optional.ofNullable(LocalTime.of(14, 04, 41)));
		
		updateEarliest(record11.getDuration());
		updateLatest(record11.getDuration());
		
        List<Line> list = new ArrayList<Line>();
        list.add(record1);
        list.add(record2);
        list.add(record3);
        list.add(record4);
        list.add(record5);
        list.add(record6);
        list.add(record7);
        list.add(record8);
        list.add(record9);
        list.add(record10);
        list.add(record11);
        
        Map<String, List<Session>> map = read(list);
        finalise(map);
	}
	
	
	static void finalise(Map<String, List<Session>> map) {
		Iterator<String> iterator = map.keySet().iterator();
		while(iterator.hasNext()) {
			String username = iterator.next();
			List<Session> sessions= map.get(username);
			long totalDuration = 0;
			for(Session session:sessions) {
				if(session.start == null && session.end != null )
					session.start = earliest;
				else if(session.start != null && session.end == null)
					session.end = latest;
				
				// find min possible total duration
				totalDuration += session.start.get().until(session.end.get(), ChronoUnit.SECONDS);
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
				case start: session.start = line.getDuration();
					break;
				
				case end: session.end = line.getDuration();
				break;
			 }
				sessions.add(session);
				map.put(line.getUsername(), sessions);
		   }
			
			
			// if username already exists
			else {
				List<Session> sessions= map.get(line.getUsername());
				session = new Session();
				
				switch(line.getStatus()){
				
				case start:{
					
					session.setUsername(line.getUsername());
					session.start = line.getDuration();
					sessions = map.get(line.getUsername());
					sessions.add(session);
					map.replace(line.getUsername(), sessions);
					break;
				}
				
				
				case end:{
					sessions = map.get(line.getUsername());
					 boolean done=false;
					for(int i=sessions.size()-1; i >= 0; i--) {
						session = sessions.get(i);
						if(session.start != null && session.end == null ) {
							session.end = line.getDuration();
							done = true;
							break;
						}    	
					}

					if(!done) {
//						session = sessions.getLast();
						
//				        if((session.start == null && session.end != null) || (session.start != null && session.end != null)) {
						    session = new Session();
						    session.setUsername(line.getUsername());
						    session.end = line.getDuration();
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
		init();
	}
}
