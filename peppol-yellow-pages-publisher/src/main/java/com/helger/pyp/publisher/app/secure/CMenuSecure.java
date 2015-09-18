/**
 * Copyright (C) 2015 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.pyp.publisher.app.secure;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class CMenuSecure
{
  // Menu item IDs
  public static final String MENU_INDEXER = "indexer";
  public static final String MENU_INDEX_MANUALLY = "index_manually";

  public static final String MENU_ADMIN = "admin";
  public static final String MENU_CHANGE_PASSWORD = "change_password";

  private CMenuSecure ()
  {}
}