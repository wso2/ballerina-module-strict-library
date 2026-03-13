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

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

/**
 * The {@code CodeAnalyzer} for Ballerina library projects.
 */
public class StrictLibraryProjectAnalyzer extends CodeAnalyzer {

    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
        StrictLibraryProjectValidator validator = new StrictLibraryProjectValidator();
        codeAnalysisContext.addSyntaxNodeAnalysisTask(validator, SyntaxKind.FUNCTION_DEFINITION);
        codeAnalysisContext.addSyntaxNodeAnalysisTask(validator, SyntaxKind.SERVICE_DECLARATION);
        codeAnalysisContext.addSyntaxNodeAnalysisTask(validator, SyntaxKind.LISTENER_DECLARATION);
    }
}
