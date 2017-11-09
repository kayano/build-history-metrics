package com.getbase.jenkins.plugins.metrics.history.influxdb.generators;

import org.influxdb.dto.Point;

public interface PointGenerator {
    Point[] generate();
}
