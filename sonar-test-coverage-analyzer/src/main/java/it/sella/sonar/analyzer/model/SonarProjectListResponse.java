package it.sella.sonar.analyzer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class SonarProjectListResponse {

	private List<SonarProjectDetails> components;

}
