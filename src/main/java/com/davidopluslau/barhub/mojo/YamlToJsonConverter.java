package com.davidopluslau.barhub.mojo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Add the following to the maven plugins section
// <plugin>
//     <groupId>org.codehaus.mojo</groupId>
//     <artifactId>exec-maven-plugin</artifactId>
//     <version>1.4.0</version>
//     <executions>
//         <execution>
//             <id>generate-openapi-json</id>
//             <phase>compile</phase>
//             <goals>
//                 <goal>java</goal>
//             </goals>
//             <configuration>
//                 <mainClass>YamlToJsonConverter</mainClass>
//             </configuration>
//         </execution>
//     </executions>
// </plugin>

/**
 * Converts openapi.yaml to openapi.json during a maven build.
 */
public class YamlToJsonConverter {
  private static final Logger LOG = LoggerFactory.getLogger(YamlToJsonConverter.class);

  /**
   * Program entry point.
   *
   * @param args commandline arguments
   */
  public static void main(String... args) {
    LOG.debug("Converting openapi.yaml to openapi.json");

    final String yamlString = readYamlFile();
    final String jsonString = convertYamlToJson(yamlString);
    writeJsonFile(jsonString);
  }

  private static String readYamlFile() {
    String cwd = System.getProperty("user.dir");
    Path path = Paths.get(String.join("/", cwd, "target/classes/config/openapi.yaml"));

    StringBuilder data = new StringBuilder();
    try (Stream<String> lines = Files.lines(path)) {
      lines.forEach(line -> data.append(line).append("\n"));
      return data.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to read yaml file", e);
    }
  }

  private static void writeJsonFile(final String output) {
    String cwd = System.getProperty("user.dir");
    String path = String.join("/", cwd, "target/classes/config/openapi.json");

    try (BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
      writer.write(output);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write to json file", e);
    }
  }

  private static String convertYamlToJson(String yaml) {
    try {
      ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
      Object obj = yamlReader.readValue(yaml, Object.class);

      ObjectMapper jsonWriter = new ObjectMapper();
      return jsonWriter.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    } catch (IOException e) {
      throw new RuntimeException("Failed to convert yaml to json", e);
    }
  }
}
