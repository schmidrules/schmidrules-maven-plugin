# schmidrules-maven-plugin
Maven plugin for schmidrules

### Project status ###
This project has been created, because we had an internal need to simply describe our application architecture, enforce it by having a build breaker in our CI pipeline and create a visualization based on the architecture descriptor file. At the time of the creation [ArchUnit](https://www.archunit.org/) was not available or at least not publicly available. 
In the meantime, [ArchUnit](https://www.archunit.org/) has become an actively maintained, sophisticated solution to test the application architecture and we recommend using it for new Java applications instead of schmidrules.

### Usage ###

1. create src/main/config/schmid-rules.xml
2. mvn org.schmidrules:schmidrules-maven-plugin:assert

Or include the plugin in the Maven build via pom.xml. 

```xml
<plugins>
  <plugin>
    <groupId>org.schmidrules</groupId>
    <artifactId>schmidrules‐maven‐plugin</artifactId>
    <version>1.0.0</version>
    <executions>
      <execution>
        <phase>process‐sources</phase>
        <goals>
          <goal>assert</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
</plugins>
```

The assert will produce a build failure if the defined application architecture in the XML file does not match the real architecture of the application.
