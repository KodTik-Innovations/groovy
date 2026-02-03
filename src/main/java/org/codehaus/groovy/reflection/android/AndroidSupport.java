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

/*
 * Modifications Copyright 2026 KodTik-Innovations
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

package org.codehaus.groovy.reflection.android;

public abstract class AndroidSupport {
  private static final boolean IS_ANDROID;
  private static final boolean IS_STACKWALKER_AVAILABLE;

  // deenu modify: add isDalvik, isStackwalkerAvailable

  static {
    boolean isAndroid = true;
    try {
      Class.forName("android.app.Activity", false, AndroidSupport.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      isAndroid = false;
    }
    IS_ANDROID = isAndroid;

    // only availavle in java 9+
    boolean isStackwalkerAvailable = true;
    try {
      Class.forName("java.lang.StackWalker", false, AndroidSupport.class.getClassLoader());
    } catch (ClassNotFoundException e) {
      isStackwalkerAvailable = false;
    }
    IS_STACKWALKER_AVAILABLE = isStackwalkerAvailable;
  }

  public static boolean isRunningAndroid() {
    return IS_ANDROID;
  }

  public static boolean isDalvik() {
    return System.getProperty("java.vm.name", "").contains("Dalvik");
  }

  public static boolean isStackwalkerAvailable() {
    return IS_STACKWALKER_AVAILABLE;
  }
}
