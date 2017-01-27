package org.schmidrules.mojo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.configurator.AbstractComponentConfigurator;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.configurator.ConfigurationListener;
import org.codehaus.plexus.component.configurator.converters.composite.ObjectWithFieldsConverter;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.configuration.PlexusConfiguration;

/**
 * A custom ComponentConfigurator which adds the project's runtime classpath elements to the
 * 
 * @author Brian Jackson
 * @since Aug 1, 2008 3:04:17 PM
 * 
 * plexus.component role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 *                   role-hint="include-project-dependencies"
 * plexus.requirement 
 *                     role="org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 *                     role-hint="default"
 * 
 * OGR: Useful resources: 
 * https://maven.apache.org/plugin-developers/cookbook/plexus-plugin-upgrade.html
 * http://blog.sonatype.com/2009/05/plexus-container-five-minute-tutorial/#.U4L9Zvl_tqV
 *                     
 */
@Component(role = org.codehaus.plexus.component.configurator.ComponentConfigurator.class,
        hint = "include-project-dependencies")
public class IncludeProjectDependenciesComponentConfigurator extends AbstractComponentConfigurator {

    @Override
    public void configureComponent(final Object component, final PlexusConfiguration configuration,
            final ExpressionEvaluator expressionEvaluator, final ClassRealm containerRealm,
            final ConfigurationListener listener) throws ComponentConfigurationException {

        addProjectDependenciesToClassRealm(expressionEvaluator, containerRealm);

        ObjectWithFieldsConverter converter = new ObjectWithFieldsConverter();

        converter.processConfiguration(converterLookup, component, containerRealm.getClassLoader(), configuration,
                expressionEvaluator, listener);

    }

    @SuppressWarnings("unchecked")
    private void addProjectDependenciesToClassRealm(final ExpressionEvaluator expressionEvaluator,
            final ClassRealm containerRealm) throws ComponentConfigurationException {
        List<String> runtimeClasspathElements;
        try {
            // noinspection unchecked
            runtimeClasspathElements =
                    (List<String>) expressionEvaluator.evaluate("${project.runtimeClasspathElements}");
        } catch (ExpressionEvaluationException e) {
            throw new ComponentConfigurationException(
                    "There was a problem evaluating: ${project.runtimeClasspathElements}", e);
        }

        // Add the project dependencies to the ClassRealm
        final URL[] urls = buildURLs(runtimeClasspathElements);
        for (URL url : urls) {
            containerRealm.addConstituent(url);
        }
    }

    private URL[] buildURLs(final List<String> runtimeClasspathElements) throws ComponentConfigurationException {
        // Add the projects classes and dependencies
        List<URL> urls = new ArrayList<>(runtimeClasspathElements.size());
        for (String element : runtimeClasspathElements) {

            try {
                final URL url = new File(element).toURI().toURL();
                urls.add(url);

            } catch (MalformedURLException e) {
                throw new ComponentConfigurationException("Unable to access project dependency: " + element, e);
            }
        }

        // Add the plugin's dependencies (so Trove stuff works if Trove isn't on
        return urls.toArray(new URL[urls.size()]);
    }

}
