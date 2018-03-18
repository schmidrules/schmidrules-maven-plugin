# schmidrules-maven-plugin
Maven plugin for schmidrules

Usage:

1. create src/main/config/schmid-rules.xml
2. mvn org.schmidrules:schmidrules-maven-plugin:assert

Or include the plugin in the Maven build via pom.xml. 

```xml
<plugins>
  <plugin>
    <groupId>org.schmidrules</groupId>
    <artifactId>schmidrules‐maven‐plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
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
