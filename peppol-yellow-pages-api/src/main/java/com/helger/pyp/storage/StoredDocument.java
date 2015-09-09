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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.lucene.document.Document;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.CollectionHelper;

/**
 * This class represents a document stored in the Lucene index but with a nicer
 * API to not work on a field basis.
 *
 * @author Philip Helger
 */
public class StoredDocument
{
  private String m_sParticipantID;
  private String m_sOwnerID;
  private String m_sCountryCode;
  private String m_sName;
  private String m_sGeoInfo;
  private final List <StoredIdentifier> m_aIdentifiers = new ArrayList <> ();
  private String m_sFreeText;
  private boolean m_bDeleted;

  protected StoredDocument ()
  {}

  public void setParticipantID (@Nonnull @Nonempty final String sParticipantID)
  {
    ValueEnforcer.notEmpty (sParticipantID, "ParticipantID");
    m_sParticipantID = sParticipantID;
  }

  @Nonnull
  @Nonempty
  public String getParticipantID ()
  {
    return m_sParticipantID;
  }

  public void setOwnerID (@Nonnull @Nonempty final String sOwnerID)
  {
    ValueEnforcer.notEmpty (sOwnerID, "OwnerID");
    m_sOwnerID = sOwnerID;
  }

  @Nonnull
  @Nonempty
  public String getOwnerID ()
  {
    return m_sOwnerID;
  }

  @Nullable
  public String getCountryCode ()
  {
    return m_sCountryCode;
  }

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @Nullable
  public String getGeoInfo ()
  {
    return m_sGeoInfo;
  }

  @Nonnull
  @ReturnsMutableCopy
  public List <StoredIdentifier> getAllIdentifiers ()
  {
    return CollectionHelper.newList (m_aIdentifiers);
  }

  @Nonnull
  public String getFreeText ()
  {
    return m_sFreeText;
  }

  public boolean isDeleted ()
  {
    return m_bDeleted;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static StoredDocument create (@Nonnull final Document aDoc)
  {
    final StoredDocument ret = new StoredDocument ();
    ret.setParticipantID (aDoc.get (CPYPStorage.FIELD_PARTICIPANTID));
    ret.setOwnerID (aDoc.get (CPYPStorage.FIELD_OWNERID));
    ret.m_sCountryCode = aDoc.get (CPYPStorage.FIELD_COUNTRY);
    ret.m_sName = aDoc.get (CPYPStorage.FIELD_NAME);
    ret.m_sGeoInfo = aDoc.get (CPYPStorage.FIELD_GEOINFO);
    final String [] aIDTypes = aDoc.getValues (CPYPStorage.FIELD_IDENTIFIER_TYPE);
    final String [] aIDValues = aDoc.getValues (CPYPStorage.FIELD_IDENTIFIER);
    if (aIDTypes.length != aIDValues.length)
      throw new IllegalStateException ("Different number of identifier types and values");
    for (int i = 0; i < aIDTypes.length; ++i)
      ret.m_aIdentifiers.add (new StoredIdentifier (aIDTypes[i], aIDValues[i]));
    ret.m_sFreeText = aDoc.get (CPYPStorage.FIELD_FREETEXT);
    ret.m_bDeleted = aDoc.getField (CPYPStorage.FIELD_DELETED) != null;
    return ret;
  }
}
