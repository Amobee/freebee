# Migration Guide

## Migrating to FreeBEE 2 from FreeBEE 1

This section covers instructions for migrating from version 1. 
FreeBEE 1 was not open source, and had different artifact group coordinates, (`group=com.videologygroup.freebee`).

FreeBEE 1 will be maintained for a short time for bug fixes and security patches, but not recieve any additional features. 
All development of new features will be done on this open source version of FreeBEE as version 2 or later.
  
For FreeBEE 2, no classes or interfaces were changed; only the artifact coordinates and packages have moved.

In FreeBEE 2, the new root package is `com.amobee.freebee` (FreeBEE 1 used `com.videologygroup.be`).
Therefore, in order to migrate from FreeBEE 1 to FreeBEE 2, update your dependency to change the `groupId` and `version`. 
For example, if using Maven: 

    <dependency>
        <groupId>com.amobee.freebee</groupId>
        <artifactId>freebee-core</artifactId>
        <version>2.x.y</version>
    </dependency>

Then, change the import location for all Boolean Expression Evaluator classes. For example:

This old evaluator usage from FreeBEE 1:

    // Old imports when using videology-java
    import BEEvaluator;
    import BEEvaluatorBuilder;
    import BEInput;
    import BENode;

When moving to FreeBEE 2, the above becomes:
    
    // New imports when using freebee 2
    import com.amobee.freebee.evaluator.evaluator.BEEvaluator;
    import com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder;
    import com.amobee.freebee.evaluator.evaluator.BEInput;
    import com.amobee.freebee.expression.BENode;
    
Performing a find and replace in your project makes this an easy change.

Lastly, to prevent accidental usage of FreeBEE 1, 
one could add a checkstyle rule that prevents importing the old package location. 
For example:

    <module name="IllegalImport">
      <property name="illegalPkgs" value="com.videologygroup.be"/>
    </module>

## Migrating to FreeBEE 2 from videology-java

This section covers instructions for users of the library migrating from the previous `videology-java` version.

The boolean expression evaluator was first implemented as part of a larger, non-public library called `videology-java`.
The last release of `videology-java` to **update** the Boolean Expression evaluator was `videology-java-1.41.1`. 
Since that release, the Boolean Expression evaluator in `videology-java` is considered deprecated, and this new stand-alone version should be used.
  
FreeBEE 2 did not change any interfaces. Only the artifact coordinates and the package containing the evaluator moved. 
The package change is intentional so that FreeBEE useres could continue to use `videology-java` without getting duplicate class exceptions at runtime.

In FreeBEE 2, the new root package is `com.amobee.freebee` (`videology-java` used `com.videologygroup.common.be`).
Therefore, in order to migrate from `videology-java`, add a Maven dependency for `freebee-core` and then 
change the import location for all existing Boolean Expression Evaluator classes. For example:

This old evaluator usage from `videology-java`:

    // Old imports when using videology-java
    import com.videologygroup.common.be.evaluator.evaluator.BEEvaluator;
    import com.videologygroup.common.be.evaluator.evaluator.BEEvaluatorBuilder;
    import com.videologygroup.common.be.evaluator.evaluator.BEInput;
    import com.videologygroup.common.be.expression.BENode;

When moving to `freebee-core`, the above becomes:
    
    // New imports when using freebee 2
    import com.amobee.freebee.evaluator.evaluator.BEEvaluator;
    import com.amobee.freebee.evaluator.evaluator.BEEvaluatorBuilder;
    import com.amobee.freebee.evaluator.evaluator.BEInput;
    import com.amobee.freebee.expression.BENode;
    
Performing a find and replace in your project makes this an easy change.

Lastly, to prevent accidental usage of the old, `videology-java` boolean expression evaluator, 
one could add a checkstyle rule that prevents importing the old package location. 
For example:

    <module name="IllegalImport">
      <property name="illegalPkgs" value="com.videologygroup.common.be"/>
    </module>

