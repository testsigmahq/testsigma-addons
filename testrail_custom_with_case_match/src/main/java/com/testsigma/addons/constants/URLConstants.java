package com.testsigma.addons.constants;

public interface URLConstants {
  String TESTRAIL_AUTH_URL = "index.php?/api/v2/get_user_by_email&email=";
  String TESTRAIL_GET_TEST_CASE_FROM_RUN_ID = "index.php?/api/v2/get_tests/";
  String TESTRAIL_GET_PROJECT_ID_FROM_RUN_ID = "index.php?/api/v2/get_run/";
  String TESTRAIL_GET_PROJECT_DETAILS_FROM_PROJECT_ID = "index.php?/api/v2/get_project/";
  String TESTRAIL_GET_RUN_DETAILS_FROM_PROJECT_ID = "index.php?/api/v2/get_runs/";
  String TESTRAIL_GET_ALL_PROJECT_DETAILS = "index.php?/api/v2/get_projects";
}
