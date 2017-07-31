/**
 * Copyright (C) 2014-2017 Philip Helger (www.helger.com)
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
package com.helger.peppol.as2server.app;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.io.resource.IReadableResource;
import com.helger.commons.string.StringHelper;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.scope.singleton.AbstractGlobalSingleton;
import com.helger.settings.ISettings;
import com.helger.settings.exchange.configfile.ConfigFile;
import com.helger.settings.exchange.configfile.ConfigFileBuilder;

/**
 * This class provides access to the web application settings. The order of the
 * properties file resolving is as follows:
 * <ol>
 * <li>Check for the value of the system property
 * <code>peppol.ap.webapp.properties.path</code></li>
 * <li>Check for the value of the system property
 * <code>ap.webapp.properties.path</code></li>
 * <li>The filename <code>private-webapp.properties</code> in the root of the
 * classpath</li>
 * <li>The filename <code>webapp.properties</code> in the root of the
 * classpath</li>
 * </ol>
 *
 * @author Philip Helger
 */
public final class AppSettings extends AbstractGlobalSingleton
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AppSettings.class);

  private static final ConfigFile s_aConfigFile;

  static
  {
    final ConfigFileBuilder aCFB = new ConfigFileBuilder ().addPathFromSystemProperty ("peppol.as2-server.properties.path")
                                                           .addPathFromSystemProperty ("as2-server.properties.path")
                                                           .addPath ("private-as2-server.properties")
                                                           .addPath ("as2-server.properties");

    s_aConfigFile = aCFB.build ();
    if (!s_aConfigFile.isRead ())
      throw new IllegalStateException ("Failed to read PEPPOL AP AS2 server properties from " + aCFB.getAllPaths ());
    s_aLogger.info ("Read PEPPOL AP AS2 server properties from " + s_aConfigFile.getReadResource ().getPath ());
  }

  @Deprecated
  @UsedViaReflection
  private AppSettings ()
  {}

  @Nonnull
  public static ISettings getSettingsObject ()
  {
    return s_aConfigFile.getSettings ();
  }

  @Nonnull
  public static IReadableResource getSettingsResource ()
  {
    return s_aConfigFile.getReadResource ();
  }

  /**
   * @return The SML to use for the SMP client lookup. Never <code>null</code>.
   *         If an invalid value is specified, the production SML is used.
   */
  @Nonnull
  public static ISMLInfo getSMLToUse ()
  {
    final String sSMLID = s_aConfigFile.getAsString ("sml.id");
    return ESML.getFromIDOrDefault (sSMLID, ESML.DIGIT_PRODUCTION);
  }

  @Nullable
  private static File _getAsFile (@Nullable final String sPath)
  {
    if (StringHelper.hasNoText (sPath))
      return null;
    try
    {
      return new File (sPath).getAbsoluteFile ().getCanonicalFile ();
    }
    catch (final IOException ex)
    {
      throw new UncheckedIOException ("Error in getCanonicalFile", ex);
    }
  }

  /**
   * @return The absolute folder/directory path from which sending should occur.
   *         May be <code>null</code> in which case startup should fail.
   */
  @Nullable
  public static File getFolderForSending ()
  {
    return _getAsFile (s_aConfigFile.getAsString ("folder.sending"));
  }

  /**
   * @return The absolute folder/directory path in which failed sending files
   *         are moved. May be <code>null</code> in which case startup should
   *         fail.
   */
  @Nullable
  public static File getFolderForSendingErrors ()
  {
    return _getAsFile (s_aConfigFile.getAsString ("folder.sending.error"));
  }

  /**
   * @return The absolute folder/directory path in which received files are
   *         stored. May be <code>null</code> in which case startup should fail.
   */
  @Nullable
  public static File getFolderForReceiving ()
  {
    return _getAsFile (s_aConfigFile.getAsString ("folder.receiving"));
  }

  /**
   * @return The absolute folder/directory path in which failed received files
   *         are stored. May be <code>null</code> in which case startup should
   *         fail.
   */
  @Nullable
  public static File getFolderForReceivingErrors ()
  {
    return _getAsFile (s_aConfigFile.getAsString ("folder.receiving.error"));
  }
}
