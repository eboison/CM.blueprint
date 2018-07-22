package com.coremedia.livecontext.ecommerce.ibm.workspace;

import com.coremedia.livecontext.ecommerce.catalog.CatalogService;
import com.coremedia.livecontext.ecommerce.catalog.Category;
import com.coremedia.livecontext.ecommerce.catalog.Product;
import com.coremedia.livecontext.ecommerce.common.BaseCommerceBeanType;
import com.coremedia.livecontext.ecommerce.common.CommerceId;
import com.coremedia.livecontext.ecommerce.common.StoreContext;
import com.coremedia.livecontext.ecommerce.ibm.IbmServiceTestBase;
import com.coremedia.livecontext.ecommerce.ibm.common.IbmCommerceIdProvider;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.ibm.user.UserContextHelper;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import com.coremedia.livecontext.ecommerce.workspace.Workspace;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceId;
import com.coremedia.livecontext.ecommerce.workspace.WorkspaceService;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static com.coremedia.blueprint.lc.test.BetamaxTestHelper.useBetamaxTapes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = IbmServiceTestBase.LocalConfig.class)
@ActiveProfiles(IbmServiceTestBase.LocalConfig.PROFILE)
public class WorkspacesIT extends IbmServiceTestBase {

  @Inject
  WorkspaceService workspaceService;

  @Inject
  CatalogService catalogService;

  @Inject
  private IbmCommerceIdProvider ibmCommerceIdProvider;

  @Test
  public void testFindAllWorkspaces() throws Exception {
    if (useBetamaxTapes()) {
      return;
    }

    Workspace workspace = findWorkspace("Anniversary");
    assertEquals("segment id has wrong format", BaseCommerceBeanType.WORKSPACE, workspace.getId().getCommerceBeanType());
  }

  @Test
  public void testFindTestContentInWorkspace() {
    if (useBetamaxTapes()) {
      return;
    }

    Workspace workspace = findWorkspace("Anniversary");

    StoreContext storeContext = testConfig.getStoreContext();
    storeContext.setWorkspaceId(WorkspaceId.of(workspace.getExternalTechId()));
    StoreContextHelper.setCurrentContext(storeContext);

    CommerceId categoryId = ibmCommerceIdProvider.formatCategoryId(storeContext.getCatalogAlias(), "PC_ForTheCook");
    Category category0 = catalogService.findCategoryById(categoryId, storeContext);
    assertNotNull("category \"PC_ForTheCook\" not found", category0);

    List<Category> subCategories = catalogService.findSubCategories(category0);
    Category category1 = findFirst(subCategories, c -> "PC_Anniversary".equals(c.getExternalId()));
    assertNotNull("category \"PC_Anniversary\" not found", category1);

    List<Product> products = catalogService.findProductsByCategory(category1);
    assertNotNull(products);
    assertFalse(products.isEmpty());

    Product product = findFirst(products, p -> "PC_COOKING_HAT".equals(p.getExternalId()));
    assertNotNull("product \"PC_COOKING_HAT\" not found", product);
  }

  private Workspace findWorkspace(String name) {
    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = UserContext.builder().build();
    UserContextHelper.setCurrentContext(userContext);

    List<Workspace> workspaces = workspaceService.findAllWorkspaces(StoreContextHelper.getCurrentContextOrThrow());
    assertNotNull(workspaces);
    assertFalse(workspaces.isEmpty());

    Workspace workspace = findFirst(workspaces, w -> w.getName().startsWith(name));
    assertNotNull("workspace \"" + name + "...\" not found", workspace);

    return workspace;
  }

  @Nullable
  private static <T> T findFirst(@NonNull Collection<T> items, @NonNull Predicate<T> predicate) {
    return items.stream()
            .filter(predicate)
            .findFirst()
            .orElse(null);
  }
}
