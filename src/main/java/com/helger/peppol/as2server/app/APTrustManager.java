/**
 * Copyright (C) 2015-2017 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Version: MPL 1.1/EUPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at:
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Copyright The PEPPOL project (http://www.peppol.eu)
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL
 * (the "Licence"); You may not use this work except in compliance
 * with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * If you wish to allow use of your version of this file only
 * under the terms of the EUPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the EUPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the EUPL License.
 */
package com.helger.peppol.as2server.app;

import java.security.KeyStore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.exception.InitializationException;
import com.helger.peppol.utils.PeppolKeyStoreHelper;
import com.helger.scope.singleton.AbstractGlobalSingleton;
import com.helger.security.keystore.EKeyStoreLoadError;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKeyStore;

/**
 * This class holds the global trust store.
 *
 * @author Philip Helger
 */
public final class APTrustManager extends AbstractGlobalSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (APTrustManager.class);

  private static final AtomicBoolean s_aCertificateValid = new AtomicBoolean (false);
  private static EKeyStoreLoadError s_eInitError;
  private static String s_sInitError;

  private KeyStore m_aTrustStore;

  private void _loadCertificates () throws InitializationException
  {
    // Reset every time
    s_aCertificateValid.set (false);
    s_sInitError = null;
    m_aTrustStore = null;

    // Load the trust store
    final LoadedKeyStore aTrustStoreLoading = KeyStoreHelper.loadKeyStore (AppSettings.getTrustStoreType (),
                                                                           AppSettings.getTrustStorePath (),
                                                                           AppSettings.getTrustStorePassword ());
    if (aTrustStoreLoading.isFailure ())
    {
      s_eInitError = aTrustStoreLoading.getError ();
      s_sInitError = PeppolKeyStoreHelper.getLoadError (aTrustStoreLoading);
      throw new InitializationException (s_sInitError);
    }
    m_aTrustStore = aTrustStoreLoading.getKeyStore ();

    s_aLogger.info ("APTrustManager successfully initialized with truststore '" +
                    AppSettings.getTrustStorePath () +
                    "'");
    s_aCertificateValid.set (true);
  }

  @Deprecated
  @UsedViaReflection
  public APTrustManager () throws InitializationException
  {
    _loadCertificates ();
  }

  @Nonnull
  public static APTrustManager getInstance ()
  {
    return getGlobalSingleton (APTrustManager.class);
  }

  /**
   * @return The global trust store to be used. This trust store is never
   *         reloaded and must be present.
   */
  @Nullable
  public KeyStore getTrustStore ()
  {
    return m_aTrustStore;
  }

  /**
   * @return A shortcut method to determine if the certification configuration
   *         is valid or not. This method can be used, even if
   *         {@link #getInstance()} throws an exception.
   */
  public static boolean isCertificateValid ()
  {
    return s_aCertificateValid.get ();
  }

  /**
   * If the certificate is not valid according to {@link #isCertificateValid()}
   * this method can be used to determine the error detail code.
   *
   * @return <code>null</code> if initialization was successful.
   */
  @Nullable
  public static EKeyStoreLoadError getInitializationErrorCode ()
  {
    return s_eInitError;
  }

  /**
   * If the certificate is not valid according to {@link #isCertificateValid()}
   * this method can be used to determine the error detail message.
   *
   * @return <code>null</code> if initialization was successful.
   */
  @Nullable
  public static String getInitializationError ()
  {
    return s_sInitError;
  }

  public static void reloadFromConfiguration ()
  {
    try
    {
      final APTrustManager aInstance = getGlobalSingletonIfInstantiated (APTrustManager.class);
      if (aInstance != null)
        aInstance._loadCertificates ();
      else
        getInstance ();
    }
    catch (final Exception ex)
    {}
  }
}
