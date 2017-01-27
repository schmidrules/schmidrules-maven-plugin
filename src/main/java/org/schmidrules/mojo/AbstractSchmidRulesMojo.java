package org.schmidrules.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.schmidrules.configuration.ConfigurationLoader;

public abstract class AbstractSchmidRulesMojo extends AbstractMojo {

    /**
     * Name of the configuration file used by Schmid Rules.
     * 
     * --parameter alias="config" default-value="schmid-rules.xml"
     */
    @Parameter(property = "config", defaultValue = "schmid-rules.xml", alias="config")
    private String configurationFileName = ConfigurationLoader.DEFAULT_CONFIGURATION_FILE_NAME;
    
    /**
     * --parameter default-value="${project}" --required --readonly
     */
    @Parameter(property = "project", readonly = true)
    MavenProject project;

    /**
     * Find the file with given {@link #configurationFileName} in the testResources.
     * 
     * @return File which may exist
     * @throws MojoExecutionException if file has not been found
     */
    public File findConfigurationFile() throws MojoExecutionException {
        File config = new File(configurationFileName);
        if (config.isFile()) {
            return config;
        }

        // TODO make configurable like
        // http://svn.apache.org/viewvc/maven/plugins/tags/maven-pmd-plugin-3.4/src/main/java/org/apache/maven/plugin/pmd/
        List<Resource> resources = new ArrayList<>();

        Resource srcMainConfig = new Resource();
        srcMainConfig.setDirectory("src/main/config");
        resources.add(srcMainConfig);
        resources.addAll(project.getTestResources());

        try {
            return findConfigurationFile(resources);
        } catch (FileNotFoundException e1) {
            throw new MojoExecutionException("Architecture Rules config file not found", e1);
        }
    }

    /**
     * Find the file with given {@link #configurationFileName} in the testResources.
     * 
     * @param testResources List<Resource>
     * @return File which may exist
     * @throws FileNotFoundException
     */
    private File findConfigurationFile(final List<Resource> testResources) throws FileNotFoundException {
        Log log = getLog();

        for (final Resource resource : testResources) {
            final String directory = resource.getDirectory();

            if (log.isDebugEnabled()) {
                log.debug("try to find configuration in " + directory);
            }

            final String fileName = directory + File.separator + configurationFileName;

            final File configFile = new File(fileName);

            final StringBuffer message = new StringBuffer();
            message.append(configurationFileName).append(" ");

            if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
                if (log.isDebugEnabled()) {
                    message.append("found in the directory ");
                    message.append(configFile.getParent());

                    log.debug(message.toString());
                }

                return configFile;
            } else if (log.isDebugEnabled()) {
                message.append("not found");
                log.debug(message.toString());
            }
        }

        throw new FileNotFoundException(configurationFileName + " not found in " + testResources);
    }

    String getConfigurationFileName() {
        return configurationFileName;
    }

}
