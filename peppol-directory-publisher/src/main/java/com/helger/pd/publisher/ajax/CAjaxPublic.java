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
package com.helger.pd.publisher.ajax;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.pd.publisher.app.AppCommonUI;
import com.helger.photon.core.ajax.IAjaxFunctionDeclaration;
import com.helger.photon.core.ajax.IAjaxInvoker;
import com.helger.photon.core.ajax.decl.PublicApplicationAjaxFunctionDeclaration;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTables;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTablesI18N;

/**
 * This class defines the available ajax functions for the view application.
 *
 * @author Philip Helger
 */
@Immutable
public final class CAjaxPublic
{
  public static final IAjaxFunctionDeclaration DATATABLES = new PublicApplicationAjaxFunctionDeclaration ("dataTables",
                                                                                                          AjaxExecutorDataTables.class);
  public static final IAjaxFunctionDeclaration DATATABLES_I18N = new PublicApplicationAjaxFunctionDeclaration ("datatables-i18n",
                                                                                                               new AjaxExecutorDataTablesI18N (AppCommonUI.DEFAULT_LOCALE));
  public static final IAjaxFunctionDeclaration LOGIN = new PublicApplicationAjaxFunctionDeclaration ("login",
                                                                                                     AjaxExecutorPublicLogin.class);

  private CAjaxPublic ()
  {}

  public static void initAjax (@Nonnull final IAjaxInvoker aAjaxInvoker)
  {
    aAjaxInvoker.registerFunction (DATATABLES);
    aAjaxInvoker.registerFunction (DATATABLES_I18N);
    aAjaxInvoker.registerFunction (LOGIN);
  }
}
