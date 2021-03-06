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
package com.helger.pd.indexer.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.mock.CommonsTestHelper;
import com.helger.commons.random.VerySecureRandom;
import com.helger.commons.thread.ThreadHelper;
import com.helger.pd.businessinformation.PDBusinessInformationType;
import com.helger.pd.businessinformation.PDEntityType;
import com.helger.pd.businessinformation.PDExtendedBusinessInformation;
import com.helger.pd.businessinformation.PDIdentifierType;
import com.helger.pd.indexer.PYPIndexerTestRule;
import com.helger.pd.indexer.clientcert.ClientCertificateValidator;
import com.helger.pd.indexer.mgr.PDIndexerManager;
import com.helger.pd.indexer.mgr.PDMetaManager;
import com.helger.peppol.identifier.doctype.EPredefinedDocumentTypeIdentifier;
import com.helger.peppol.identifier.participant.IPeppolParticipantIdentifier;
import com.helger.peppol.identifier.participant.SimpleParticipantIdentifier;
import com.helger.peppol.utils.KeyStoreHelper;
import com.helger.web.https.DoNothingTrustManager;
import com.helger.web.https.HostnameVerifierAlwaysTrue;

/**
 * Test class for class {@link IndexerResource}.
 *
 * @author Philip Helger
 */
public final class IndexerResourceTest
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (IndexerResourceTest.class);

  @Rule
  public final TestRule m_aRule = new PYPIndexerTestRule ();

  private HttpServer m_aServer;
  private WebTarget m_aTarget;

  @Nonnull
  private static PDExtendedBusinessInformation _createMockBI (@Nonnull final IPeppolParticipantIdentifier aParticipantID)
  {
    final PDBusinessInformationType aBI = new PDBusinessInformationType ();
    {
      final PDEntityType aEntity = new PDEntityType ();
      aEntity.setCountryCode ("AT");
      aEntity.setName ("Philip's mock PEPPOL receiver");
      PDIdentifierType aID = new PDIdentifierType ();
      aID.setType ("mock");
      aID.setValue ("12345678");
      aEntity.addIdentifier (aID);
      aID = new PDIdentifierType ();
      aID.setType ("provided");
      aID.setValue (aParticipantID.getURIEncoded ());
      aEntity.addIdentifier (aID);
      aEntity.setFreeText ("This is a mock entry for testing purposes only");
      aBI.addEntity (aEntity);
    }
    {
      final PDEntityType aEntity = new PDEntityType ();
      aEntity.setCountryCode ("NO");
      aEntity.setName ("Philip's mock PEPPOL receiver 2");
      final PDIdentifierType aID = new PDIdentifierType ();
      aID.setType ("mock");
      aID.setValue ("abcdefgh");
      aEntity.addIdentifier (aID);
      aEntity.setFreeText ("This is another mock entry for testing purposes only");
      aBI.addEntity (aEntity);
    }
    return new PDExtendedBusinessInformation (aBI,
                                              CollectionHelper.newList (EPredefinedDocumentTypeIdentifier.INVOICE_T010_BIS5A_V20.getAsDocumentTypeIdentifier ()));
  }

  @Before
  public void setUp () throws GeneralSecurityException, IOException
  {
    // Set test BI provider
    PDMetaManager.setIndexerMgrFactory (aStorageMgr -> new PDIndexerManager (aStorageMgr).setBusinessInformationProvider (aParticipantID -> _createMockBI (aParticipantID))
                                                                                         .readAndQueueInitialData ());
    PDMetaManager.getInstance ();

    final File aTestClientCertificateKeyStore = new File ("src/test/resources/smp.pilot.jks");
    if (aTestClientCertificateKeyStore.exists ())
    {
      // https
      m_aServer = MockServer.startSecureServer ();

      final KeyStore aKeyStore = KeyStoreHelper.loadKeyStore (aTestClientCertificateKeyStore.getAbsolutePath (), "peppol");
      // Try to create the socket factory from the provided key store
      final KeyManagerFactory aKeyManagerFactory = KeyManagerFactory.getInstance ("SunX509");
      aKeyManagerFactory.init (aKeyStore, "peppol".toCharArray ());

      final SSLContext aSSLContext = SSLContext.getInstance ("TLS");
      aSSLContext.init (aKeyManagerFactory.getKeyManagers (),
                        new TrustManager [] { new DoNothingTrustManager (false) },
                        VerySecureRandom.getInstance ());
      final Client aClient = ClientBuilder.newBuilder ().sslContext (aSSLContext).hostnameVerifier (new HostnameVerifierAlwaysTrue (false)).build ();
      m_aTarget = aClient.target (MockServer.BASE_URI_HTTPS);
    }
    else
    {
      // http only
      s_aLogger.warn ("The SMP pilot keystore is missing for the tests! Client certificate handling will not be tested!");
      ClientCertificateValidator.allowAllForTests (true);

      m_aServer = MockServer.startRegularServer ();

      final Client aClient = ClientBuilder.newClient ();
      m_aTarget = aClient.target (MockServer.BASE_URI_HTTP);
    }
  }

  @After
  public void tearDown ()
  {
    m_aServer.shutdownNow ();
  }

  @Test
  public void testCreateAndDeleteParticipant () throws IOException
  {
    final AtomicInteger aIndex = new AtomicInteger (0);
    final SimpleParticipantIdentifier aPI_0 = SimpleParticipantIdentifier.createWithDefaultScheme ("9915:test0");

    final int nCount = 4;
    CommonsTestHelper.testInParallel (nCount, (Runnable) () -> {
      // Create
      final SimpleParticipantIdentifier aPI = SimpleParticipantIdentifier.createWithDefaultScheme ("9915:test" + aIndex.getAndIncrement ());

      final String sResponseMsg = m_aTarget.path ("1.0").request ().put (Entity.text (aPI.getURIEncoded ()), String.class);
      assertEquals ("", sResponseMsg);
    });

    ThreadHelper.sleep (2000);
    assertTrue (PDMetaManager.getStorageMgr ().containsEntry (aPI_0));

    aIndex.set (0);
    CommonsTestHelper.testInParallel (nCount, (Runnable) () -> {
      // Delete
      final SimpleParticipantIdentifier aPI = SimpleParticipantIdentifier.createWithDefaultScheme ("9915:test" + aIndex.getAndIncrement ());

      final String sResponseMsg = m_aTarget.path ("1.0").path (aPI.getURIEncoded ()).request ().delete (String.class);
      assertEquals ("", sResponseMsg);
    });

    ThreadHelper.sleep (2000);
    assertFalse (PDMetaManager.getStorageMgr ().containsEntry (aPI_0));
  }
}
