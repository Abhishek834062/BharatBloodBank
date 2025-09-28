package com.BharatBlood.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"health","status"})
public class HomeController {

	@GetMapping
	public String healthCheking()
	{
		return "Application is running";
	}
}
