package it.sella.sonar.analyzer.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.DefaultPropertiesPersister;

import it.sella.sonar.analyzer.model.SonarProjectDetails;
import it.sella.sonar.analyzer.util.SonarMetricMeasures;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SonarBaselineReportCreator {

	@Autowired
	private Environment environment;

	private DefaultPropertiesPersister propertyPersister = new DefaultPropertiesPersister();

	public void updateBaselineValues(List<SonarProjectDetails> projectDetails) {
		log.debug("SonarBaselineReportCreator >> updateBaselineValues");
		String resourceFile = environment.getProperty("baselineFilePath");
		try {
			Path baselineFile = createBaseLineValuePropertyFileInClassPath(projectDetails);
			log.debug("Copying property file to resource folder...");
			Files.copy(baselineFile, Paths.get(resourceFile), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ex) {
			log.error("IOException occurred in updateBaselineValues: ", ex);
		}
	}

	private Path createBaseLineValuePropertyFileInClassPath(List<SonarProjectDetails> projectDetails)
			throws IOException {
		log.debug("SonarBaselineReportCreator >> createBaseLineValuePropertyFileInClassPath");
		File classPathResource = new ClassPathResource("/baseline-values.properties").getFile();
		log.debug("deleting property file from classpath if already exists...");
		Files.deleteIfExists(Paths.get(classPathResource.getAbsolutePath()));
		log.debug("creating property file in classpath");
		Path baselineFile = Files.createFile(Paths.get(classPathResource.getAbsolutePath()));
		try (OutputStream outputStream = new FileOutputStream(baselineFile.toString());) {
			Properties porperties = getBaseLineValues(projectDetails);
			log.debug("storing property values in file...");
			propertyPersister.store(porperties, outputStream, "baseline values");
		} finally {
			log.debug("Updated baseline values in classpath property file.");
		}
		return baselineFile;
	}

	private Properties getBaseLineValues(List<SonarProjectDetails> projectDetails) {
		log.debug("SonarBaselineReportCreator >> getBaseLineValues");
		Properties properties = new Properties();
		for (SonarProjectDetails project : projectDetails) {
			String linesToCoverKey = project.getName() + '_' + SonarMetricMeasures.LINES_TO_COVER.getValue();
			String linesToCoverValue = project.getMetricDataMap().get(SonarMetricMeasures.LINES_TO_COVER.getValue());
			String coveredLinesKey = project.getName() + '_' + SonarMetricMeasures.COVERED_LINES.getValue();
			String coveredLinesValue = project.getMetricDataMap().get(SonarMetricMeasures.COVERED_LINES.getValue());
			properties.put(linesToCoverKey, linesToCoverValue);
			properties.put(coveredLinesKey, coveredLinesValue);
		}
		return properties;
	}

}
