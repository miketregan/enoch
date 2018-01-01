package com.mtregan.enoch;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeekData {
	private static final Logger logger = LoggerFactory.getLogger(WeekData.class);
	
	private List<DayData> days = new ArrayList<>();
	
	public WeekData(LocalDate startDay) {
		if (startDay.getDayOfWeek() != DayOfWeek.SUNDAY) {
			logger.trace("Day of week is {}", startDay.getDayOfWeek());
			int numberOfDays = 0;
			for (int i=0; i<startDay.getDayOfWeek().getValue(); i++) {
				++numberOfDays;
			}
			
			for (int i=numberOfDays; i > 0; i--) {
				logger.trace("Adding day");
				this.days.add(new DayData(startDay.minusDays(i)));
			} 
		}
	}
	
	public void addDay(int dayNumber, LocalDate day) {
		this.days.add(new DayData(dayNumber, day));
	}

	public List<DayData> getDays() {
		return days;
	}

	public void setDays(List<DayData> days) {
		this.days = days;
	}
	
	
}
