/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.wso2.strict.library;

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for Ballerina strict library compiler plugin.
 */
public class CompilerPluginTest {

    private static final Path RESOURCE_DIRECTORY = Paths.get("src", "test", "resources", "ballerina_sources")
            .toAbsolutePath();
    private static final Path DISTRIBUTION_PATH = Paths.get("build", "target", "extracted-distributions",
            "jballerina-tools-zip", "jballerina-tools-2201.13.0",
            "jballerina-tools-2201.13.0").toAbsolutePath();

    private static final String STRICT_LIBRARY_101 = "STRICT_LIBRARY_101";

    private Package loadPackage(String path) {
        Path projectDirPath = RESOURCE_DIRECTORY.resolve(path);
        BuildProject project = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(DISTRIBUTION_PATH).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    @Test
    public void testValidLibraryProject() {
        Package currentPackage = loadPackage("valid_library_project");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        long errorCount = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> d.diagnosticInfo().code() != null
                        && d.diagnosticInfo().code().startsWith("STRICT_LIBRARY_"))
                .count();
        Assert.assertEquals(errorCount, 0);
    }

    @Test
    public void testProjectWithMain() {
        Package currentPackage = loadPackage("project_with_main");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("public function main"));
    }

    @Test
    public void testProjectWithInit() {
        Package currentPackage = loadPackage("project_with_init");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("function init"));
    }

    @Test
    public void testProjectWithService() {
        Package currentPackage = loadPackage("project_with_service");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("service declaration"));
    }

    @Test
    public void testProjectWithListener() {
        Package currentPackage = loadPackage("project_with_listener");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("listener declaration"));
    }

    @Test
    public void testProjectWithMultipleEntrypoints() {
        Package currentPackage = loadPackage("project_with_multiple");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        String message = libraryErrors[0].message();
        Assert.assertTrue(message.contains("public function main"));
        Assert.assertTrue(message.contains("service declaration"));
    }

    @Test
    public void testProjectWithoutLibBal() {
        Package currentPackage = loadPackage("project_without_lib_bal");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("public function main"));
    }

    @Test
    public void testProjectImportNotInLib() {
        Package currentPackage = loadPackage("project_import_not_in_lib");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("public function main"));
    }

    @Test
    public void testProjectImportInSubmodule() {
        Package currentPackage = loadPackage("project_import_in_submodule");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("function init"));
    }

    @Test
    public void testProjectServiceInSubmodule() {
        Package currentPackage = loadPackage("project_service_in_submodule");
        PackageCompilation compilation = currentPackage.getCompilation();
        DiagnosticResult diagnosticResult = compilation.diagnosticResult();
        Diagnostic[] libraryErrors = diagnosticResult.diagnostics().stream()
                .filter(d -> d.diagnosticInfo().severity().equals(DiagnosticSeverity.ERROR))
                .filter(d -> STRICT_LIBRARY_101.equals(d.diagnosticInfo().code()))
                .toArray(Diagnostic[]::new);
        Assert.assertEquals(libraryErrors.length, 1);
        Assert.assertTrue(libraryErrors[0].message().contains("service declaration"));
    }
}
