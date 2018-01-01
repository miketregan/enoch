package com.mtregan.enoch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private YearDataService yearDataService;

	@RequestMapping("/")
	public String home(@RequestParam(required=false, defaultValue="2016", name="year") Integer year, 
					   @RequestParam(required=false, defaultValue="1", name="number") Integer number, 
					   @RequestParam(required=false, defaultValue="0", name="intercalation") Integer intercalation,
					   Model model) {
		List<YearData> years = yearDataService.getYearsData(year, number, intercalation);
		logger.debug("Returned {} years", years.size());
		
		model.addAttribute("years", years);
		return "home";
	}
}
