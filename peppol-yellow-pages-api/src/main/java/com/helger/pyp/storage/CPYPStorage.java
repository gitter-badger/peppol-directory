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
package com.helger.pyp.storage;

import javax.annotation.concurrent.Immutable;

/**
 * Constants Lucene field names
 *
 * @author Philip Helger
 */
@Immutable
public final class CPYPStorage
{
  public static final String FIELD_PARTICIPANTID = "participantid";
  public static final String FIELD_DOCUMENT_TYPE_ID = "doctypeid";
  public static final String FIELD_OWNERID = "ownerid";
  public static final String FIELD_COUNTRY_CODE = "country";
  public static final String FIELD_NAME = "name";
  public static final String FIELD_GEOINFO = "geoinfo";
  public static final String FIELD_IDENTIFIER_TYPE = "identifiertype";
  public static final String FIELD_IDENTIFIER = "identifier";
  public static final String FIELD_FREETEXT = "freetext";
  public static final String FIELD_DELETED = "deleted";

  private CPYPStorage ()
  {}
}
