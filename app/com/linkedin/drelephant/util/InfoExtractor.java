/*
 * Copyright 2016 LinkedIn Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.linkedin.drelephant.util;

import com.linkedin.drelephant.analysis.HadoopApplicationData;
import com.linkedin.drelephant.spark.data.SparkApplicationData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import models.AppResult;

import com.linkedin.drelephant.mapreduce.data.MapReduceApplicationData;


/**
 * InfoExtractor is responsible for retrieving information and context about a
 * job from the job's configuration which will be leveraged by the UI
 */
public class InfoExtractor {
  private static final Logger logger = Logger.getLogger(InfoExtractor.class);
  private static final String SPARK_EXTRA_JAVA_OPTIONS = "spark.driver.extraJavaOptions";

  private static final String AZKABAN_WORKFLOW_URL = "azkaban.link.workflow.url";
  private static final String AZKABAN_JOB_URL = "azkaban.link.job.url";
  private static final String AZKABAN_EXECUTION_URL = "azkaban.link.execution.url";
  private static final String AZKABAN_ATTEMPT_URL = "azkaban.link.attempt.url";
  private static final String AZKABAN_JOB_NAME = "azkaban.job.id";

  // TODO: this utils class is not ideal, probably should merge retrieve URLs logics directly into the data interface?
  public static void retrieveURLs(AppResult result, HadoopApplicationData data) {
    if (data instanceof MapReduceApplicationData) {
      retrieveURLs(result, (MapReduceApplicationData) data);
    } else if (data instanceof SparkApplicationData) {
      retrieveURLs(result, (SparkApplicationData) data);
    }
  }

  public static void retrieveURLs(AppResult result, MapReduceApplicationData appData) {
    Properties jobConf = appData.getConf();
    String jobId = appData.getJobId();

    result.jobExecUrl = jobConf.getProperty(AZKABAN_ATTEMPT_URL) != null ?
        Utils.truncateField(jobConf.getProperty(AZKABAN_ATTEMPT_URL), AppResult.URL_LEN_LIMIT, jobId) : "";
    // For jobs launched by Azkaban, we consider different attempts to be different jobs
    result.jobDefUrl = jobConf.getProperty(AZKABAN_JOB_URL) != null ?
        Utils.truncateField(jobConf.getProperty(AZKABAN_JOB_URL), AppResult.URL_LEN_LIMIT, jobId) : "";
    result.flowExecUrl = jobConf.getProperty(AZKABAN_EXECUTION_URL) != null ?
        Utils.truncateField(jobConf.getProperty(AZKABAN_EXECUTION_URL), AppResult.URL_LEN_LIMIT, jobId) : "";
    result.flowDefUrl = jobConf.getProperty(AZKABAN_WORKFLOW_URL) != null ?
        Utils.truncateField(jobConf.getProperty(AZKABAN_WORKFLOW_URL), AppResult.URL_LEN_LIMIT, jobId) : "";

    // For Azkaban, The url and ids are the same
    result.jobDefId = result.jobDefUrl;
    result.jobExecId = result.jobExecUrl;
    result.flowDefId = result.flowDefUrl;
    result.flowExecId = Utils.truncateField(result.flowExecUrl, AppResult.FLOW_EXEC_ID_LIMIT, jobId);

    if (!result.jobExecId.isEmpty()) {
      result.scheduler = Utils.truncateField("azkaban", AppResult.SCHEDULER_LIMIT, jobId);
      result.workflowDepth = 0;
    }
    result.jobName = jobConf.getProperty(AZKABAN_JOB_NAME) != null ?
        Utils.truncateField(jobConf.getProperty(AZKABAN_JOB_NAME), AppResult.JOB_NAME_LIMIT, jobId) : "";
  }

  public static void retrieveURLs(AppResult result, SparkApplicationData appData) {
    String prop = appData.getEnvironmentData().getSparkProperty(SPARK_EXTRA_JAVA_OPTIONS);
    String appId = appData.getAppId();

    if (prop != null) {
      try {
        Map<String, String> options = Utils.parseJavaOptions(prop);

        List<String> s = new ArrayList<String>();
        for (Map.Entry<String, String> e : options.entrySet()) {
          s.add(e.getKey() + "->" + e.getValue());
        }
        logger.info("Parsed options:" + StringUtils.join(s, ","));

        result.jobExecUrl = options.get(AZKABAN_ATTEMPT_URL) != null ?
            Utils.truncateField(unescapeString(options.get(AZKABAN_ATTEMPT_URL)), AppResult.URL_LEN_LIMIT, appId) : "";
        result.jobDefUrl = options.get(AZKABAN_JOB_URL) != null ?
            Utils.truncateField(unescapeString(options.get(AZKABAN_JOB_URL)), AppResult.URL_LEN_LIMIT, appId) : "";
        result.flowExecUrl = options.get(AZKABAN_EXECUTION_URL) != null ?
            Utils.truncateField(unescapeString(options.get(AZKABAN_EXECUTION_URL)), AppResult.URL_LEN_LIMIT, appId) : "";
        result.flowDefUrl = options.get(AZKABAN_WORKFLOW_URL) != null ?
            Utils.truncateField(unescapeString(options.get(AZKABAN_WORKFLOW_URL)), AppResult.URL_LEN_LIMIT, appId) : "";

        result.jobDefId = result.jobDefUrl;
        result.jobExecId = result.jobExecUrl;
        result.flowDefId = result.flowDefUrl;
        result.flowExecId = Utils.truncateField(result.flowExecUrl, AppResult.FLOW_EXEC_ID_LIMIT, appId);

        if (!result.jobExecId.isEmpty()) {
          result.scheduler = "azkaban";
          result.workflowDepth = 0;
        }

        result.jobName = options.get(AZKABAN_JOB_NAME) != null ?
            Utils.truncateField(unescapeString(options.get(AZKABAN_JOB_NAME)), AppResult.JOB_NAME_LIMIT, appId) : "";

      } catch (IllegalArgumentException e) {
        logger.error("Encountered error while parsing java options into urls: " + e.getMessage());
      }
    } else {
      logger.error("Unable to retrieve azkaban properties from application [" +
          appData.getGeneralData().getApplicationId() + "] it does not contain [" + SPARK_EXTRA_JAVA_OPTIONS
          + "] property in its spark properties.");

      result.scheduler = null;
      result.workflowDepth = 0;
      result.jobExecId = "";
      result.jobDefId = "";
      result.flowExecId = "";
      result.flowDefId = "";
      result.jobExecUrl = "";
      result.jobDefUrl = "";
      result.flowExecUrl = "";
      result.flowDefUrl = "";
      result.jobName = "";
    }
  }

  /**
   * A temporary solution that SPARK 1.2 need to escape '&' with '\&' in its javaOptions.
   * This is the reverse process that recovers the escaped string.
   *
   * @param s The string to unescape
   * @return The original string
   */
  private static String unescapeString(String s) {
    if (s == null) {
      return null;
    }
    return s.replaceAll("\\\\\\&", "\\&");
  }
}
