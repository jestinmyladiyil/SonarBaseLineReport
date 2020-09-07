package it.sella.sonar.analyzer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.sella.sonar.analyzer.external.SonarCoverageAnalyzerRestClient;
import it.sella.sonar.analyzer.helper.SonarBaselineReportCreator;
import it.sella.sonar.analyzer.helper.SonarCoverageReportCreator;
import it.sella.sonar.analyzer.model.ReportGeneratorRequest;
import it.sella.sonar.analyzer.model.SonarCoverageMeasures;
import it.sella.sonar.analyzer.model.SonarCoverageMetrics;
import it.sella.sonar.analyzer.model.SonarProjectDetails;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SonarCoverageAnalyzerService {

	@Autowired
	private Environment environment;

	@Autowired
	private SonarCoverageAnalyzerRestClient restClient;

	@Autowired
	private SonarBaselineReportCreator baselineReportCreator;

	@Autowired
	private SonarCoverageReportCreator coverageReportCreator;

	public void generateCoverageReport(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerService >> generateCoverageReport");
		ReportGeneratorRequest validatedRequest = validateRequestParam(request);
		List<SonarProjectDetails> projectDetails = fetchSonarProjectDetails(validatedRequest);
		updateCoverageMetricData(request, projectDetails);
		generateReportFile(projectDetails, validatedRequest);
	}

	private void updateCoverageMetricData(ReportGeneratorRequest request, List<SonarProjectDetails> projectDetails) {
		log.debug("SonarCoverageAnalyzerService >> updateCoverageMetricData");
		log.debug("Found projects :" + projectDetails.size());
		projectDetails.stream().forEach(project -> {
			SonarCoverageMetrics metricData = fetchSonarCoverageMetricData(request, project);
			project.setMetricDataMap(prepareMetricDataMap(metricData));
		});
	}

	private Map<String, String> prepareMetricDataMap(SonarCoverageMetrics metricData) {
		log.debug("SonarCoverageAnalyzerService >> prepareMetricDataMap");
		Map<String, String> dataMap = metricData.getMeasures().stream()
				.collect(Collectors.toMap(SonarCoverageMeasures::getMetric, SonarCoverageMeasures::getValue));
		long linesToCover = Long.parseLong(dataMap.get("lines_to_cover"));
		long uncoveredLines = Long.parseLong(dataMap.get("uncovered_lines"));
		long coveredLines = linesToCover - uncoveredLines;
		dataMap.put("covered_lines", String.valueOf(coveredLines));
		return dataMap;
	}

	private SonarCoverageMetrics fetchSonarCoverageMetricData(ReportGeneratorRequest request,
			SonarProjectDetails project) {
		log.debug("SonarCoverageAnalyzerService >> fetchSonarCoverageMetricData");
		return restClient.fetchSonarCoverageMetricData(request, project.getKey());
	}

	private void generateReportFile(List<SonarProjectDetails> projectDetails, ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerService >> generateReportFile");
		if (request.isBaseLineReport()) {
			baselineReportCreator.updateBaselineValues(projectDetails);
		}
		coverageReportCreator.createBaseLineReport(projectDetails, request.getReportFolderPath());
	}

	private List<SonarProjectDetails> fetchSonarProjectDetails(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerService >> fetchSonarProjectDetails");
		List<SonarProjectDetails> projectList = restClient.fetchSonarProjectDetails(request);
		return projectList == null ? new ArrayList<>() : projectList;
	}

	private ReportGeneratorRequest validateRequestParam(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerService >> validateRequestParam");
		if (isMandatoryParamsMissing(request)) {
			log.debug("mandatory parameters are missing... default values will be set.");
			request.setHost(environment.getProperty("host"));
			request.setPort(environment.getProperty("port"));
			request.setToken(environment.getProperty("userToken"));
		}
		if (StringUtils.isEmpty(request.getReportFolderPath())) {
			log.debug("report path not available, file will be generated at: "+environment.getProperty("reportPath"));
			request.setReportFolderPath(environment.getProperty("reportPath"));
		}
		return request;
	}

	private boolean isMandatoryParamsMissing(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerService >> isMandatoryParamsMissing");
		return StringUtils.isEmpty(request.getHost()) || StringUtils.isEmpty(request.getPort())
				|| StringUtils.isEmpty(request.getToken());
	}

}
