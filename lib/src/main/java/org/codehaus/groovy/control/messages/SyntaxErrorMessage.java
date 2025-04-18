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
package org.codehaus.groovy.control.messages;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.groovy.control.Janitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.SyntaxException;

/** A class for error messages produced by the parser system. */
public class SyntaxErrorMessage extends Message {
  protected SyntaxException cause;
  protected SourceUnit source;

  public SyntaxErrorMessage(SyntaxException cause, SourceUnit source) {
    this.source = source;
    
    // deenu modify: fix error line
    /** Returns the valid error line for invalid SyntaxException. */
    String message = cause.getMessage();
    boolean isEOF = message != null && message.contains("EOF");

    String errorLineString =
        this.source != null && this.source.getSource() != null
            ? this.source.getSource().getLine(cause.getLine(), new Janitor())
            : null;
    String errorName = null;

    if (isEOF) {
      if (errorLineString != null && errorLineString.trim().isEmpty()) {
        int line = getValidErrorLine(cause.getLine());
        errorLineString = this.source.getSource().getLine(line, new Janitor());
        if (errorLineString != null) {
          errorName = errorLineString.trim();
          int start = errorLineString.indexOf(errorName) + 1;
          int end = start + errorName.length();
          cause.setLine(line);
          cause.setStartColumn(start);
          cause.setEndColumn(end);
        }
      } else if (errorLineString != null) {
        errorName = errorLineString.trim();
        int start = errorLineString.indexOf(errorName) + 1;
        int end = start + errorName.length();
        cause.setStartColumn(start);
        cause.setEndColumn(end);
      }
    } else if (message != null) {
      Matcher matcher = Pattern.compile("'(.*?)'|\\[(.*?)\\]").matcher(message);
      if (matcher.find()) {
        errorName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
      }
      if (errorName != null && !errorName.isEmpty()) {
        errorName = errorName.trim();
        if (errorLineString != null && !errorLineString.trim().isEmpty()) {
          if (errorName.equals(errorLineString.trim())) {
            int start = errorLineString.indexOf(errorName) + 1;
            int end = start + errorName.length();
            if (cause.getStartColumn() != start || cause.getEndColumn() != end) {
              cause.setStartColumn(start);
              cause.setEndColumn(end);
              cause.setEndLine(end);
            }
          } else if (errorName.equals("\\n")) {
            errorLineString = this.source.getSource().getLine(cause.getLine(), new Janitor());
            if (errorLineString != null && errorLineString.trim().startsWith("import")) {
              errorName = errorLineString.replace("import", "").trim();
              int start = errorLineString.indexOf(errorName) + 1;
              int end = start + errorName.length();
              cause.setStartColumn(start);
              cause.setEndColumn(end);
              cause.setEndLine(end);
            }
          } else if (errorName.contains("}")) {
            cause.setLine(cause.getLine() - 1);
            errorLineString = this.source.getSource().getLine(cause.getLine(), new Janitor());
            if (errorLineString != null && !errorLineString.trim().isEmpty()) {
              errorName = errorLineString.trim();
              int start = errorLineString.indexOf(errorName) + 1;
              int end = start + errorName.length();
              cause.setStartColumn(start);
              cause.setEndColumn(end);
              cause.setEndLine(end);
            } else {
              errorName = errorName.replace("\\n", "").replace("}", "").trim();
              int line = getValidErrorLine(cause.getLine(), errorName);
              errorLineString = this.source.getSource().getLine(line, new Janitor());
              if (errorLineString != null) {
                int start = errorLineString.indexOf(errorName) + 1;
                int end = start + errorName.length();
                cause.setLine(line);
                cause.setStartColumn(start);
                cause.setEndColumn(end);
                cause.setEndLine(end);
              }
            }
          } else {
            errorLineString = this.source.getSource().getLine(cause.getLine(), new Janitor());
            if (errorName != null
                && !errorName.isEmpty()
                && errorLineString != null
                && !errorLineString.trim().isEmpty()) {
              errorName = errorName.trim();
              int start = errorLineString.indexOf(errorName) + 1;
              int end = start + errorName.length();
              cause.setStartColumn(start);
              cause.setEndColumn(end);
              cause.setEndLine(end);
            }
          }
        } else {
          errorName = errorName.replace("\\n", "").trim();
          int line = getValidErrorLine(cause.getLine(), errorName);
          errorLineString = this.source.getSource().getLine(line, new Janitor());
          if (errorLineString != null) {
            int start = errorLineString.indexOf(errorName) + 1;
            int end = start + errorName.length();
            cause.setLine(line);
            cause.setStartColumn(start);
            cause.setEndColumn(end);
            cause.setEndLine(end);
          }
        }
      }
    }

    this.cause = cause;
    cause.setSourceLocator(source != null ? source.getName() : null);
  }

  /** Returns the underlying SyntaxException. */
  public SyntaxException getCause() {
    return this.cause;
  }

  /** Returns the valid error line for EOF SyntaxException. */
  private int getValidErrorLine(int line) {
    while (line >= 0) {
      String string = this.source.getSource().getLine(line, new Janitor());
      if (string != null && !string.trim().isEmpty()) {
        return line;
      }
      line--;
    }
    return line;
  }

  /** Returns the valid error line for invalid SyntaxException. */
  private int getValidErrorLine(int line, String search) {
    while (line >= 0) {
      String string = this.source.getSource().getLine(line, new Janitor());
      if (string != null && !string.isEmpty()) {
        if (string.trim().equals(search)) {
          return line;
        }
      }
      line--;
    }
    return line;
  }

  /** Writes out a nicely formatted summary of the syntax error. */
  @Override
  public void write(PrintWriter output, Janitor janitor) {
    String name = source.getName();
    int line = getCause().getStartLine();
    int column = getCause().getStartColumn();
    String sample = source.getSample(line, column, janitor);

    output.print(name + ": " + line + ": " + getCause().getMessage());
    if (sample != null) {
      output.println();
      output.print(sample);
      output.println();
    }
  }
}
