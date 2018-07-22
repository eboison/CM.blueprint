package com.coremedia.livecontext.ecommerce.ibm.login;

import com.coremedia.livecontext.ecommerce.common.StoreContext;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Service interface to logon to the catalog.
 */
public interface LoginService {

  /**
   * Login an arbitraty user for a current store context.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context current store contexttext
   * @return The users credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials loginIdentity(String username, String password, @NonNull StoreContext context);

  /**
   * Login a service user for a current store context.
   * This user session will be reused for all user contexts.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context current store contexttext
   * @return The service credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials loginServiceIdentity(@NonNull StoreContext context);

  /**
   * Logout a service user for a current store context.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context the current store context
   * @return true if the the logout was successful
   * @throws com.coremedia.livecontext.ecommerce.common.CommerceException
   */
  boolean logoutServiceIdentity(@NonNull StoreContext context);

  /**
   * Renew a service user login for a current store context.
   * This user session will be reused for all user contexts.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context the current store context
   * @return The service credentials if the login was valid
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  WcCredentials renewServiceIdentityLogin(@NonNull StoreContext context);

  /**
   * Gets a preview token for the current store context.
   * The preview token will be only returned if the context contains a certain property that can be previewed.
   * This classification is different for each commerce implementation as well as the token mechanism itself.
   * Often there are settings that are assigned to the user or usage time (like the user segments or preview date).
   * Other ecommerce systems are using preview tokens to support workspaces.
   * <p>This operation depends on the current {@link com.coremedia.livecontext.ecommerce.common.StoreContext}.</p>
   * @param context the current store context
   * @return The preview token requested for the current store context
   * @throws com.coremedia.livecontext.ecommerce.common.InvalidLoginException if logon was not successful.
   */
  @Nullable
  WcPreviewToken getPreviewToken(@NonNull StoreContext context);

  /**
   * Internal routine to clear all service credentials from cache.
   */
  void clearIdentityCache();

}
