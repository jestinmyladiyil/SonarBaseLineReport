package it.sella.sonar.analyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.sella.sonar.analyzer.model.ReportGeneratorRequest;
import it.sella.sonar.analyzer.service.SonarCoverageAnalyzerService;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SonarCoverageAnalyzerController {

	private final SonarCoverageAnalyzerService service;

	SonarCoverageAnalyzerController(SonarCoverageAnalyzerService service) {
		this.service = service;
	}

	@GetMapping(value = { "/coverage_report", "/coverage_report/{reportType}" })
	public String generateBaseLineReport(@PathVariable(required = false) String reportType) {
		log.debug("SonarCoverageAnalyzerController >> generateBaseLineReport");
		service.generateCoverageReport(new ReportGeneratorRequest("baseline_report".equals(reportType)));
		log.debug("Report created successfully.");
		return "done";
	}

	@PostMapping("/coverage_report")
	public void generateCoverageReport(@RequestBody ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerController >> generateCoverageReport");
		service.generateCoverageReport(request);
		log.debug("Report created successfully.");
	}

}
