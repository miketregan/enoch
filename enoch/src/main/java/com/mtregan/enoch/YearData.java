package com.mtregan.enoch;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YearData {
	private static final Logger logger = LoggerFactory.getLogger(YearData.class);
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDateTime equinox;
	private LocalDate equinoxDay;
	private int intercalation;
	private String intercalationMarker;
	private boolean intercalated;
	private List<MonthData> months = new ArrayList<MonthData>();
	
	public YearData() {
	}
	
	public YearData(List<YearData> lastYears, LocalDateTime equinox, int totalYearCount, int intercalation) {
		this.equinox = equinox.getHour() > 19 ? equinox.plusDays(1) : equinox;
		this.intercalation = intercalation;
		this.equinoxDay = this.equinox.toLocalDate();
		 
		createMonths(intercalate(lastYears, totalYearCount, intercalation));
	}
	
	public YearData(LocalDateTime equinox, int intercalation) {
		this.equinox = equinox;
		this.equinoxDay = equinox.toLocalDate();
		
		if (intercalation == 6) {
			LocalDate date = this.equinoxDay;
			if (equinox.getDayOfWeek() == DayOfWeek.WEDNESDAY ||
				equinox.getDayOfWeek() == DayOfWeek.THURSDAY ||
				equinox.getDayOfWeek() == DayOfWeek.FRIDAY ||
				equinox.getDayOfWeek() == DayOfWeek.SATURDAY) {
				setIntercalated(true);
				date = date.plusDays(1);
			}
			// Find the first wed after the equinox
			while(date.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
				date = date.plusDays(1);
			}
			createMonths(date);
		}
		else {
			if (intercalation == 3) {
				setIntercalated(true);
			}
			if (equinox.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
				createMonths(equinox.toLocalDate());
			}
			else {
				// Find the first wed after the equinox
				LocalDate date = this.equinoxDay;
				while(date.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
					date = date.plusDays(1);
				}
				createMonths(date);
			}
		}
	}
	
	private LocalDate intercalate(List<YearData> lastYears, int totalYearCount, int method) {
		switch(method) {
		case 0:
			if (lastYears != null && !lastYears.isEmpty()) {
				return lastYears.get(lastYears.size()-1).getEndDate().plusDays(1);
			}
			return getEquinoxDay().plusDays(1);
		case 1:
			if (lastYears != null && !lastYears.isEmpty()) {
				return intercalateNumberOfYears(lastYears, 7);
			}
			return getEquinoxDay().plusDays(1);
		case 2:
			LocalDate lastDay = getEquinoxDay();
			if (lastYears != null && !lastYears.isEmpty()) {
				lastDay = lastYears.get(lastYears.size()-1).getEndDate().plusDays(1);
			}
			if (lastDay.isBefore(getEquinoxDay())) {
				lastDay = getEquinoxDay();
			}
			while(lastDay.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
				lastDay = lastDay.plusDays(1);
				setIntercalated(true);
			}
			return lastDay;
		case 3:
			if (lastYears != null && !lastYears.isEmpty()) {
				// We can only intercalate if the last intercalation was >= 3 years and never above 8 years
				YearData lastIntercalationYear = getLastIntercalatedYear(lastYears);
				
				if (lastIntercalationYear != null) {
					// We will always intercalate on 3 or 5 years
					int yearDifference = getEquinoxDay().getYear() - lastIntercalationYear.getEquinoxDay().getYear();
					logger.debug("Year difference = {}", yearDifference);
					
					if ( yearDifference == 3 || yearDifference == 5 || yearDifference == 6) {
						if (yearDifference == 3 && lastIntercalationYear.getIntercalationMarker() == "M") {
							setIntercalated(true);
							setIntercalationMarker("B");
							return lastYears.get(lastYears.size()-1).getEndDate().plusDays(8); 
						}
						else if (yearDifference == 3 && lastIntercalationYear.getIntercalationMarker() == "E") {
							setIntercalationMarker("M");
							return lastYears.get(lastYears.size()-1).getEndDate().plusDays(1);
						}
						else if (yearDifference == 5 && lastIntercalationYear.getIntercalationMarker() == null) {
							setIntercalated(true);
							setIntercalationMarker("E");
							return lastYears.get(lastYears.size()-1).getEndDate().plusDays(8); 
						}
						else if (yearDifference == 5 && lastIntercalationYear.getIntercalationMarker() == "B") {
							// This can be an E or a blue moon
							// TODO
							setIntercalated(true);
							setIntercalationMarker("E");
							return lastYears.get(lastYears.size()-1).getEndDate().plusDays(8); 
						}
					}
					else {
						// No need to intercalate
						logger.debug("No need to intercalate for {}", getEquinoxDay().getYear());
					}
				}
				else {
					logger.debug("No intercalated year for {}", getEquinoxDay().getYear());
				}
				return lastYears.get(lastYears.size()-1).getEndDate().plusDays(1);
			}
			return getEquinoxDay().plusDays(1);
		case 4:
			if (lastYears != null && !lastYears.isEmpty()) {
				YearData lastYear = lastYears.get(lastYears.size()-1);
				if ( Math.abs(lastYear.getEndDate().plusDays(1).getDayOfMonth() - getEquinoxDay().getDayOfMonth()) > 3) {
					setIntercalated(true);
					return lastYear.getEndDate().plusDays(8); 
				}
			}
			else {
				return getEquinoxDay().plusDays(1);
			}
		case 5:
			if (lastYears != null && !lastYears.isEmpty()) {
				return intercalateNumberOfYears(lastYears, 5);
			}
			else {
				return getEquinoxDay().plusDays(1);
			}
		case 6:
			if (lastYears != null && !lastYears.isEmpty()) {
				return getWedAfterEquinox(lastYears);
			}
			else {
				return getWedAfterEquinox(this);
			}
		case 8:
			if (lastYears != null && !lastYears.isEmpty()) {
				return intercalateNumberOfYears(lastYears, 8);
			}
			else {
				return getEquinoxDay().plusDays(1);
			}
		default:
			if (lastYears != null && !lastYears.isEmpty()) {
				return lastYears.get(lastYears.size()-1).getEndDate().plusDays(1);
			}
		}
		return getEquinoxDay().plusDays(1);
	}
	
	private LocalDate getWedAfterEquinox(List<YearData> years) {
		YearData lastYear = years.get(years.size()-1);
		
		if (getEquinoxDay().isBefore(lastYear.getEndDate())) {
			return lastYear.getEndDate().plusDays(1);
		}
		else {
			return getWedAfterEquinox(this);
		}
	}
	
	private LocalDate getWedAfterEquinox(YearData year) {
		if (year.getEquinoxDay().getDayOfWeek() == DayOfWeek.WEDNESDAY ||
			year.getEquinoxDay().getDayOfWeek() == DayOfWeek.THURSDAY ||
			year.getEquinoxDay().getDayOfWeek() == DayOfWeek.FRIDAY ||
			year.getEquinoxDay().getDayOfWeek() == DayOfWeek.SATURDAY) {
			
			year.setIntercalated(true);
			return getNextWed(year.getEquinoxDay().plusDays(1));
		}
		else {
			return getNextWed(year.getEquinoxDay());
		}
	}
	
	private LocalDate getNextWed(LocalDate date) {
		LocalDate d = date;
		while(d.getDayOfWeek() != DayOfWeek.WEDNESDAY) {
			d = d.plusDays(1);
		}
		return d;
	}
		
	private YearData getLastIntercalatedYear(List<YearData> years) {
		YearData lastIntercalationYear = null;
		
		logger.debug("Getting last intercalated year for {}", getEquinoxDay().getYear());
		if (years != null && !years.isEmpty()) {
			int lastYearIndex = years.size()-1;
			YearData lastYear = years.get(lastYearIndex);
			while (lastIntercalationYear == null && lastYearIndex >= 0) {
				logger.debug("Checking {}", lastYear.getEquinox().getYear());
				if (lastYear.intercalated || lastYear.getIntercalationMarker() != null) {
					logger.debug("Year {} is intercalated", lastYear.getEquinox().getYear());
					lastIntercalationYear = lastYear;
				}
				else {
					if (lastYearIndex > 0) {
						lastYear = years.get(--lastYearIndex);
					}
					else {
						--lastYearIndex;
					}
				}
			}
		}
		return lastIntercalationYear;
	}
	
	private LocalDate intercalateNumberOfYears(List<YearData> years, int numberOfYears) {
		YearData lastIntercalatedYear = getLastIntercalatedYear(years);
		if (lastIntercalatedYear == null) {
			if (years.size() == (numberOfYears-1)) {
				setIntercalated(true);
				return years.get(years.size() - 1).getEndDate().plusDays(8);
			}
			else if (!years.isEmpty()) {
				return years.get(years.size() - 1).getEndDate().plusDays(1);
			}
			else {
				return getEquinoxDay().plusDays(1);
			}
		}
		else {
			if ( (getEquinoxDay().getYear() - lastIntercalatedYear.getEquinox().getYear()) >= numberOfYears) {
				setIntercalated(true);
				return years.get(years.size() - 1).getEndDate().plusDays(8);
			}
			else {
				return years.get(years.size() - 1).getEndDate().plusDays(1);
			}
		}
	}
	
	public DayData getDay(LocalDateTime day) {
		LocalDate dayOnly = day.toLocalDate();
		for (MonthData month : months) {
			for (WeekData week : month.getWeeks()) {
				for (DayData weekDay : week.getDays()) {
					if (weekDay.getDay() != null && dayOnly.isEqual(weekDay.getDay())) {
						return weekDay;
					}
				}
			}
		}
		
		return null;
	}
	
	private void createMonths(LocalDate startDate) {
		MonthData month1 = addMonth(createMonth(1, startDate.minusDays(1), 30));
		this.startDate = month1.getStartDate();
		
		MonthData month2 = addMonth(createMonth(2, month1.getEndDate(), 30));
		MonthData month3 = addMonth(createMonth(3, month2.getEndDate(), 31));
		MonthData month4 = addMonth(createMonth(4, month3.getEndDate(), 30));
		MonthData month5 = addMonth(createMonth(5, month4.getEndDate(), 30));
		MonthData month6 = addMonth(createMonth(6, month5.getEndDate(), 31));
		MonthData month7 = addMonth(createMonth(7, month6.getEndDate(), 30));
		MonthData month8 = addMonth(createMonth(8, month7.getEndDate(), 30));
		MonthData month9 = addMonth(createMonth(9, month8.getEndDate(), 31));
		MonthData month10 = addMonth(createMonth(10, month9.getEndDate(), 30));
		MonthData month11 = addMonth(createMonth(11, month10.getEndDate(), 30));
		MonthData month12 = addMonth(createMonth(12, month11.getEndDate(), 31));
		
		this.endDate = month12.getEndDate();
	}
	
	private MonthData addMonth(MonthData monthData) {
		this.months.add(monthData);
		return monthData;
	}
	
	public LocalTime getEquinoxTime() {
		return equinox != null ? equinox.toLocalTime() : null;
	}
	
	
	private MonthData createMonth(int number, LocalDate lastMonthStartDate, int days) {
		return new MonthData(number, lastMonthStartDate.plusDays(1), days);
	}

	public LocalDateTime getEquinox() {
		return equinox;
	}

	public void setEquinox(LocalDateTime equinox) {
		this.equinox = equinox;
	}

	public List<MonthData> getMonths() {
		return months;
	}

	public void setMonths(List<MonthData> months) {
		this.months = months;
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

	public LocalDate getEquinoxDay() {
		return equinoxDay;
	}

	public void setEquinoxDay(LocalDate equinoxDay) {
		this.equinoxDay = equinoxDay;
	}

	public boolean isIntercalated() {
		return intercalated;
	}

	public void setIntercalated(boolean intercalated) {
		this.intercalated = intercalated;
	}

	public int getIntercalation() {
		return intercalation;
	}

	public String getIntercalationMarker() {
		return intercalationMarker;
	}

	public void setIntercalation(int intercalation) {
		this.intercalation = intercalation;
	}

	public void setIntercalationMarker(String intercalationMarker) {
		this.intercalationMarker = intercalationMarker;
	}
}
