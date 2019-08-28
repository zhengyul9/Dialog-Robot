# labrador_algo

[![Build Status](http://115.182.62.171:8080/buildStatus/icon?job=labrador_algo_master)](http://115.182.62.171:8080/job/labrador_algo_master/)

Algorithm module of Labrador dialog framework

Logically consists of the following sub-modules:
- NLU
- DM
- KG
- NLG
- Dialog (which glues NLU/DM/KG/NLG together)
- Local (which provides local runnable interfaces)
- Train

## Use it
In pom.xml:
```xml
<dependency>
    <groupId>ai.hual</groupId>
    <artifactId>labrador_algo</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
