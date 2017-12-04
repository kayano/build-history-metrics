package com.getbase.jenkins.plugins.metrics.history.influxdb.generators;

import com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerJobAction;
import com.synopsys.arc.jenkins.plugins.ownership.jobs.JobOwnerJobProperty;
import hudson.model.Result;
import hudson.model.Run;
import jenkins.metrics.impl.TimeInQueueAction;
import org.influxdb.dto.Point;

public class JenkinsBasePointGenerator implements PointGenerator {
    public static final String MEASUREMENT_NAME = "jenkins_build_data";
    public static final String JOB_NAME = "job_name";
    public static final String BUILD_NUMBER = "build_number";
    public static final String BUILD_RESULT = "build_result";
    public static final String BUILD_TIMESTAMP = "build_timestamp";
    public static final String BUILD_DURATION = "build_duration";
    public static final String QUEUING_DURATION = "queuing_duration";
    public static final String TOTAL_DURATION = "total_duration";
    public static final String BUILD_STATUS_MESSAGE = "build_status_message";
    public static final String JOB_OWNER = "job_owner";
    public static final String JOB_SCORE = "job_score";

    private final Run<?, ?> build;

    public JenkinsBasePointGenerator(Run<?, ?> build) {
        this.build = build;
    }

    public Point[] generate() {
        // Build is not finished when running with pipelines. Duration must be calculated manually
        long startTime = build.getTimeInMillis();
        long currTime = System.currentTimeMillis();
        long dt = currTime - startTime;
        long duration = build.getDuration() == 0 ? dt : build.getDuration();

        TimeInQueueAction action = build.getAction(TimeInQueueAction.class);
        String owner = build.getParent().getAction(JobOwnerJobAction.class).getOwnership().getPrimaryOwnerEmail();
        int score = build.getParent().getBuildHealth().getScore();
        final Result result = build.getResult();
        final String resultStr = result != null ? result.toString() : "UNKNOWN";

        Point.Builder point = Point
                .measurement(MEASUREMENT_NAME)
                .addField(JOB_NAME, build.getParent().getFullName())
                .tag(JOB_NAME, build.getParent().getFullName())
                .addField(BUILD_NUMBER, build.getNumber())
                .addField(BUILD_RESULT, resultStr)
                .tag(BUILD_RESULT, resultStr)
                .addField(JOB_OWNER, owner)
                .tag(JOB_OWNER, owner)
                .addField(JOB_SCORE, score)
                .tag(JOB_SCORE, Integer.toString(score))
                .addField(BUILD_TIMESTAMP, build.getTimeInMillis())
                .addField(BUILD_DURATION, duration)
                .addField(QUEUING_DURATION, action.getQueuingDurationMillis())
                .addField(TOTAL_DURATION, duration + action.getQueuingDurationMillis())
                .addField(BUILD_STATUS_MESSAGE, build.getBuildStatusSummary().message);

        return new Point[] {point.build()};
    }


}
