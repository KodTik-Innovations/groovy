/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.vmplugin;

import static org.codehaus.groovy.reflection.android.AndroidSupport.isDalvik;
import static org.codehaus.groovy.reflection.android.AndroidSupport.isRunningAndroid;

import groovy.lang.MetaClass;
import groovy.lang.MetaMethod;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;

/** Interface to access VM version based actions. This interface is for internal use only! */
public interface VMPlugin {
  void setAdditionalClassInformation(ClassNode c);

  Class[] getPluginDefaultGroovyMethods();

  Class[] getPluginStaticGroovyMethods();

  void configureAnnotationNodeFromDefinition(AnnotationNode definition, AnnotationNode root);

  void configureAnnotation(AnnotationNode an);

  void configureClassNode(CompileUnit compileUnit, ClassNode classNode);

  void invalidateCallSites();

  /**
   * Returns a handle with bound receiver to invokeSpecial the given method. This method will
   * require at least Java 7, but since the source has to compile on older Java versions as well it
   * is not marked to return a MethodHandle and uses Object instead
   *
   * @return null in case of jdk&lt;7, otherwise a handle that takes the method call arguments for
   *     the invokespecial call
   */
  Object getInvokeSpecialHandle(Method m, Object receiver);

  /**
   * Invokes a handle produced by #getInvokeSpecialdHandle
   *
   * @param handle the handle
   * @param args arguments for the method call, can be empty but not null
   * @return the result of the method call
   */
  Object invokeHandle(Object handle, Object[] args) throws Throwable;

  /**
   * Gives the version the plugin is made for
   *
   * @return 7 for jdk7, 8 for jdk8, 9 for jdk9 or higher
   */
  int getVersion();

  /**
   * Returns java version, e.g. 1.8, 9, 11, 17
   *
   * @return java version
   * @since 4.0.0
   * @deprecated
   */
  @Deprecated
  static String getJavaVersion() {
    try {
      // deenu modify: return java version 1.8
      if (isRunningAndroid() || isDalvik()) return "1.8";
      return System.getProperty("java.specification.version");
    } catch (SecurityException se) {
      Class<?> versionClass;
      try {
        versionClass =
            Class.forName("java.lang.Runtime$Version"); // `Version` has been added since Java 9
      } catch (ClassNotFoundException e) {
        return "1.8";
      }

      try {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle versionMethodHandle = lookup.unreflect(Runtime.class.getMethod("version"));
        Object version = versionMethodHandle.invoke();
        MethodHandle majorMethodHandle = lookup.unreflect(versionClass.getMethod("major"));
        return String.valueOf(majorMethodHandle.invoke(version));
      } catch (Throwable t) {
        throw new GroovyBugError(t.getMessage()); // should never happen
      }
    }
  }

  /**
   * Check whether invoking {@link AccessibleObject#setAccessible(boolean)} on the accessible object
   * will be completed successfully
   *
   * @param accessibleObject the accessible object to check
   * @param callerClass the callerClass to invoke {@code setAccessible}
   * @return the check result
   */
  boolean checkCanSetAccessible(AccessibleObject accessibleObject, Class<?> callerClass);

  /**
   * check whether the member can be accessed or not
   *
   * @param callerClass callerClass the callerClass to invoke {@code setAccessible}
   * @param declaringClass the type of member owner
   * @param memberModifiers modifiers of member
   * @param allowIllegalAccess whether to allow illegal access
   * @return the result of checking
   */
  boolean checkAccessible(
      Class<?> callerClass,
      Class<?> declaringClass,
      int memberModifiers,
      boolean allowIllegalAccess);

  /**
   * Set the {@code accessible} flag for this reflected object to {@code true} if possible.
   *
   * @param ao the accessible object
   * @return {@code true} if the {@code accessible} flag is set to {@code true}; {@code false} if
   *     access cannot be enabled.
   * @throws SecurityException if the request is denied by the security manager
   */
  boolean trySetAccessible(AccessibleObject ao);

  /**
   * transform meta method
   *
   * @param metaClass metaclass
   * @param metaMethod the original meta method
   * @param caller caller class, whose method sets accessible for methods
   * @return the transformed meta method
   */
  MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod, Class<?> caller);

  /**
   * Performs the specified PrivilegedAction with privileges enabled on platforms which support that
   * capability, otherwise the action is performed ignoring privileges.
   *
   * @param action the action to be performed
   * @param <T> the type of the value returned by the PrivilegedAction's run method
   * @return the value returned by the action's run method
   */
  @Deprecated
  <T> T doPrivileged(java.security.PrivilegedAction<T> action);

  /**
   * Performs the specified PrivilegedExceptionAction with privileges enabled on platforms which
   * support that capability, otherwise the action is performed ignoring privileges.
   *
   * @param action the action to be performed
   * @param <T> the type of the value returned by the PrivilegedAction's run method
   * @return the value returned by the action's run method
   */
  @Deprecated
  <T> T doPrivileged(java.security.PrivilegedExceptionAction<T> action)
      throws java.security.PrivilegedActionException;

  /**
   * transform meta method.
   *
   * @param metaClass metaclass
   * @param metaMethod the original meta method
   * @return the transformed meta method
   */
  MetaMethod transformMetaMethod(MetaClass metaClass, MetaMethod metaMethod);

  /**
   * Returns the default import classes: class name -&gt; the relevant package names
   *
   * @param packageNames the default import package names, e.g. java.lang.
   * @return the default import classes
   * @since 3.0.2
   */
  default Map<String, Set<String>> getDefaultImportClasses(String[] packageNames) {
    return Collections.emptyMap();
  }

  /**
   * Returns the list of record component names or the empty list if the class is not a record or
   * running on a pre16 JDK.
   *
   * @param maybeRecord the class in question
   * @return the default list of names
   * @since 4.0.15
   */
  @Incubating
  default List<String> getRecordComponentNames(Class<?> maybeRecord) {
    return Collections.emptyList();
  }
}
