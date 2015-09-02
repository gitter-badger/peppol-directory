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
package com.helger.pyp.indexer.jetty;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.charset.CCharset;
import com.helger.commons.io.stream.StreamHelper;

public final class JettyStopPYPINDEXER
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (JettyStopPYPINDEXER.class);

  public static void main (final String [] args) throws IOException
  {
    final Socket s = new Socket (InetAddress.getByName (null), JettyMonitor.STOP_PORT);
    try
    {
      s.setSoLinger (false, 0);

      final OutputStream out = s.getOutputStream ();
      s_aLogger.info ("Sending jetty stop request");
      out.write ((JettyMonitor.STOP_KEY + "\r\nstop\r\n").getBytes (CCharset.CHARSET_ISO_8859_1_OBJ));
      out.flush ();
    }
    catch (final ConnectException ex)
    {
      s_aLogger.warn ("Jetty is not running");
    }
    finally
    {
      StreamHelper.close (s);
    }
  }
}
