package com.drajer.sof.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import com.drajer.ecr.it.common.BaseIntegrationTest;
import com.drajer.ecr.it.common.WireMockHelper;
import com.drajer.sof.model.LaunchDetails;
import com.drajer.sof.model.R4FhirData;
import com.drajer.test.util.TestDataGenerator;
import com.drajer.test.util.TestUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang3.time.DateUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TriggerQueryServiceTest extends BaseIntegrationTest {

  private String testCaseId;

  public TriggerQueryServiceTest(String testCaseId) {
    this.testCaseId = testCaseId;
  }

  static TestDataGenerator testDataGenerator;
  private LaunchDetails launchDetails;
  Date startDate;
  Date endDate;

  @Autowired TriggerQueryService triggerQueryService;

  WireMockHelper stubHelper;

  private static final Logger logger = LoggerFactory.getLogger(TriggerQueryServiceTest.class);

  @Before
  public void launchTestSetUp() throws IOException {
    tx = session.beginTransaction();

    try {
      launchDetails =
          mapper.readValue(
              this.getClass()
                  .getClassLoader()
                  .getResourceAsStream("R4/Misc/LaunchDetails/LaunchDetails.json"),
              LaunchDetails.class);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    launchDetails.setEhrServerURL(
        launchDetails.getEhrServerURL().replace("port", "" + wireMockHttpPort));
    launchDetails.setAccessToken(
        launchDetails.getAccessToken().replace("port", "" + wireMockHttpPort));

    startDate = DateUtils.addHours(new Date(), 3);
    endDate = DateUtils.addHours(new Date(), 30);

    session.flush();
    tx.commit();

    stubHelper = new WireMockHelper(baseUrl, wireMockHttpPort);
    logger.info("Creating wiremockstubs..");
    stubHelper.stubResources(testDataGenerator.getResourceMappings(testCaseId));
    stubHelper.stubAuthAndMetadata(testDataGenerator.getOtherMappings(testCaseId));
  }

  @After
  public void cleanUp() {
    stubHelper.stopMockServer();
  }

  @Parameters(name = "{index}: Execute TestSystemLaunch with Test Case = {0}")
  public static Collection<Object[]> data() {
    testDataGenerator = new TestDataGenerator("TriggerQueryServiceTest.yaml");
    Set<String> testCaseSet = testDataGenerator.getAllTestCases();
    Object[][] data = new Object[testCaseSet.size()][1];
    int count = 0;
    for (String testCase : testCaseSet) {
      data[count][0] = testCase;
      count++;
    }

    return Arrays.asList(data);
  }

  @Test
  public void getDataTest() throws IOException {
    R4FhirData r4FhirData =
        (R4FhirData) triggerQueryService.getData(launchDetails, startDate, endDate);
    Bundle generatedBundle = r4FhirData.getData();

    assertNotNull(generatedBundle);

    Condition generatedCondition =
        (Condition) TestUtils.getResourceFromBundle(generatedBundle, Condition.class);

    String expectedBundleAsString =
        TestUtils.getFileContentAsString("R4/Condition/ConditionBundle_d2572364249.json");
    FhirContext context = FhirContext.forR4();
    Bundle expectedBundle = TestUtils.getExpectedBundle(context, expectedBundleAsString);
    Condition expectedCondition = (Condition) expectedBundle.getEntry().get(0).getResource();

    assertEquals(generatedCondition.getCode().getText(), expectedCondition.getCode().getText());
  }
}
