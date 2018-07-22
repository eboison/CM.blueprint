package com.coremedia.livecontext.ecommerce.ibm.common;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.AbstractStoreContextProvider;
import com.coremedia.blueprint.lc.test.TestConfig;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper.createContext;
import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_8_0;

public class IbmTestConfig implements TestConfig {

  private static final String SITE_ID = "awesome-site";
  private static final String STORE_ID = System.getProperty("lc.test.storeId", "10201");
  static final String STORE_NAME = System.getProperty("lc.test.storeName", "AuroraESite");
  private static final String B2B_STORE_ID = System.getProperty("lc.test.storeId", "10303");
  private static final String B2B_STORE_ID_V80 = System.getProperty("lc.test.storeId", "715838085");
  private static final String B2B_STORE_NAME = System.getProperty("lc.test.storeName", "AuroraB2BESite");

  private static String CATALOG_NAME = System.getProperty("lc.test.catalogName", "Extended Sites Catalog Asset Store");
  private static CatalogId CATALOG_ID = CatalogId.of(System.getProperty("lc.test.catalogId", "10051"));

  private static CatalogId CATALOG_ID_B2C_V78 = CatalogId.of(System.getProperty("lc.test.catalogId", "10152"));
  private static CatalogId CATALOG_ID_B2B_V78 = CatalogId.of(System.getProperty("lc.test.catalogId", "10151"));
  private static String STORE_ID_V78 = System.getProperty("lc.test.storeId", "10301");

  private static CatalogId CATALOG_ID_B2C_V80 = CatalogId.of(System.getProperty("lc.test.catalogId", "3074457345616676719"));
  private static CatalogId CATALOG_ID_B2B_V80 = CatalogId.of(System.getProperty("lc.test.catalogId", "3074457345616676718"));
  private static String STORE_ID_V80 = System.getProperty("lc.test.storeId", "715838084");

  private static final String LOCALE = "en_US";
  private static final Currency CURRENCY = Currency.getInstance("USD");
  private static final WorkspaceId WORKSPACE_ID = WorkspaceId.of("4711");
  private static final String CONNECTION_ID = "wcs1";

  private static final String USER1_NAME = System.getProperty("lc.test.user1.name", "arover");
  private static final String USER1_ID = System.getProperty("lc.test.user1.id", "3");
  private static final String USER2_NAME = System.getProperty("lc.test.user2.name", "gstevens");
  private static final String USER2_ID = System.getProperty("lc.test.user2.id", "4");
  private static final String PREVIEW_USER_NAME = System.getProperty("lc.test.previewuser.name", "preview");

  private static final String USERSEGMENT1_ID = "8000000000000000551";
  private static final String USERSEGMENT2_ID = "8000000000000000554";
  private static final String USERSEGMENT1_ID_V80 = "8407790678950000502";
  private static final String USERSEGMENT2_ID_V80 = "8407790678950000505";

  private WcsVersion wcsVersion = WcsVersion.fromVersionString(System.getProperty("wcs.version", "8.0")).orElse(null);

  public static final StoreContext STORE_CONTEXT_WITH_WORKSPACE =
          createContext(SITE_ID, STORE_ID, STORE_NAME, CATALOG_ID, LOCALE, CURRENCY);

  {
    STORE_CONTEXT_WITH_WORKSPACE.setWorkspaceId(WORKSPACE_ID);
  }

  public static final StoreContext STORE_CONTEXT_WITHOUT_CATALOG_ID =
          createContext(SITE_ID, STORE_ID, STORE_NAME, null, LOCALE, CURRENCY);

  {
    STORE_CONTEXT_WITH_WORKSPACE.setWorkspaceId(WORKSPACE_ID);
  }

  @NonNull
  @Override
  public StoreContext getStoreContext() {
    return getStoreContext(CURRENCY);
  }

  @NonNull
  public StoreContext getStoreContext(@NonNull Currency currency) {
    StoreContext result = createInitialStoreContext(currency);

    Map replacements = new HashMap<>();
    replacements.put("storeId", result.getStoreId());
    replacements.put("catalogId", result.getCatalogId());
    replacements.put("locale", result.getLocale());
    StoreContextHelper.setReplacements(result, replacements);

    result.put(AbstractStoreContextProvider.CONFIG_KEY_WCS_VERSION, wcsVersion);
    result.put("site", "mySiteIndicator");

    return result;
  }

  @NonNull
  private StoreContext createInitialStoreContext(@NonNull Currency currency) {
    switch (wcsVersion) {
      case WCS_VERSION_8_0:
        return createContext(SITE_ID, STORE_ID_V80, STORE_NAME, CATALOG_ID_B2C_V80, LOCALE, currency);
      case WCS_VERSION_7_8:
        return createContext(SITE_ID, STORE_ID_V78, STORE_NAME, CATALOG_ID_B2C_V78, LOCALE, currency);
      default:
        return createContext(SITE_ID, STORE_ID, STORE_NAME, CATALOG_ID, LOCALE, currency);
    }
  }

  @Override
  public String getConnectionId() {
    return "wcs1";
  }

  @NonNull
  @Override
  public StoreContext getGermanStoreContext() {
    StoreContext result = getStoreContext();
    StoreContextHelper.setLocale(result, new Locale("de"));
    return result;
  }

  @NonNull
  public StoreContext getB2BStoreContext() {
    StoreContext context = createInitialB2BStoreContext();

    Map replacements = new HashMap<>();
    replacements.put("storeId", context.getStoreId());
    replacements.put("catalogId", context.getCatalogId());
    replacements.put("locale", context.getLocale());
    StoreContextHelper.setReplacements(context, replacements);

    context.put(AbstractStoreContextProvider.CONFIG_KEY_WCS_VERSION, wcsVersion);
    context.put("site", "myB2BSiteIndicator");
    context.put("dynamicPricing.enabled", true);

    return context;
  }

  @NonNull
  private StoreContext createInitialB2BStoreContext() {
    switch (wcsVersion) {
      case WCS_VERSION_8_0:
        return createContext(SITE_ID, B2B_STORE_ID_V80, B2B_STORE_NAME, CATALOG_ID_B2B_V80, LOCALE, CURRENCY);
      case WCS_VERSION_7_8:
        return createContext(SITE_ID, B2B_STORE_ID, B2B_STORE_NAME, CATALOG_ID_B2B_V78, LOCALE, CURRENCY);
      default:
        return createContext(SITE_ID, STORE_ID, STORE_NAME, CATALOG_ID, LOCALE, CURRENCY);
    }
  }

  @NonNull
  public StoreContext getStoreContextWithWorkspace() {
    StoreContext result = getStoreContext();
    result.setWorkspaceId(WORKSPACE_ID);
    return result;
  }

  @Override
  public String getCatalogName() {
    return CATALOG_NAME;
  }

  @Override
  public String getStoreName() {
    return StoreContextHelper.getStoreName(getStoreContext());
  }

  public String getStoreId() {
    return StoreContextHelper.getStoreId(getStoreContext());
  }

  public Locale getLocale() {
    return StoreContextHelper.getLocale(getStoreContext());
  }

  public String getUser1Name() {
    return USER1_NAME;
  }

  public String getUser1Id() {
    return USER1_ID;
  }

  public String getUser2Name() {
    return USER2_NAME;
  }

  public String getUser2Id() {
    return USER2_ID;
  }

  public String getPreviewUserName() {
    return PREVIEW_USER_NAME;
  }

  public String getUserSegment1Id() {
    return WCS_VERSION_8_0 == wcsVersion ? USERSEGMENT1_ID_V80 : USERSEGMENT1_ID;
  }

  public String getUserSegment2Id() {
    return WCS_VERSION_8_0 == wcsVersion ? USERSEGMENT2_ID_V80 : USERSEGMENT2_ID;
  }

  public WcsVersion getWcsVersion() {
    return wcsVersion;
  }

  public void setWcsVersion(@NonNull String wcsVersion) {
    this.wcsVersion = WcsVersion.fromVersionString(wcsVersion).orElse(null);
  }
}
