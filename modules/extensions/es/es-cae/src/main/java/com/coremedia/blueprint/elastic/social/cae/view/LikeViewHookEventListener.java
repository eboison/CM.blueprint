package com.coremedia.blueprint.elastic.social.cae.view;

import com.coremedia.blueprint.base.elastic.social.configuration.ElasticSocialConfiguration;
import com.coremedia.blueprint.elastic.social.cae.controller.ContributionResult;
import com.coremedia.blueprint.elastic.social.cae.controller.LikeResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;
import java.util.List;

/**
 * A {@link com.coremedia.objectserver.view.events.ViewHookEventListener} that
 * is responsible for adding the comments widget to rendered views.
 */
@Named
public class LikeViewHookEventListener extends AbstractESViewHookEventListener {

  @Override
  protected boolean isEnabled(@Nonnull ElasticSocialConfiguration elasticSocialConfiguration) {
    return elasticSocialConfiguration.isLikeEnabled();
  }

  @Nullable
  @Override
  protected List<String> getWhitelistTypes(@Nonnull ElasticSocialConfiguration elasticSocialConfiguration) {
    return elasticSocialConfiguration.getLikeDocumentTypes();
  }

  @Nullable
  @Override
  protected ContributionResult getContribution(@Nonnull Object target) {
    return new LikeResult(target);
  }

  @Override
  public int getOrder() {
    return DEFAULT_ORDER;
  }
}
