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
package com.helger.pd.indexer.storage;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * This class represents a single business contact as stored by Lucene
 * consisting of several fields.
 *
 * @author Philip Helger
 */
@Immutable
public final class PDStoredBusinessContact
{
  private final String m_sDescription;
  private final String m_sName;
  private final String m_sPhone;
  private final String m_sEmail;

  public PDStoredBusinessContact (@Nullable final String sDescription,
                                  @Nullable final String sName,
                                  @Nullable final String sPhone,
                                  @Nullable final String sEmail)
  {
    m_sDescription = sDescription;
    m_sName = sName;
    m_sPhone = sPhone;
    m_sEmail = sEmail;
  }

  @Nullable
  public String getDescription ()
  {
    return m_sDescription;
  }

  @Nullable
  public String getName ()
  {
    return m_sName;
  }

  @Nullable
  public String getPhone ()
  {
    return m_sPhone;
  }

  @Nullable
  public String getEmail ()
  {
    return m_sEmail;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;
    final PDStoredBusinessContact rhs = (PDStoredBusinessContact) o;
    return EqualsHelper.equals (m_sDescription, rhs.m_sDescription) &&
           EqualsHelper.equals (m_sName, rhs.m_sName) &&
           EqualsHelper.equals (m_sPhone, rhs.m_sPhone) &&
           EqualsHelper.equals (m_sEmail, rhs.m_sEmail);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_sDescription).append (m_sName).append (m_sPhone).append (m_sEmail).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Description", m_sDescription)
                                       .append ("Name", m_sName)
                                       .append ("Phone", m_sPhone)
                                       .append ("Email", m_sEmail)
                                       .toString ();
  }
}
