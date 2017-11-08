package com.coremedia.livecontext.handler;

import com.coremedia.blueprint.base.links.UrlPrefixResolver;
import com.coremedia.blueprint.base.settings.SettingsService;
import com.coremedia.blueprint.cae.handlers.PageHandlerBase;
import com.coremedia.blueprint.cae.handlers.PreviewHandler;
import com.coremedia.cap.content.ContentRepository;
import com.coremedia.cap.multisite.Site;
import com.coremedia.livecontext.context.LiveContextNavigation;
import com.coremedia.livecontext.context.ResolveContextStrategy;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.CommerceBean;
import com.coremedia.livecontext.ecommerce.common.CommercePropertyProvider;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.handler.util.LiveContextSiteResolver;
import com.coremedia.livecontext.navigation.LiveContextNavigationFactory;
import com.coremedia.objectserver.web.links.LinkFormatter;
import com.coremedia.objectserver.web.links.LinkTransformer;
import com.coremedia.objectserver.web.links.UriComponentsHelper;
import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponents;

import javax.annotation.Nonnull;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.coremedia.blueprint.base.links.UriConstants.Links.ABSOLUTE_URI_KEY;
import static com.coremedia.blueprint.base.links.UriConstants.Links.SCHEME_KEY;

public class LiveContextPageHandlerBase extends PageHandlerBase {
  protected static final String SHOP_NAME_VARIABLE = "shop";
  public static final String URL_PROVIDER_URL_TEMPLATE = "urlTemplate";
  public static final String URL_PROVIDER_STORE_CONTEXT = "storeContext";
  public static final String URL_PROVIDER_QUERY_PARAMS = "queryParams";
  public static final String URL_PROVIDER_SEO_SEGMENT = "seoSegment";
  public static final String URL_PROVIDER_IS_STUDIO_PREVIEW = "isStudioPreview";
  public static final String URL_PROVIDER_IS_INITIAL_STUDIO_REQUEST = "isInitialStudioRequest";
  public static final String HAS_PREVIEW_TOKEN = "hasPreviewToken";
  public static final String URL_PROVIDER_SEARCH_TERM = "searchTerm";
  private static final String PRODUCT_ID = "productId";
  private static final String CATEGORY_ID = "categoryId";
  public static final String CATALOG_ID = "catalogId";
  public static final String P13N_URI_PARAMETER = "p13n_test";

  private ResolveContextStrategy resolveContextStrategy;
  private LiveContextNavigationFactory liveContextNavigationFactory;
  private UrlPrefixResolver urlPrefixResolver;
  private LiveContextSiteResolver siteResolver;
  private SettingsService settingsService;
  private ContentRepository contentRepository;

  private int wcsStorefrontMaxUrlSegments = 2;
  private CommercePropertyProvider urlProvider;
  private LinkFormatter linkFormatter;

  // --- construct and configure ------------------------------------

  @Required
  public void setSettingsService(SettingsService settingsService) {
    this.settingsService = settingsService;
  }

  @Required
  public void setResolveContextStrategy(ResolveContextStrategy resolveContextStrategy) {
    this.resolveContextStrategy = resolveContextStrategy;
  }

  @Required
  public void setUrlProvider(CommercePropertyProvider urlProvider) {
    this.urlProvider = urlProvider;
  }

  @Required
  public void setContentRepository(ContentRepository contentRepository) {
    this.contentRepository = contentRepository;
  }

  @Required
  public void setLiveContextNavigationFactory(LiveContextNavigationFactory liveContextNavigationFactory) {
    this.liveContextNavigationFactory = liveContextNavigationFactory;
  }

  @Required
  public void setUrlPrefixResolver(UrlPrefixResolver urlPrefixResolver) {
    this.urlPrefixResolver = urlPrefixResolver;
  }

  @Required
  public void setSiteResolver(LiveContextSiteResolver siteResolver) {
    this.siteResolver = siteResolver;
  }

  public void setWcsStorefrontMaxUrlSegments(int wcsStorefrontMaxUrlSegments) {
    this.wcsStorefrontMaxUrlSegments = wcsStorefrontMaxUrlSegments;
  }

  // --- features ---------------------------------------------------

  protected SettingsService getSettingsService() {
    return settingsService;
  }

  protected LiveContextNavigation getNavigationContext(@Nonnull Site site, @Nonnull CommerceBean commerceBean) {
    try {
      return resolveContextStrategy.resolveContext(site, commerceBean);
    } catch (Exception ignored) {
      // Do not log, means actually just "not found", does not indicate a problem.
      return null;
    }
  }

  protected LiveContextNavigationFactory getLiveContextNavigationFactory() {
    return liveContextNavigationFactory;
  }

  protected LiveContextSiteResolver getSiteResolver() {
    return siteResolver;
  }

  protected UriComponents absoluteUri(UriComponents originalUri, Object bean, Site site, Map<String,Object> linkParameters, ServletRequest request) {
    if (!isAbsoluteUrlRequested(request)) {
      return originalUri;
    }
    String siteId = site.getId();
    String absoluteUrlPrefix = urlPrefixResolver.getUrlPrefix(siteId, bean, null);
    if (absoluteUrlPrefix == null) {
      throw new IllegalStateException("Cannot calculate an absolute URL for " + bean);
    } else if(!StringUtils.isBlank(absoluteUrlPrefix)) {
      //explicitly set scheme if it is set in link parameters
      String scheme = null;
      if(linkParameters != null) {
        Object schemeAttribute = linkParameters.get(SCHEME_KEY);
        if(schemeAttribute != null) {
          scheme = (String) schemeAttribute;
        }
      }
      return UriComponentsHelper.prefixUri(absoluteUrlPrefix, scheme , originalUri);
    }

    return UriComponentsHelper.prefixUri(absoluteUrlPrefix, null, originalUri);
  }

  protected Object buildCommerceLinkFor(String urlTemplate, String seoSegments, Map<String, Object> queryParams, StoreContext context) {
    Map<String, Object> newQueryParams = new HashMap<>(queryParams);

    Map<String,Object> params = new HashMap<>();
    params.put(URL_PROVIDER_URL_TEMPLATE, urlTemplate);
    params.put(URL_PROVIDER_STORE_CONTEXT, context);
    params.put(URL_PROVIDER_QUERY_PARAMS, newQueryParams);
    params.put(URL_PROVIDER_SEO_SEGMENT, seoSegments);
    params.put(URL_PROVIDER_IS_STUDIO_PREVIEW, isStudioPreview());
    params.put(URL_PROVIDER_IS_INITIAL_STUDIO_REQUEST, isInitialStudioRequest());
    params.put(CATALOG_ID, queryParams.get(CATALOG_ID));

    return urlProvider.provideValue(params);
  }

  /**
   * To evaluate if the newPreviewSession query parameter has to be applied to a commerce url, the evaluator has to know
   * if it's the first request triggered by a studio action (e.g. open in tab) or it's a follow up trigger by an author
   * clicking in the preview.
   * If it's a request triggered by a studio action an author want's to have a cleared session (no logged in user or
   * p13n context). If he tests in studio the preview the author want to stay logged in and use the same p13n context.
   *
   * @return true if the request was triggered by a studio action.
   */
  public static boolean isInitialStudioRequest() {
    return PreviewHandler.isStudioPreviewRequest();
  }

  private boolean isStudioPreview() {
    if (isPreview()) {
      return isStudioPreviewRequest();
    }

    return false;
  }

  /**
   * Builds complete, absolute WCS links with query parameters.
   * Do not postprocess.
   */
  protected Object buildCommerceLinkFor(CommerceBean commerceBean, Map<String, Object> queryParams) {
    String seoSegments = buildSeoSegmentsFor(commerceBean);
    Map<String, Object> params;
    if (StringUtils.isBlank(seoSegments)) {
      // build non-seo URL including category/product id
      params = updateQueryParams(commerceBean, queryParams);
    } else {
      // add catalog id for seo URL
      params = addCatalogIdQueryParam(commerceBean, queryParams);
    }

    return buildCommerceLinkFor(null, seoSegments, params, commerceBean.getContext());
  }

  protected Map<String, Object> updateQueryParams(@Nonnull CommerceBean commerceBean,
                                                  @Nonnull Map<String, Object> queryParams) {
    String externalTechId = commerceBean.getExternalTechId();
    Map<String, Object> modifiableQueryParams = new LinkedHashMap<>(queryParams);
    if (commerceBean instanceof Category) {
      modifiableQueryParams.put(CATEGORY_ID, externalTechId);
    } else if (commerceBean instanceof Product) {
      modifiableQueryParams.put(PRODUCT_ID, externalTechId);
    }
    return addCatalogIdQueryParam(commerceBean, queryParams);
  }

  @Nonnull
  private Map<String, Object> addCatalogIdQueryParam(@Nonnull CommerceBean commerceBean, @Nonnull Map<String, Object> queryParams) {
    Map<String, Object> modifiableQueryParams = new LinkedHashMap<>(queryParams);
    //check if commercebeanlink points to default catalog, if not add catalogId parameter
    commerceBean.getCatalog().filter(c -> !c.isDefaultCatalog())
            .ifPresent(catalog -> {
              modifiableQueryParams.put(CATALOG_ID, catalog.getExternalId());
            });
    return modifiableQueryParams;
  }

  protected boolean isPreview() {
    return contentRepository.isContentManagementServer();
  }

  public static boolean isStudioPreviewRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    return isInitialStudioRequest()
           || isTrue(requestAttributes.getAttribute(HAS_PREVIEW_TOKEN, 0))
           || isTrue(((ServletRequestAttributes) requestAttributes).getRequest().getParameter(P13N_URI_PARAMETER));
  }

  private static boolean isTrue(Object attribute) {
    return Boolean.valueOf(attribute + "");
  }

  protected String getSiteSegment(Site site) {
    return getContentLinkBuilder().getVanityName(site.getSiteRootDocument());
  }

  protected String applyLinkTransformers(String source, HttpServletRequest request, HttpServletResponse response, boolean forRedirect) {
    String result = source;
    if (linkFormatter != null && source != null) {
      List<LinkTransformer> transformers = linkFormatter.getTransformers();
      for (LinkTransformer transformer : transformers) {
        result = transformer.transform(result, null, null, request, response, true);
      }
    }
    return result;
  }

  // --- internal ---------------------------------------------------

  /**
   * Return the SEO URL for the given commerce bean.
   */
  private String buildSeoSegmentsFor(CommerceBean commerceBean) {
    StringBuilder segments = new StringBuilder();
    if (commerceBean instanceof Category) {
      Category category = (Category) commerceBean;
      segments.append(buildSeoBreadCrumbs(category));
    } else if (commerceBean instanceof Product) {
      Product product = (Product) commerceBean;
      String seoSegment = product.getSeoSegment();
      if (!StringUtils.isBlank(seoSegment)) {
        segments.append(buildSeoBreadCrumbs(product.getCategory()));
        segments.append(seoSegment);
      }
    }

    return segments.toString();
  }

  /**
   * This method returns the string
   * with the whole category path of the current category starting with the top level category and ending with the
   * current category + '/'.
   */
  private String buildSeoBreadCrumbs(Category category) {
    StringBuilder segments = new StringBuilder();
    List<Category> breadcrumb = category.getBreadcrumb();
    if (breadcrumb.size() > wcsStorefrontMaxUrlSegments) {
      breadcrumb = breadcrumb.subList(breadcrumb.size() - wcsStorefrontMaxUrlSegments, breadcrumb.size());
    }
    for (Category c : breadcrumb) {
      segments.append(c.getSeoSegment());
      segments.append('/');
    }
    return segments.toString();
  }

  @VisibleForTesting
  SecurityContext getSecurityContext() {
    return SecurityContextHolder.getContext();
  }

  //====================================================================================================================

  private static boolean isAbsoluteUrlRequested(ServletRequest request) {
    Object absolute = request.getAttribute(ABSOLUTE_URI_KEY);
    return "true".equals(absolute) || Boolean.TRUE.equals(absolute);
  }

  public void setLinkFormatter(LinkFormatter linkFormatter) {
    this.linkFormatter = linkFormatter;
  }
}
