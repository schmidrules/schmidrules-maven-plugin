package org.schmidrules.mojo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.schmidrules.SchmidRules;
import org.schmidrules.check.violation.Violation;
import org.schmidrules.configuration.dto.ConfigurationException;

/**
 * Asserts your architecture
 */
@Mojo(name = "assert", defaultPhase = LifecyclePhase.TEST, configurator = "include-project-dependencies",
        requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = false)
public class AssertMojo extends AbstractSchmidRulesMojo {

    /**
     * For <a href="http://maven.apache.org/pom.html#Aggregation">Aggregation (or Multi-Module)
     * project</a> we don't run Architecture Rules assertions by default. To change this behavior
     * use <code>-Darchitecture-rules.skipRoot=false</code>
     * 
     * --parameter default-value="true" expression="${architecture-rules.skipRoot}"
     */
    @Parameter(defaultValue = "true")
    private boolean skipRoot;

    /**
     * If your failed rules shouldn't break a build (you *had* to introduce a dependency a day
     * before a release, which of course isn't nice, but you can easily correct the problem a few
     * days after a release) set this parameter to <code>false</code>.
     * 
     * --parameter default-value="true" expression="${architecture-rules.failOnError}"
     */
    @Parameter(defaultValue = "true")
    private boolean failOnError;

    /**
     * Skip current project (i.e. in
     * <a href="http://maven.apache.org/pom.html#Aggregation">Aggregation (or Multi-Module)
     * project</a>) and don't run Architecture Rules assertions against it.
     * 
     * --parameter default-value="false" expression="${architecture-rules.skip}"
     */
    @Parameter(defaultValue = "false")
    private boolean skip;

    /**
     * Reference to the Maven's reactor that is being tested.
     * 
     * --parameter expression="${reactorProjects}" --readonly
     */
    @Parameter(property = "reactorProjects", readonly = true)
    private Collection<MavenProject> reactorProjects;

    @Override
    public void execute() throws MojoExecutionException {
        Log log = getLog();
        if (!project.isExecutionRoot()) {
            log.info("Project is not execution root. Skipping.");
            return;
        }

        File configFile = findConfigurationFile();
        log.info(configFile.getAbsolutePath());

        List<Violation> violations = new ArrayList<>();
        List<File> baseDirs = new ArrayList<>();
        for (MavenProject reactorProject : reactorProjects) {
            baseDirs.add(reactorProject.getBasedir());
        }
        SchmidRules rules = new SchmidRules(configFile, baseDirs);
        
        try {
        	violations.addAll(rules.check());
        } catch (ConfigurationException coex) {
        	throw new MojoExecutionException("Configuration Exception", coex);
        }

        evaluateViolations(violations);
    }

    private void evaluateViolations(final Collection<Violation> violations) throws MojoExecutionException {
        if (violations.isEmpty()) {
            return;
        }

        for (Violation violation : violations) {
            switch (violation.getSeverity()) {
                case ERROR:
                    getLog().error(violation.getDescription());
                    break;
                case WARN:
                    getLog().warn(violation.getDescription());
                    break;
                default:
                    throw new RuntimeException("Severity not implemented: " + violation.getSeverity());
            }
        }

        if (isFailOnError()) {
            throw new MojoExecutionException(violations, "SchmidRules violated - see warnings above for details", "");
        }
    }

    private boolean isFailOnError() {
        return failOnError;
    }
}
