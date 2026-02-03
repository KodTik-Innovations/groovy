/*
 * Copyright 2026 KodTik-Innovations
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.lang;

import groovyjarjarasm.asm.ClassWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

public class CustomGroovyClassLoader extends GroovyClassLoader {

  public CustomGroovyClassLoader(ClassLoader loader, CompilerConfiguration config) {
    super(loader, config);
  }

  public CustomGroovyClassLoader(
      ClassLoader parent, CompilerConfiguration configuration, boolean b) {
    super(parent, configuration, b);
  }

  @Override
  protected ClassCollector createCollector(CompilationUnit unit, SourceUnit su) {
    InnerLoader loader =
        AccessController.doPrivileged(
            (PrivilegedAction<InnerLoader>) () -> new InnerLoader(CustomGroovyClassLoader.this));

    return new ClassCollector(loader, unit, su) {

      @Override
      protected Class createClass(byte[] code, ClassNode classNode) {
        return super.createClass(code, classNode);
      }

      @Override
      protected Class onClassNode(ClassWriter classWriter, ClassNode classNode) {
        try {
          return super.onClassNode(classWriter, classNode);
        } catch (Exception e) {
          return null;
        }
      }
    };
  }
}
