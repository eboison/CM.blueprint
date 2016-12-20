package com.coremedia.blueprint.coderesources;

import com.coremedia.blueprint.base.tree.TreeRelation;
import com.coremedia.cache.Cache;
import com.coremedia.cache.CacheKey;
import com.coremedia.cap.content.Content;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Cache Key for caching the {@link CodeResources} for a CMNavigation.
 * <p>
 * Equality and evaluation of this cache key is determined by two CMNavigation
 * contents which are derived from the constructor arg navigation:
 * <ul>
 *   <li>A CMNavigation that carries direct code in a code property</li>
 *   <li>A CMNavigation that carries a theme</li>
 * </ul>
 * Both may be null.  Direct code is not inherited, so the code carrier content
 * is either the constructor arg navigation itself or null.  The theme is
 * inherited along the navigation hierarchy.
 * <p>
 * If most CMNavigations simply inherit their theme from the root channel, they
 * all share the same cache entry.  Only the presumably few CMNavigations that
 * have direct code or a theme of their own will lead to distinct cache keys of
 * this kind.
 */
public class CodeResourcesCacheKey extends CacheKey<CodeResources> {
  private static final String CMNAVIGATION = "CMNavigation";
  private static final String CMNAVIGATION_THEME = "theme";

  private final boolean developerMode;
  private final String codePropertyName;
  private final CodeCarriers codeCarriers = new CodeCarriers();

  /**
   * Constructor for link building.
   * <p>
   * Determines the relevant channels, that carry the theme and the direct
   * code, from the given navigation.
   */
  public CodeResourcesCacheKey(Content navigation,
                               String codePropertyName,
                               boolean developerMode,
                               TreeRelation<Content> treeRelation) {
    checkIsNavigation(navigation, false);
    this.developerMode = developerMode;
    this.codePropertyName = codePropertyName;
    initCodeCarriers(codeCarriers, navigation, codePropertyName, treeRelation);
  }

  /**
   * Constructor for link resolution.
   * <p>
   * Creates a CacheKey of the given channelWithTheme and channelWithCode.
   */
  public CodeResourcesCacheKey(Content channelWithTheme,
                               Content channelWithCode,
                               String codePropertyName,
                               boolean developerMode) {
    checkIsNavigation(channelWithTheme, true);
    checkIsNavigation(channelWithCode, true);
    this.developerMode = developerMode;
    this.codePropertyName = codePropertyName;
    codeCarriers.setThemeCarrier(channelWithTheme);
    codeCarriers.setCodeCarrier(channelWithCode);
  }

  @Override
  public CodeResources evaluate(Cache cache) {
    return new CodeResourcesImpl(codeCarriers, codePropertyName, developerMode);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CodeResourcesCacheKey that = (CodeResourcesCacheKey) o;
    return developerMode==that.developerMode &&
           codePropertyName.equals(that.codePropertyName) &&
           codeCarriers.equals(that.codeCarriers);
  }

  @Override
  public int hashCode() {
    int result = developerMode ? 1 : 0;
    result = 31 * result + codeCarriers.hashCode();
    result = 31 * result + codePropertyName.hashCode();
    return result;
  }


  // --- internal ---------------------------------------------------

  // Impl note: if you are about to make this non-static, be aware that it is
  // invoked already by the constructor.
  @VisibleForTesting
  static void initCodeCarriers(CodeCarriers cc, Content content, String codeProperty, TreeRelation<Content> treeRelation) {
    // Direct code is not inherited, either the content itself or none at all.
    if (hasDirectCode(content, codeProperty)) {
      cc.setCodeCarrier(content);
    }
    // Grab the nearest theme along the navigation hierarchy.
    List<Content> path = Lists.reverse(treeRelation.pathToRoot(content));
    cc.setThemeCarrier(path.stream().filter(CodeResourcesCacheKey::hasTheme).findFirst().orElse(null));
  }

  private static boolean hasDirectCode(Content content, String codePropertyName) {
    return isNavigation(content) && !content.getLinks(codePropertyName).isEmpty();
  }

  private static boolean hasTheme(Content content) {
    return isNavigation(content) && content.getLink(CMNAVIGATION_THEME)!=null;
  }

  private static void checkIsNavigation(Content content, boolean nullable) {
    if (content==null ? !nullable : !isNavigation(content)) {
      throw new IllegalArgumentException(content + " is no CMNavigation");
    }
  }

  private static boolean isNavigation(Content content) {
    return content.getType().isSubtypeOf(CMNAVIGATION);
  }
}
