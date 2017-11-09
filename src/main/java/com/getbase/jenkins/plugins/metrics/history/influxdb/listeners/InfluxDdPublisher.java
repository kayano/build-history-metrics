package com.getbase.jenkins.plugins.metrics.history.influxdb.listeners;

import com.getbase.jenkins.plugins.metrics.history.influxdb.InfluxDbConfig;
import com.getbase.jenkins.plugins.metrics.history.influxdb.generators.JenkinsBasePointGenerator;
import com.getbase.jenkins.plugins.metrics.history.influxdb.generators.PointGenerator;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;
import org.apache.commons.lang.StringUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Send Jenkins build information to InfluxDB
 */
@Extension
public class InfluxDdPublisher extends RunListener<Run<?, ?>> {
    private static final Logger logger = Logger.getLogger(InfluxDdPublisher.class.getName());

    @Extension
    public static final InfluxDbConfig dbConfig = new InfluxDbConfig();

    @Override
    public  void onCompleted(Run<?, ?> build, TaskListener listener){

        List<Point> pointsToWrite = new ArrayList<>();

        JenkinsBasePointGenerator jGen = new JenkinsBasePointGenerator(build);
        addPoints(pointsToWrite, jGen, listener);

        InfluxDB influxDB = StringUtils.isBlank(dbConfig.getUsername()) ? InfluxDBFactory.connect(dbConfig.getUrl()) :
                InfluxDBFactory.connect(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());
        writeToInflux(influxDB, pointsToWrite);
        listener.getLogger().println("[InfluxDB Plugin] Completed.");
    }

    private void addPoints(List<Point> pointsToWrite, PointGenerator generator, TaskListener listener) {
        try {
            pointsToWrite.addAll(Arrays.asList(generator.generate()));
        } catch (Exception e) {
            listener.getLogger().println("[InfluxDB Plugin] Failed to collect data. Ignoring Exception:" + e);
        }
    }

    private void writeToInflux(InfluxDB influxDB, List<Point> pointsToWrite) {
        /**
         * build batchpoints for a single write.
         */
        try {
            BatchPoints batchPoints = BatchPoints
                    .database(dbConfig.getDatabase())
                    .points(pointsToWrite.toArray(new Point[0]))
                    .retentionPolicy(dbConfig.getRetentionPolicy())
                    .consistency(InfluxDB.ConsistencyLevel.ANY)
                    .build();
            influxDB.write(batchPoints);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not report to InfluxDB. Ignoring Exception.", e);
        }
    }
}
