package com.coremedia.ecommerce.studio.rest;

import com.coremedia.ecommerce.studio.rest.model.Store;
import com.coremedia.livecontext.ecommerce.common.CommerceConnection;
import com.coremedia.livecontext.ecommerce.common.CommerceException;
import com.coremedia.rest.linking.LocationHeaderResourceFilter;
import com.coremedia.rest.linking.RemoteBeanLink;
import com.sun.jersey.spi.container.ResourceFilters;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * A store {@link com.coremedia.ecommerce.studio.rest.model.Store} object as a RESTful resource.
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("livecontext/store/{siteId:[^/]+}/{workspaceId:[^/]+}")
public class StoreResource extends AbstractCatalogResource<Store> {

  private static final String SHOP_URL_PBE_PARAM = "shopUrl";

  @Inject
  private PbeShopUrlTargetResolver pbeShopUrlTargetResolver;

  @POST
  @Path("urlService")
  public Object handlePost(@Nonnull Map<String, Object> rawJson) {
    String shopUrlStr = (String) rawJson.get(SHOP_URL_PBE_PARAM);
    String siteId = getSiteId();

    return pbeShopUrlTargetResolver.resolveUrl(shopUrlStr, siteId);
  }

  @Override
  public StoreRepresentation getRepresentation() {
    StoreRepresentation storeRepresentation = new StoreRepresentation();
    fillRepresentation(storeRepresentation);
    return storeRepresentation;
  }

  private void fillRepresentation(StoreRepresentation representation) {
    Store entity = getEntity();

    if (entity == null) {
      LOG.debug("Error loading store bean: store context is null (site: {})", getSiteId());
      throw new CatalogRestException(Response.Status.NOT_FOUND, CatalogRestErrorCodes.COULD_NOT_FIND_CATALOG_BEAN, "Could not load store bean");
    }

    try {
      CommerceConnection connection = getConnection();
      representation.setMarketingEnabled(connection.getMarketingSpotService() != null &&
              !connection.getMarketingSpotService().findMarketingSpots().isEmpty());
      representation.setId(entity.getId());
      representation.setVendorUrl(entity.getVendorUrl());
      representation.setVendorName(entity.getVendorName());
      representation.setVendorVersion(entity.getVendorVersion());
      representation.setContext(getStoreContext());
      representation.setRootCategory(RemoteBeanLink.create(rootCategoryUri(connection)));
    } catch (CommerceException e) {
      LOG.warn("Error loading store bean: {} (site: {})", e.getMessage(), getSiteId());
      throw e;
    }
  }

  private String rootCategoryUri(CommerceConnection connection) {
    String siteId = connection.getStoreContext().getSiteId();
    String workspaceId = connection.getStoreContext().getWorkspaceId();
    return "livecontext/category/" + siteId + "/" + workspaceId + "/" + CategoryResource.ROOT_CATEGORY_ROLE_ID;
  }

  @Override
  protected Store doGetEntity() {
    return new Store(getStoreContext());
  }

  @Override
  public void setEntity(Store store) {
    setSiteId(store.getContext().getSiteId());
    setWorkspaceId(store.getContext().getWorkspaceId());
  }
}
