/**
 * Copyright (C) 2014-2015 Philip Helger (www.helger.com)
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
package com.helger.pyp.publisher.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.vendor.VendorInfo;
import com.helger.photon.bootstrap3.servlet.AbstractWebAppListenerMultiAppBootstrap;
import com.helger.photon.core.app.CApplication;
import com.helger.photon.core.app.context.LayoutExecutionContext;
import com.helger.photon.core.app.init.IApplicationInitializer;
import com.helger.pyp.publisher.app.AppCommonUI;
import com.helger.pyp.publisher.app.AppInternalErrorHandler;
import com.helger.pyp.publisher.app.AppSecurity;
import com.helger.pyp.publisher.app.InitializerPublic;
import com.helger.pyp.publisher.app.InitializerSecure;
import com.helger.pyp.publisher.app.MetaManager;
import com.helger.pyp.settings.PYPSettings;

/**
 * This listener is invoked during the servlet initiailization. This is
 * basically a ServletContextListener.
 *
 * @author Philip Helger
 */
public final class AppWebAppListener extends AbstractWebAppListenerMultiAppBootstrap <LayoutExecutionContext>
{
  @Override
  protected String getInitParameterDebug (@Nonnull final ServletContext aSC)
  {
    return PYPSettings.getGlobalDebug ();
  }

  @Override
  protected String getInitParameterProduction (@Nonnull final ServletContext aSC)
  {
    return PYPSettings.getGlobalProduction ();
  }

  @Override
  protected String getDataPath (@Nonnull final ServletContext aSC)
  {
    return PYPSettings.getDataPath ();
  }

  @Override
  protected boolean shouldCheckFileAccess (@Nonnull final ServletContext aSC)
  {
    return PYPSettings.isCheckFileAccess ();
  }

  @Override
  @Nonnull
  @Nonempty
  protected Map <String, IApplicationInitializer <LayoutExecutionContext>> getAllInitializers ()
  {
    final Map <String, IApplicationInitializer <LayoutExecutionContext>> ret = new HashMap <String, IApplicationInitializer <LayoutExecutionContext>> ();
    ret.put (CApplication.APP_ID_SECURE, new InitializerSecure ());
    ret.put (CApplication.APP_ID_PUBLIC, new InitializerPublic ());
    return ret;
  }

  @Override
  protected void initGlobals ()
  {
    // Internal stuff:
    VendorInfo.setVendorName ("Philip Helger");
    VendorInfo.setVendorURL ("http://www.helger.com");
    VendorInfo.setVendorEmail ("philip@helger.com");
    VendorInfo.setVendorLocation ("Vienna, Austria");
    VendorInfo.setInceptionYear (2015);

    super.initGlobals ();

    // UI stuff
    AppCommonUI.init ();

    // Set all security related stuff
    AppSecurity.init ();

    // Load managers
    MetaManager.getInstance ();

    // Setup error handler
    AppInternalErrorHandler.doSetup ();
  }
}