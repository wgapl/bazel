// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.rules.java;

import static com.google.devtools.build.lib.packages.Attribute.ConfigurationTransition.HOST;
import static com.google.devtools.build.lib.packages.Attribute.attr;
import static com.google.devtools.build.lib.packages.BuildType.LABEL;
import static com.google.devtools.build.lib.packages.BuildType.LABEL_LIST;
import static com.google.devtools.build.lib.syntax.Type.BOOLEAN;
import static com.google.devtools.build.lib.syntax.Type.STRING_LIST;

import com.google.devtools.build.lib.analysis.BaseRuleClasses;
import com.google.devtools.build.lib.analysis.RuleDefinition;
import com.google.devtools.build.lib.analysis.RuleDefinitionEnvironment;
import com.google.devtools.build.lib.packages.RuleClass;
import com.google.devtools.build.lib.packages.RuleClass.Builder;
import com.google.devtools.build.lib.packages.RuleClass.Builder.RuleClassType;
import com.google.devtools.build.lib.rules.cpp.CppConfiguration;

/**
 * A base rule for building the java_import rule.
 */
public class JavaImportBaseRule implements RuleDefinition {

  @Override
  public RuleClass build(Builder builder, RuleDefinitionEnvironment environment) {
    return builder
        .requiresConfigurationFragments(JavaConfiguration.class, CppConfiguration.class)
        .add(attr(":host_jdk", LABEL)
            .cfg(HOST)
            .value(JavaSemantics.HOST_JDK))
        /* <!-- #BLAZE_RULE(java_import).ATTRIBUTE(jars) -->
        The list of JAR files provided to Java targets that depend on this target.
        <!-- #END_BLAZE_RULE.ATTRIBUTE --> */
        .add(attr("jars", LABEL_LIST)
            .mandatory()
            .nonEmpty()
            .allowedFileTypes(JavaSemantics.JAR))
        /* <!-- #BLAZE_RULE(java_import).ATTRIBUTE(srcjar) -->
        A JAR file that contains source code for the compiled JAR files.
        <!-- #END_BLAZE_RULE.ATTRIBUTE --> */
        .add(attr("srcjar", LABEL)
            .allowedFileTypes(JavaSemantics.SOURCE_JAR, JavaSemantics.JAR)
            .direct_compile_time_input())
        .removeAttribute("deps")  // only exports are allowed; nothing is compiled
        /* <!-- #BLAZE_RULE(java_import).ATTRIBUTE(neverlink) -->
        Only use this library for compilation and not at runtime.
        Useful if the library will be provided by the runtime environment
        during execution. Examples of libraries like this are IDE APIs
        for IDE plug-ins or <code>tools.jar</code> for anything running on
        a standard JDK.
        <!-- #END_BLAZE_RULE.ATTRIBUTE --> */
        .add(attr("neverlink", BOOLEAN).value(false))
        /* <!-- #BLAZE_RULE(java_import).ATTRIBUTE(constraints) -->
        Extra constraints imposed on this rule as a Java library.
        <!-- #END_BLAZE_RULE.ATTRIBUTE --> */
        .add(attr("constraints", STRING_LIST)
            .orderIndependent()
            .nonconfigurable("used in Attribute.validityPredicate implementations (loading time)"))
        .advertiseProvider(JavaSourceInfoProvider.class)
        .advertiseProvider(JavaCompilationArgsProvider.class)
        .build();
  }

  @Override
  public Metadata getMetadata() {
    return RuleDefinition.Metadata.builder()
        .name("$java_import_base")
        .type(RuleClassType.ABSTRACT)
        .ancestors(BaseRuleClasses.RuleBase.class, ProguardLibraryRule.class)
        .build();
  }
}
/*<!-- #BLAZE_RULE (NAME = java_import, TYPE = LIBRARY, FAMILY = Java) -->

  <p>This rule allows the use of precompiled JAR files as libraries for
  <code><a href="#java_library">java_library</a></code> rules.</p>

<!-- #END_BLAZE_RULE -->*/
