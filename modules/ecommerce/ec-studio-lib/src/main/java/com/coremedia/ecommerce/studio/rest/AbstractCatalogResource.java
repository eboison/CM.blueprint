package com.coremedia.ecommerce.studio.rest;

import com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.CurrentCommerceConnection;
import com.coremedia.blueprint.base.livecontext.ecommerce.common.StoreContextImpl;
import com.coremedia.livecontext.ecommerce.catalog.Catalog;
import com.coremedia.livecontext.ecommerce.catalog.CatalogAlias;
import com.coremedia.livecontext.ecommerce.catalog.CatalogId;
import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceObject;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.rest.linking.EntityResource;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import java.util.Optional;

import static com.coremedia.blueprint.base.livecontext.ecommerce.common.CatalogAliasTranslationService.DEFAULT_CATALOG_ALIAS;

/**
 * An abstract catalog object as a RESTful resource.
 */
public abstract class AbstractCatalogResource<Entity extends CommerceObject> implements EntityResource<Entity> {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractCatalogResource.class);

  private CatalogAliasTranslationService catalogAliasTranslationService;

  private static final String ID = "id";
  private static final String SITE_ID = "siteId";
  protected static final String CATALOG_ALIAS = "catalogAlias";
  private static final String WORKSPACE_ID = "workspaceId";

  private String id;
  private String siteId;
  protected CatalogAlias catalogAlias;
  private String workspaceId = StoreContextImpl.NO_WS_MARKER;

  @Nullable
  @Override
  public Entity getEntity() {
    if (getStoreContext() == null) {
      return null;
    }

    return doGetEntity();
  }

  protected abstract Entity doGetEntity();

  @GET
  public AbstractCatalogRepresentation get() {
    return getRepresentation();
  }

  protected abstract AbstractCatalogRepresentation getRepresentation();

  public String getId() {
    return id;
  }

  @PathParam(ID)
  public void setId(@Nullable String id) {
    this.id = id;
  }

  @Nonnull
  @VisibleForTesting
  static String decodeId(@Nonnull String id) {
    // At least, encoded `+` chars (`%2B`) must be decoded because
    // some program logic double escapes it in order to avoid `+`
    // characters being unescaped to SPACE characters.
    return id.replace("%2B", "+");
  }

  public String getSiteId() {
    return siteId;
  }

  @PathParam(SITE_ID)
  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getCatalogAlias() {
    return catalogAlias != null ? catalogAlias.value() : null;
  }

  @PathParam(CATALOG_ALIAS)
  public void setCatalogAlias(String catalogAliasValue) {
    CatalogAlias.ofNullable(catalogAliasValue).ifPresent(catalogAlias -> {
      this.catalogAlias = isDefaultCatalog(catalogAlias) ? DEFAULT_CATALOG_ALIAS : catalogAlias;
    });
  }

  public String getWorkspaceId() {
    return workspaceId;
  }

  @PathParam(WORKSPACE_ID)
  public void setWorkspaceId(@Nullable String workspaceId) {
    this.workspaceId = workspaceId == null ? StoreContextImpl.NO_WS_MARKER : workspaceId;
  }

  @Nullable
  protected StoreContext getStoreContext() {
    StoreContext storeContext = CurrentCommerceConnection.find()
            .map(CommerceConnection::getStoreContext)
            .map(StoreContext::getClone)
            .orElse(null);

    if (storeContext == null) {
      return null;
    }

    storeContext.setWorkspaceId(workspaceId);
    if (catalogAlias != null && catalogAliasTranslationService != null) {
      Optional<CatalogId> catalogId = catalogAliasTranslationService.getCatalogIdForAlias(catalogAlias, siteId);
      storeContext.setCatalog(catalogAlias, catalogId.orElse(null));
    }
    storeContext.setSiteId(siteId);
    return storeContext;
  }

  @Nonnull
  protected CommerceConnection getConnection() {
    return CurrentCommerceConnection.get();
  }

  private static boolean isDefaultCatalog(CatalogAlias catalogAlias) {
    if (catalogAlias.equals(DEFAULT_CATALOG_ALIAS)) {
      return true;
    }

    CommerceConnection commerceConnection = CurrentCommerceConnection.get();

    CatalogService catalogService = commerceConnection.getCatalogService();
    StoreContext storeContext = commerceConnection.getStoreContext();

    Optional<Catalog> catalog = catalogService.getCatalog(catalogAlias, storeContext);
    return catalog.isPresent() && catalog.get().isDefaultCatalog();
  }

  @Autowired
  public void setCatalogAliasTranslationService(CatalogAliasTranslationService catalogAliasTranslationService) {
    this.catalogAliasTranslationService = catalogAliasTranslationService;
  }

  protected CatalogAliasTranslationService getCatalogAliasTranslationService() {
    return catalogAliasTranslationService;
  }
}
