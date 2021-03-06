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
package com.helger.pd.publisher.app.secure.page;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.pd.indexer.mgr.PDMetaManager;
import com.helger.pd.publisher.ui.AbstractAppWebPage;
import com.helger.photon.bootstrap3.alert.BootstrapInfoBox;
import com.helger.photon.uicore.page.WebPageExecutionContext;

public final class PageSecureAllParticipants extends AbstractAppWebPage
{
  public PageSecureAllParticipants (@Nonnull @Nonempty final String sID)
  {
    super (sID, "All participants");
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();

    final HCUL aUL = new HCUL ();
    for (final String sParticipantID : PDMetaManager.getStorageMgr ().getAllContainedParticipantIDs ())
      aUL.addItem (sParticipantID);

    if (aUL.hasChildren ())
      aNodeList.addChild (aUL);
    else
      aNodeList.addChild (new BootstrapInfoBox ().addChild ("No participant identifier is yet in the index"));
  }
}
