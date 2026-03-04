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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ListenerDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.ballerina.tools.diagnostics.DiagnosticFactory;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.wso2.strict.library.Constants.INIT;
import static io.wso2.strict.library.Constants.LIBRARY;
import static io.wso2.strict.library.Constants.MAIN;
import static io.wso2.strict.library.Constants.WSO2;
import static io.wso2.strict.library.Constants.PUBLIC_KEYWORD;
import static io.wso2.strict.library.Constants.STRICT;

/**
 * Validates that library projects do not contain entrypoints such as main functions, services, or listeners.
 */
public class StrictLibraryProjectValidator implements AnalysisTask<SyntaxNodeAnalysisContext> {

    private final AtomicBoolean executed = new AtomicBoolean(false);

    @Override
    public void perform(SyntaxNodeAnalysisContext context) {
        if (!executed.compareAndSet(false, true)) {
            return;
        }

        Location importLocation = null;
        for (Module module : context.currentPackage().modules()) {
            importLocation = findStrictLibraryImport(module);
            if (importLocation != null) {
                break;
            }
        }
        if (importLocation == null) {
            return;
        }

        List<String> entrypoints = new ArrayList<>();
        for (Module module : context.currentPackage().modules()) {
            for (DocumentId docId : module.documentIds()) {
                Document doc = module.document(docId);
                ModulePartNode root = doc.syntaxTree().rootNode();
                String fileName = doc.name();
                for (Node member : root.members()) {
                    String entrypoint = getEntrypointDescription(member);
                    if (entrypoint != null) {
                        entrypoints.add(entrypoint + " in " + fileName);
                    }
                }
            }
        }

        if (!entrypoints.isEmpty()) {
            reportDiagnostic(context, importLocation, String.join(", ", entrypoints));
        }
    }

    private Location findStrictLibraryImport(Module module) {
        for (DocumentId docId : module.documentIds()) {
            Document doc = module.document(docId);
            ModulePartNode root = doc.syntaxTree().rootNode();
            for (ImportDeclarationNode importDecl : root.imports()) {
                if (isStrictLibraryImport(importDecl)) {
                    return importDecl.location();
                }
            }
        }
        return null;
    }

    private boolean isStrictLibraryImport(ImportDeclarationNode importDecl) {
        if (importDecl.orgName().isEmpty()) {
            return false;
        }
        String orgName = importDecl.orgName().get().orgName().text().trim();
        if (!WSO2.equals(orgName)) {
            return false;
        }
        var moduleNameNodes = importDecl.moduleName();
        if (moduleNameNodes.size() != 2) {
            return false;
        }
        String firstPart = moduleNameNodes.get(0).toString().trim();
        String secondPart = moduleNameNodes.get(1).toString().trim();
        return STRICT.equals(firstPart) && LIBRARY.equals(secondPart);
    }

    private String getEntrypointDescription(Node member) {
        SyntaxKind kind = member.kind();
        if (kind == SyntaxKind.FUNCTION_DEFINITION) {
            FunctionDefinitionNode funcNode = (FunctionDefinitionNode) member;
            String funcName = funcNode.functionName().text().trim();
            if (MAIN.equals(funcName)
                    && funcNode.qualifierList().stream()
                            .anyMatch(token -> PUBLIC_KEYWORD.equals(token.text().trim()))) {
                return "'public function main'";
            }
            if (INIT.equals(funcName)) {
                return "'function init'";
            }
        } else if (kind == SyntaxKind.SERVICE_DECLARATION) {
            return "service declaration";
        } else if (kind == SyntaxKind.LISTENER_DECLARATION) {
            ListenerDeclarationNode listenerNode = (ListenerDeclarationNode) member;
            String listenerName = listenerNode.variableName().text().trim();
            return "listener declaration '" + listenerName + "'";
        }
        return null;
    }

    private void reportDiagnostic(SyntaxNodeAnalysisContext context, Location location, String entrypoints) {
        StrictLibraryDiagnostic diagnostic = StrictLibraryDiagnostic.STRICT_LIBRARY_101;
        DiagnosticInfo diagnosticInfo = new DiagnosticInfo(
                diagnostic.getCode(),
                String.format(diagnostic.getMessage(), entrypoints),
                diagnostic.getSeverity());
        context.reportDiagnostic(DiagnosticFactory.createDiagnostic(diagnosticInfo, location));
    }
}
