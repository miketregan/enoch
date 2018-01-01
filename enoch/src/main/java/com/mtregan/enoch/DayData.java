package com.mtregan.enoch;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DayData {
	private LocalDateTime moonTime;
	private String phase;
	private int dayNumber = 0;
	private LocalDate day;
	
	public DayData() {
	}
	
	public DayData(LocalDate day) {
		this.day = day;
	}
	
	public DayData(int dayNumber, LocalDate day) {
		this.dayNumber = dayNumber;
		this.day = day;
	}
	
	public LocalTime getMoonDayTime() {
		return moonTime != null ? moonTime.toLocalTime() : null;
	}
	

	public LocalDate getDay() {
		return day;
	}

	public void setDay(LocalDate day) {
		this.day = day;
	}

	public int getDayNumber() {
		return dayNumber;
	}

	public void setDayNumber(int dayNumber) {
		this.dayNumber = dayNumber;
	}

	public LocalDateTime getMoonTime() {
		return moonTime;
	}

	public String getPhase() {
		return phase;
	}

	public void setMoonTime(LocalDateTime moonTime) {
		this.moonTime = moonTime;
	}

	public void setPhase(String phase) {
		this.phase = phase;
	}
	
	
}
