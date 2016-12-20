package com.coremedia.blueprint.themeimporter;

import com.coremedia.blueprint.localization.LocalizationService;
import com.coremedia.cap.common.CapConnection;
import com.coremedia.cap.content.Content;
import com.coremedia.cap.test.xmlrepo.XmlRepoConfiguration;
import com.coremedia.cap.test.xmlrepo.XmlUapiConfig;
import com.coremedia.mimetype.TikaMimeTypeService;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ThemeImporterJSTest.LocalConfig.class)
public class ThemeImporterJSTest {
  private static final String THEMES = "/Themes";
  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Inject
  private CapConnection capConnection;

  @Configuration
  @Import(XmlRepoConfiguration.class)
  public static class LocalConfig {
    @Bean
    @Scope(SCOPE_SINGLETON)
    public XmlUapiConfig xmlUapiConfig() {
      return XmlUapiConfig.builder().withContentTypes("classpath:framework/doctypes/blueprint/blueprint-doctypes.xml").build();
    }
  }

  private InputStream jsTheme;
  private ThemeImporter themeImporter;

  @Before
  public void setUp() throws Exception {
    jsTheme = getClass().getResource("./testjavascript-theme.zip").openStream();
    TikaMimeTypeService mimeTypeService = new TikaMimeTypeService();
    mimeTypeService.init();
    themeImporter = new ThemeImporter(capConnection, mimeTypeService, new LocalizationService(null, null, null));
  }

  @After
  public void tearDown() {
    IOUtils.closeQuietly(jsTheme);
  }

  @Test
  public void extractFromZip() {
    themeImporter.importThemes(THEMES, jsTheme);

    String jsThemePath = THEMES + "/testjavascript/Testjavascript Theme";
    Content jsTheme = capConnection.getContentRepository().getChild(jsThemePath);
    Assert.assertNotNull(jsTheme);

    List<Content> jsls = jsTheme.getLinks("javaScriptLibs");
    Assert.assertNotNull(jsls);
    Assert.assertEquals(2, jsls.size());
    checkJs(jsls.get(0), true, "lte IE 9");
    checkJs(jsls.get(1), false, "");

    List<Content> jss = jsTheme.getLinks("javaScripts");
    Assert.assertNotNull(jss);
    Assert.assertEquals(2, jss.size());
    checkJs(jss.get(0), true, "");
    checkJs(jss.get(1), false, "gte IE 10");
  }

  private void checkJs(Content content, boolean inHead, String ieExpression) {
    assertEquals(inHead, content.getBoolean("inHead"));
    assertEquals(ieExpression, content.getString("ieExpression"));
  }
}