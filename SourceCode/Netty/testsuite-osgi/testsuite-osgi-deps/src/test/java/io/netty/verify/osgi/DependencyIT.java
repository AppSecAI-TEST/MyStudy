/*
 * Copyright 2013 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.verify.osgi;

import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Dependency Integration Tests.
 */
public class DependencyIT {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DependencyIT.class);

    /**
     * Default location of generated karaf features file.
     * <p>
     * See <a href=
     * "http://karaf.apache.org/manual/3.0.0-SNAPSHOT/developers-guide/karaf-maven-plugin.html"
     * >karaf-maven-plugin</a>
     */
    public static final String FEATURE = "./target/feature/feature.xml";
    private static final Pattern WRAPPED_MODULE_PATTERN = Pattern.compile("wrap:mvn:io\\.netty/");

    @Test
    public void verifyKarafFeatureHasNoWrapProtocol() throws Exception {
        String text = FileUtils.readFileToString(new File(FEATURE));

        // Ignore wrap:mvn:io.netty - it occurs when Maven didn't give the Netty modules to karaf-maven-plugin
        // as class directories.
        Matcher matcher = WRAPPED_MODULE_PATTERN.matcher(text);
        if (matcher.find()) {
            text = matcher.replaceAll("mvn:io.netty/");
            logger.info("Ignored wrap:mvn:io.netty");
        }

        if (text.contains("wrap:")) {
            fail(
                    "feature.xml generated by karaf-maven-plugin contains 'wrap:' protocol; " +
                    "some transitive dependencies are not OSGi bundles: " + StringUtil.NEWLINE +
                    text
            );
        } else {
            logger.info("All transitive dependencies are OSGi bundles.");
        }
    }
}
