package com.coremedia.livecontext.ibm.studio {

import flexunit.framework.TestSuite;

public class TestSuite {
  public static function suite():flexunit.framework.TestSuite {
    var suite:flexunit.framework.TestSuite = new flexunit.framework.TestSuite();

    suite.addTestSuite(EcommerceIbmCatalogLinkPropertyFieldTest);
    suite.addTestSuite(EcommerceIbmCategoryAndProductLinksPropertyFieldTest);

    return suite;
  }
}
}
