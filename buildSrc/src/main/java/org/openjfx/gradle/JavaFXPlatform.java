/*
 * Copyright (c) 2018, 2023, Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjfx.gradle;

import com.google.gradle.osdetector.OsDetector;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaFXPlatform {

    public static enum Predefined {
        // see https://stackoverflow.com/questions/75006480/javafx-maven-platform-specific-build-mac-aarm64-qualifier
        LINUX("linux", "linux-x86_64"),
        LINUX_AARCH64("linux-aarch64", "linux-aarch_64"),
        LINUX_ARM32("linux-arm32-monocle", "linux-arm32"),
        WINDOWS("win", "windows-x86_64"),
        WIN_x86_32("win-x86", "windows-x86_32"),
        OSX("mac", "osx-x86_64"),
        OSX_AARCH64("mac-aarch64", "osx-aarch_64");
        
        public final JavaFXPlatform platform;
        
        Predefined(String classifier, String osDetectorClassifier) {
            platform = predefine(classifier, osDetectorClassifier);
        }
    }

    private static final Set<JavaFXPlatform> predefined;
    
    static {
        predefined = new HashSet<>();
        Predefined.values(); // force initialization
    }
    

    private final String classifier;
    private final String osDetectorClassifier;

    JavaFXPlatform(String classifier, String osDetectorClassifier) {
        this.classifier = classifier;
        this.osDetectorClassifier = osDetectorClassifier;
    }

    private static JavaFXPlatform predefine(String classifier, String osDetectorClassifier) {
        JavaFXPlatform result = new JavaFXPlatform(classifier, osDetectorClassifier);
        predefined.add(result);
        return result;
    }

    public String getClassifier() {
        return classifier;
    }

    public static JavaFXPlatform withClassifier(String classifier) {
        return new JavaFXPlatform(classifier, "<custom>");
    }

    public static JavaFXPlatform detect(Project project) {
        final String osClassifier = project.getExtensions().getByType(OsDetector.class).getClassifier();

        for (JavaFXPlatform platform : predefined) {
            if (platform.osDetectorClassifier.equals(osClassifier)) {
                return platform;
            }
        }

        String supportedPlatforms = predefined.stream()
                .map(p -> p.osDetectorClassifier)
                .collect(Collectors.joining("', '", "'", "'"));

        throw new GradleException(
                String.format(
                        "Unsupported JavaFX platform found: '%s'! "
                        + "This plugin is designed to work on supported platforms only."
                        + "Current supported platforms are %s.", osClassifier, supportedPlatforms)
        );
    }

    public static JavaFXPlatform fromString(String platformId) {
        switch (platformId) {
            case "linux":
                return Predefined.LINUX.platform;
            case "linux-aarch64":
                return Predefined.LINUX_AARCH64.platform;
            case "win":
            case "windows":
                return Predefined.WINDOWS.platform;
            case "win-x86":
            case "win-x86_32":
                return Predefined.WIN_x86_32.platform;
            case "osx":
            case "mac":
            case "macos":
                return Predefined.OSX.platform;
            case "osx-aarch64":
            case "mac-aarch64":
            case "macos-aarch64":
                return Predefined.OSX_AARCH64.platform;
        }
        return Predefined.valueOf(platformId).platform;
    }
}
