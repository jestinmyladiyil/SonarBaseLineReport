package it.sella.sonar.analyzer.external;

import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.sella.sonar.analyzer.model.ReportGeneratorRequest;
import it.sella.sonar.analyzer.model.SonarCoverageMetrics;
import it.sella.sonar.analyzer.model.SonarProjectDetails;
import it.sella.sonar.analyzer.model.SonarProjectListResponse;
import it.sella.sonar.analyzer.model.SonarProjectMetricResponse;
import it.sella.sonar.analyzer.util.SonarAnalyzerConstants;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SonarCoverageAnalyzerRestClient {

	@Autowired
	private RestTemplate restTemplate;

	public List<SonarProjectDetails> fetchSonarProjectDetails(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerRestClient >> fetchSonarProjectDetails");
		log.debug("preparing url for projectlist...");
		String apiURL = SonarAnalyzerConstants.getURLForProjectList(request);
		SonarProjectListResponse projectListResponse = null;
		HttpHeaders headers = new HttpHeaders();
		if (authenticationRequired(request)) {
			log.debug("adding authentication header...");
			headers.add("Authorization", prepareAuthHeader(request.getToken()));
		}
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		log.debug("invoking project list rest service...");
		ResponseEntity<SonarProjectListResponse> response = restTemplate.exchange(apiURL, HttpMethod.GET, requestEntity,
				SonarProjectListResponse.class);
		if (200 == response.getStatusCode().value()) {
			log.debug("Sonar web api call success.");
			projectListResponse = response.getBody();
		} else {
			log.debug("Failed to fetch project list from the host :" + request.getHost());
			projectListResponse = new SonarProjectListResponse();
		}
		return projectListResponse.getComponents();
	}

	public SonarCoverageMetrics fetchSonarCoverageMetricData(ReportGeneratorRequest request, String projectKey) {
		log.debug("SonarCoverageAnalyzerRestClient >> fetchSonarCoverageMetricData");
		log.debug("preparing url for projectlist...");
		String apiURL = SonarAnalyzerConstants.getURLForMetricData(request, projectKey);
		SonarProjectMetricResponse metricResponse = null;
		HttpHeaders headers = new HttpHeaders();
		if (authenticationRequired(request)) {
			log.debug("adding authentication header...");
			headers.add("Authorization", prepareAuthHeader(request.getToken()));
		}
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
		log.debug("invoking metric data rest service...");
		ResponseEntity<SonarProjectMetricResponse> response = restTemplate.exchange(apiURL, HttpMethod.GET,
				requestEntity, SonarProjectMetricResponse.class);
		if (200 == response.getStatusCode().value()) {
			log.debug("Sonar web api call success.");
			metricResponse = response.getBody();
		} else {
			log.debug("Failed to fetch metric data for the project " + projectKey + " from host :" + request.getHost());
			metricResponse = new SonarProjectMetricResponse();
		}
		return metricResponse.getComponent();
	}

	private String prepareAuthHeader(String token) {
		log.debug("SonarCoverageAnalyzerRestClient >> prepareAuthHeader");
		String base64Creds = Base64.getEncoder().encodeToString((token + ":").getBytes());
		return "Basic " + base64Creds;
	}

	private boolean authenticationRequired(ReportGeneratorRequest request) {
		log.debug("SonarCoverageAnalyzerRestClient >> authenticationRequired");
		return !"localhost".equals(request.getHost());
	}

}
