package org.schmidrules.mojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.schmidrules.SchmidRulesXmi;

/**
 * Transforms the schmid rules configuration file to XMI so that it can be imported to standard UML
 * tools (especially Enterprise Architect)
 */
@Mojo(name = "createXmi", defaultPhase = LifecyclePhase.TEST, configurator = "include-project-dependencies",
        requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = false)
public class CreateXmiMojo extends AbstractSchmidRulesMojo {

    @Parameter(property = "output", defaultValue = "${project.artifactId}.xml", alias = "output")
    private String outputFileName;

    @Parameter(property = "outputDir", alias = "outputDir")
    private String outputDir;

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (!project.isExecutionRoot()) {
            log.info("Project is not execution root. Skipping.");
            return;
        }

        File configFile = findConfigurationFile();
        log.info(configFile.getAbsolutePath());

        SchmidRulesXmi schmidRulesXmi = new SchmidRulesXmi(configFile, project.getArtifactId());

        File file = new File(obtainOutputDirectory(log) + File.separator + outputFileName);

        try (FileOutputStream out = new FileOutputStream(file)) {

            schmidRulesXmi.createXmi(out);
            log.info("Created XMI file " + file.getAbsolutePath());

        } catch (IOException e) {
            throw new MojoExecutionException("Could not create XMI file", e);
        }
    }

    private String obtainOutputDirectory(Log log) {

        String projectDir = null;
        if (project.getBasedir() != null) {
            projectDir = project.getBuild().getDirectory();
        }

        Optional<String> directory =
                Stream.of(outputDir, projectDir, System.getProperty("user.dir")) //
                        .filter(Objects::nonNull) //
                        .findFirst();

        directory.ifPresent(d -> createDir(d, log));

        return directory.get();
    }

    private void createDir(String directory, Log log) {
        File dir = new File(directory);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("Directory " + dir.getAbsolutePath() + " created.");
        }
    }

}
