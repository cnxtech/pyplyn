/*
 *  Copyright (c) 2016-2017, Salesforce.com, Inc.
 *  All rights reserved.
 *  Licensed under the BSD 3-Clause license.
 *  For full license text, see the LICENSE.txt file in repo root
 *    or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.pyplyn.duct.etl.configuration;

import com.google.inject.Inject;
import com.salesforce.pyplyn.configuration.Configuration;
import com.salesforce.pyplyn.duct.app.BootstrapException;
import com.salesforce.pyplyn.util.SerializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;

/**
 * Parses configuration files
 *
 * @author Mihai Bojin &lt;mbojin@salesforce.com&gt;
 * @since 3.0
 */
public class ConfigurationIntake {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationIntake.class);
    private final List<Throwable> errors = new ArrayList<>();
    private final SerializationHelper serializer;


    @Inject
    public ConfigurationIntake(SerializationHelper serializer) {
        this.serializer = serializer;
    }

    /**
     * Reads the specified configuration directory
     *
     * @param dir Path to config dir
     * @return List of all configuration files
     * @throws IOException on any errors when deserializing {@link Configuration}s
     */
    List<String> listOfConfigurations(String dir) throws IOException {
        // nothing to do if configuration not passed
        if (isNull(dir)) {
            logger.warn("Null configuration dir passed, returning empty configuration list");
            return Collections.emptyList();
        }

        return StreamSupport.stream(Files.newDirectoryStream(Paths.get(dir)).spliterator(), true)
                .filter(ConfigurationIntake::isJsonFile)
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the path component is a file that ends in json
     * <p/>
     * <p/>Does not ensure the file's contents are a valid json string.
     *
     * @return true if json file
     */
    private static boolean isJsonFile(Path path) {
        return Files.isRegularFile(path) && path.getFileName().toString().endsWith(".json");
    }

    /**
     * Parses all configuration files and deserializes the {@link Configuration} objects
     *
     * @param configurations List of configuration files
     * @return
     */
    Set<Configuration> parseAll(List<String> configurations) {
        return configurations.stream().map(this::parseConfigurationFile).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Parses a single configuration file and returns the list of defined Configuration objects
     *
     * @param file Configuration file
     * @return A collection containing the required objects, or empty if none found
     */
    private Set<Configuration> parseConfigurationFile(String file) {
        try {
            return new HashSet<>(Arrays.asList(serializer.deserializeJsonFile(file, Configuration[].class)));

        } catch (IOException e) {
            // store error and return empty list
            addError(e);

            return Collections.emptySet();
        }
    }

    /**
     * Make note of any errors, as we require them later for debugging purposes
     */
    void addError(Throwable cause) {
        errors.add(cause);
    }

    /**
     * Create a new {@link BootstrapException} object, referencing any exceptions
     * @throws BootstrapException if any errors were logged during deserialization
     */
    void throwRuntimeExceptionOnErrors() {
        // if we have errors
        if (!errors.isEmpty()) {
            // get first cause
            Throwable cause = null;
            Optional<Throwable> first = errors.stream().findFirst();
            if (first.isPresent()) {
                cause = first.get();
            }

            // create new exception and add first error as cause
            final BootstrapException ex =
                    new BootstrapException("Errors encountered reading configurations: " + errors.size(), cause);

            // add remaining errors as suppressed exceptions
            errors.stream().skip(1).forEach(ex::addSuppressed);

            throw ex;
        }
    }
}
