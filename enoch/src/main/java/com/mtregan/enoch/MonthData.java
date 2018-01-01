package com.mtregan.enoch;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonthData {
	private static final Logger logger = LoggerFactory.getLogger(MonthData.class);
	
	private int number;
	private int days;
	private List<WeekData> weeks = new ArrayList<>();
	private LocalDate startDate;
	private LocalDate endDate;
	
	public MonthData(int number, LocalDate startDate, int days) {
		logger.trace("Start day for month {} of {} days is {}", number, days, startDate);
		this.number = number;
		this.startDate = startDate;
		this.endDate = startDate.plusDays(days-1);
		this.days = days;
		
		logger.trace("Last day of month is {}", endDate);
		createWeeks();
	}
	
	private void createWeeks() {
		LocalDate currentDate = startDate;
		WeekData week = null;
		
		int dayNumber = 1;
		while(dayNumber <= days) {
			if (currentDate.getDayOfWeek() == DayOfWeek.SUNDAY || week == null) {
				week = new WeekData(currentDate);
				weeks.add(week);
			}
			
			week.addDay(dayNumber++, currentDate);
			
			currentDate = currentDate.plusDays(1);
		}
	}
	
	public LocalDate getStartDate() {
		return startDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<WeekData> getWeeks() {
		return weeks;
	}

	public void setWeeks(List<WeekData> weeks) {
		this.weeks = weeks;
	}
	
	
}
