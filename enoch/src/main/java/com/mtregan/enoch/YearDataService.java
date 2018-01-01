package com.mtregan.enoch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.buf.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class YearDataService {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	public List<YearData> getYearsData(int startYear, int numberOfYears, int intercalation) {
		final List<YearData> years = new ArrayList<>();
		
		List<Integer> yearsIn = new ArrayList<>();
		for (int i=0; i<numberOfYears+1; i++) {
			yearsIn.add((startYear + i));
		}
		
		logger.debug("Getting year data for {}", yearsIn);
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("years", yearsIn);
		jdbcTemplate.query(
				"SELECT e.year as eqyear, e.day as eqday, e.hour as eqhour, e.minute as eqmin " +
				"FROM equinox e " + 
				"WHERE e.year in (:years)", parameters, new ResultSetExtractor<Void>() {
			@Override
			public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
				YearData lastYear = null;
				int totalYearCount = 1;
				while (rs.next()) {
					if (lastYear == null) {
						YearData yd = new YearData(LocalDateTime.of(rs.getInt("eqyear"), 3, rs.getInt("eqday"), rs.getInt("eqhour"), rs.getInt("eqmin")), intercalation);
						years.add(yd);
						
						++totalYearCount;
						
						lastYear = yd;
					}
					else {
						YearData yd = new YearData(years, LocalDateTime.of(rs.getInt("eqyear"), 3, rs.getInt("eqday"), rs.getInt("eqhour"), rs.getInt("eqmin")), totalYearCount++, intercalation);
						years.add(yd);
						
						lastYear = yd;
					}
				}
				return null;
			}
		});
		
		
		jdbcTemplate.query(
				"SELECT year, month, day, hour, minute, phase " +
				"FROM moon " +
				"WHERE year in (:years) AND phase in ('N', 'F') " +
				"ORDER BY year, month, day",
				parameters,
				new ResultSetExtractor<Void>() {
					@Override
					public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
						
						while(rs.next()) {
							String phase = rs.getString("phase");
							if (phase.equals("F") || phase.equals("N")) {
								LocalDateTime moonDay = LocalDateTime.of(rs.getInt("year"), rs.getInt("month"), rs.getInt("day"), rs.getInt("hour"), rs.getInt("minute"));
								for (YearData year : years) {
									DayData day = year.getDay(moonDay);
									if (day != null) {
										day.setMoonTime(moonDay);
										day.setPhase(phase);
										break;
									}
								}
							}
						}
						
						return null;
					}
					
				});
		
		return years;
	}
	
	
	public YearData getYearData(int year) {
		logger.debug("Getting year data for {}", year);
		
		List<Integer> yearsIn = new ArrayList<>();
		yearsIn.add(year);
		yearsIn.add(year+1);
		
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("years", yearsIn);
		
		final YearData yearData = jdbcTemplate.query(
				"SELECT e.year as eqyear, e.day as eqday, e.hour as eqhour, e.minute as eqmin " +
				"FROM equinox e " + 
				"WHERE e.year in (:years) ORDER BY e.year", parameters, new ResultSetExtractor<YearData>() {
			@Override
			public YearData extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					YearData yd = new YearData(LocalDateTime.of(rs.getInt("eqyear"), 3, rs.getInt("eqday"), rs.getInt("eqhour"), rs.getInt("eqmin")), 0);
					logger.debug("Created a year of {} months", yd.getMonths().size());
					return yd;
				}
				return null;
			}
		});
		
		
		jdbcTemplate.query(
				"SELECT year, month, day, hour, minute, phase " +
				"FROM moon " +
				"WHERE year in (:years) AND phase in ('N', 'F') " +
				"ORDER BY year, month, day",
				parameters,
				new ResultSetExtractor<Void>() {
					@Override
					public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
						
						while(rs.next()) {
							String phase = rs.getString("phase");
							if (phase.equals("F") || phase.equals("N")) {
								LocalDateTime moonDay = LocalDateTime.of(rs.getInt("year"), rs.getInt("month"), rs.getInt("day"), rs.getInt("hour"), rs.getInt("minute"));
								logger.debug("Phase for {} is {}", moonDay, phase);
								DayData day = yearData.getDay(moonDay);
								if (day != null) {
									day.setMoonTime(moonDay);
									day.setPhase(phase);
								}
							}
						}
						
						return null;
					}
					
				});
		return yearData;
	}
}
