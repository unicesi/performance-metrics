Performance-Metrics
===================

#### The configuration file

The configuration file is a .properties file that must be provided when registering a configuration class in the metrics factory (cf. [MetricFactory](https://github.com/unicesi/performance-metrics/blob/master/src/main/java/co/edu/icesi/driso/measurement/metrics/MetricFactory.java)). Its main purpose is to set a general structure for metrics, without calculation or formatting methods.
This file must contain all of the following properties:

1. __identifier__: the metric's name (must be unique) 
2. __attributes__: general attributes attached to a metric (e.g., computation node, component name, etc.)
3. __measureunit__: 
