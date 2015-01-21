Performance metrics
===================

#### The configuration file

The configuration file is a .properties file that must be provided when registering a configuration class in the metrics factory (cf. [MetricFactory](https://github.com/unicesi/performance-metrics/blob/master/src/main/java/co/edu/icesi/driso/measurement/metrics/MetricFactory.java)). Its main purpose is to set a general structure for metrics, without calculation or formatting strategies.
This file must contain all of the following properties:

1. __identifier__: represents the metric's name (must be unique) 
2. __attributes__: defines general attribute names attached to a metric (e.g., computation node, component name, etc.)
3. __measureunit__: establishes the unit (after scaling measurement values) in which all measures should be presented in reports and charts
4. __stages__: defines stages. A stage can optionally be defined as shared, meaning that it can be merged from a child metric to a father metric
5. __levels__: defines levels and, mandatorily, its composing stages
6. __phases__: defines phases and, mandatorily, its composing levels or stages

###### Accepted values

| Property    | Format                                                                                                        |
| :---------- | :------------------------------------------------------------------------------------------------------------ |
| identifier  | A [Java valid identifier](http://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8) (JVI)          |
| attributes  | Comma-separated JVIs                                                                                          |
| measureunit | a string (without quotes)                                                                                     |
| stages      | Comma-separated JVIs, each optionally followed by ":shared"                                                   |
| levels      | Comma-separated JVIs, each mandatorily followed by a parenthesized expression containing comma-separated JVIs |
| phases      | Comma-separated JVIs, each mandatorily followed by a parenthesized expression containing comma-separated JVIs |

###### Example

| name        | value                                                                    |
| :---------- | :----------------------------------------------------------------------- |
| identifier  | msorting                                                                 |
| attributes  | NODE, COMPOSITE, COMPONENT                                               |
| measureunit | s                                                                        |
| stages      | Start:shared, End:shared                                                 |
| levels      | One:(Start, End), Two:(Start, End), Three:(Start, End)                   |
| phases      | Sorting:(Start, End), Merge:[One, Two, Three], Distribution:(Start, End) |
