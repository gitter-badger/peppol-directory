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
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.pd.indexer.domain.EIndexerWorkItemType;
import com.helger.pd.indexer.mgr.PDMetaManager;
import com.helger.pd.publisher.ui.AbstractAppWebPage;
import com.helger.peppol.identifier.CIdentifier;
import com.helger.peppol.identifier.participant.SimpleParticipantIdentifier;
import com.helger.photon.bootstrap3.alert.BootstrapSuccessBox;
import com.helger.photon.bootstrap3.alert.BootstrapWarnBox;
import com.helger.photon.bootstrap3.button.BootstrapButtonToolbar;
import com.helger.photon.bootstrap3.form.BootstrapForm;
import com.helger.photon.bootstrap3.form.BootstrapFormGroup;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.validation.error.FormErrors;

public final class PageSecureIndexManually extends AbstractAppWebPage
{
  private static final String FIELD_PARTICIPANT_ID = "participantid";

  public PageSecureIndexManually (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Index participant");
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final FormErrors aFormErrors = new FormErrors ();

    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      final String sParticipantID = aWPEC.getAttributeAsString (FIELD_PARTICIPANT_ID);
      final SimpleParticipantIdentifier aParticipantID = SimpleParticipantIdentifier.createFromURIPartOrNull (sParticipantID);

      if (StringHelper.hasNoText (sParticipantID))
        aFormErrors.addFieldError (FIELD_PARTICIPANT_ID, "A participant ID must be provided!");
      else
        if (aParticipantID == null)
          aFormErrors.addFieldError (FIELD_PARTICIPANT_ID, "The provided participant ID is syntactically invalid!");

      if (aFormErrors.isEmpty ())
      {
        if (PDMetaManager.getIndexerMgr ()
                          .queueWorkItem (aParticipantID,
                                          EIndexerWorkItemType.CREATE_UPDATE,
                                          "manually-triggered",
                                          "localhost")
                          .isChanged ())
        {
          aWPEC.postRedirectGet (new BootstrapSuccessBox ().addChild ("The indexing of participant ID '" +
                                                                      sParticipantID +
                                                                      "' was successfully triggered!"));
        }
        else
        {
          aWPEC.postRedirectGet (new BootstrapWarnBox ().addChild ("Participant ID '" +
                                                                   sParticipantID +
                                                                   "' is already in the indexing queue!"));
        }
      }
    }

    final BootstrapForm aForm = aNodeList.addAndReturnChild (createFormSelf (aWPEC));
    aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Participant ID")
                                                 .setCtrl (new HCEdit (new RequestField (FIELD_PARTICIPANT_ID,
                                                                                         CIdentifier.DEFAULT_PARTICIPANT_IDENTIFIER_SCHEME +
                                                                                                               CIdentifier.URL_SCHEME_VALUE_SEPARATOR)))
                                                 .setHelpText ("Enter the fully qualified PEPPOL participant ID you want to index. Must contain the meta scheme!")
                                                 .setErrorList (aFormErrors.getListOfField (FIELD_PARTICIPANT_ID)));

    final BootstrapButtonToolbar aToolbar = aForm.addAndReturnChild (new BootstrapButtonToolbar (aWPEC));
    aToolbar.addHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM);
    aToolbar.addSubmitButton ("Add to queue", EDefaultIcon.YES);
  }
}
