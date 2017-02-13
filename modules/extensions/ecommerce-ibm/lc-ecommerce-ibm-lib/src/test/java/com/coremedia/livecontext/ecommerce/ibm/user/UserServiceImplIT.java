package com.coremedia.livecontext.ecommerce.ibm.user;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.MatchRule;
import com.coremedia.livecontext.ecommerce.ibm.SystemProperties;
import com.coremedia.livecontext.ecommerce.ibm.common.AbstractServiceTest;
import com.coremedia.livecontext.ecommerce.ibm.common.StoreContextHelper;
import com.coremedia.livecontext.ecommerce.user.User;
import com.coremedia.livecontext.ecommerce.user.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import javax.inject.Inject;
import java.util.UUID;

import static com.coremedia.livecontext.ecommerce.ibm.common.WcsVersion.WCS_VERSION_7_7;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = AbstractServiceTest.LocalConfig.class)
@ActiveProfiles(AbstractServiceTest.LocalConfig.PROFILE)
public class UserServiceImplIT extends AbstractServiceTest {

  @Inject
  UserServiceImpl testling;

  @Betamax(tape = "psi_testFindPerson", match = {MatchRule.path, MatchRule.query})
  @Test
  public void testFindPerson() throws Exception {
    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = userContextProvider.createContext(null);
    UserContextHelper.setCurrentContext(userContext);
    User user = testling.findCurrentUser();
    assertNotNull(user);
    assertNotNull(user.getLogonId());
    assertNotNull(user.getLogonId().equals(UserContextHelper.getForUserName(userContext)));

    user.getChallengeAnswer();
    user.getChallengeQuestion();
    assertEquals("Hamburg", user.getCity());
    assertEquals("D", user.getCountry());
    assertNotNull(user.getEmail1());
    assertTrue(StringUtils.isEmpty(user.getEmail2()));
    assertTrue(StringUtils.isEmpty(user.getEmail3()));
    assertEquals("Cm", user.getFirstName());
    assertEquals("Admin", user.getLastName());
    assertEquals("cmadmin", user.getLogonId());
    assertTrue(StringUtils.isEmpty(user.getLogonPassword()));
    assertTrue(StringUtils.isEmpty(user.getLogonPasswordVerify()));
    assertEquals("2", user.getUserId());
  }

  /**
   * Attention: This test is not intended to run with betamax. Technically spoken it succeeds automatically
   * if it detects a betamax proxy mode. Only if betamax.ignoreHosts is set to "*" the function is tested.
   * The reason is that it writes state on the server and it is not able to run concurrently.
   */
  @Test
  public void testUpdatePerson() throws Exception {
    if (!"*".equals(SystemProperties.getBetamaxIgnoreHosts()) ||
            StoreContextHelper.getWcsVersion(testConfig.getStoreContext()).lessThan(WCS_VERSION_7_7)) {
      return;
    }

    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = userContextProvider.createContext(testConfig.getUser1Name());
    UserContextHelper.setCurrentContext(userContext);

    User tester = testling.findCurrentUser();
    tester.setCity(tester.getCity().equalsIgnoreCase("Hamburg") ? "HH" : "Hamburg");

    User user = testling.updateCurrentUser(tester);
    assertNotNull(user);
//    assertNotNull(person.getLogonId());
//    assertNotNull(person.getCity().equals(city));
  }

  @Test
  public void testRegisterPerson() throws Exception {
    if (!"*".equals(SystemProperties.getBetamaxIgnoreHosts()) ||
            StoreContextHelper.getWcsVersion(testConfig.getStoreContext()).lessThan(WCS_VERSION_7_7)) {
      return;
    }

    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = userContextProvider.createContext(null);
    UserContextHelper.setCurrentContext(userContext);
    String userName = "" + System.currentTimeMillis() + "-" + UUID.randomUUID();
    String password = "passw0rd";
    String email = "mail@bla.de";
    User user = testling.registerUser(userName, password, email);
    assertNotNull(user);
    assertNotNull(user.getLogonId());
    assertNotNull(user.getLogonId().equals(UserContextHelper.getForUserName(userContext)));
  }

  /**
   * Attention: This test is not intended to run with betamax. Technically spoken it succeeds automatically
   * if it detects a betamax proxy mode. Only if betamax.ignoreHost is set to "*" the function is tested.
   * The reason is that it writes state on the server and it is not able to run concurrently.
   */
/*
  @Test
  public void testUpdatePassword() throws Exception {
    if (!"*".equals(SystemProperties.getBetamaxIgnoreHosts()) ||
            StoreContextHelper.getWcsVersion(testConfig.getStoreContext()) < StoreContextHelper.WCS_VERSION_7_7)
      return;
    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = userContextProvider.createContext("userservicetest");
    UserContextHelper.setCurrentContext(userContext);
    testling.updateCurrentUserPassword("passw0rd", "userservicetest2", "userservicetest2", userContext, testConfig.getStoreContext());
    testling.updateCurrentUserPassword("userservicetest2", "passw0rd", "passw0rd", userContext, testConfig.getStoreContext());
  }
*/

  /**
   * Attention: This test is not intended to run with betamax. Technically spoken it succeeds automatically
   * if it detects a betamax proxy mode. Only if betamax.ignoreHosts is set to "*" the function is tested.
   * The reason is that it writes state on the server and it is not able to run concurrently.
   */
/*
  @Test
  public void testResetPasswordForUser() throws Exception {
    if (!"*".equals(SystemProperties.getBetamaxIgnoreHosts())) return;
    StoreContextHelper.setCurrentContext(testConfig.getStoreContext());
    UserContext userContext = userContextProvider.createContext(null);
    UserContextHelper.setCurrentContext(userContext);
    testling.resetPassword("userservicetest", null, testConfig.getStoreContext());
  }
*/
}
