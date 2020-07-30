
 [![Download BlockingFramework](https://img.shields.io/sourceforge/dm/officefloor.svg)](https://sourceforge.net/projects/officefloor/files/latest/download)
 [![Website](https://img.shields.io/website-up-down-green-red/http/officefloor.net.svg?label=http://officefloor.net)](http://officefloor.net)

[![Build Status](https://dev.azure.com/officefloor/OfficeFloor/_apis/build/status/officefloor.OfficeFloor?branchName=master)](https://dev.azure.com/officefloor/OfficeFloor/_build/latest?definitionId=3&branchName=master)
 [![Build Status](https://travis-ci.org/officefloor/OfficeFloor.svg?branch=master)](https://travis-ci.org/officefloor/OfficeFloor)
 [![Codacy Badge](https://api.codacy.com/project/badge/Grade/814039475f634e7183c8cca435446459)](https://www.codacy.com/app/officefloor/OfficeFloor?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=officefloor/OfficeFloor&amp;utm_campaign=Badge_Grade)
 [![codecov](https://codecov.io/gh/officefloor/OfficeFloor/branch/master/graph/badge.svg)](https://codecov.io/gh/officefloor/OfficeFloor)
 [![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=officefloor/OfficeFloor)](https://dependabot.com)

 [![Maven Central](https://img.shields.io/maven-central/v/net.officefloor/officefloor.svg)](https://search.maven.org/search?q=a:officefloor)
 [![GitHub](https://img.shields.io/github/license/officefloor/OfficeFloor.svg)](http://officefloor.net/pricing.html)
 [![License](https://img.shields.io/badge/license-Apache%202.0%20%28by%20subscription%29-blue.svg)](http://officefloor.net/pricing.html)

# OfficeFloor

OfficeFloor - inversion of coupling control

> Inversion of Control = Dependency Injection + Continuation Injection + Thread Injection

More information available at [http://officefloor.net](http://officefloor.net)


## Inversion of Coupling Control

OfficeFloor completes inversion of control by adding two new paradigms:

* **Continuation Injection**: to inject functions to orchestrate application behaviour
* **Thread Injection**: to inject/select thread (pools) to execute particular functions
* *Dependency (State) Injection*: to inject objects for state into functions (currently only paradigm implemented by "inversion of control" frameworks)
 
In doing this, OfficeFloor is capable of running different threading models (e.g. both asynchronous single threaded and synchronous multi-threaded).  In actual fact, OfficeFloor opens up mixing the threading models within the application and even introduces ability for taking advantage of thread affinity to CPUs.

This follows OfficeFloor modeling people in an office environment.  As per the paper [OfficeFloor: using office patterns to improve software design](http://doi.acm.org/10.1145/2739011.2739013) ( [free download here](http://www.officefloor.net/about.html) ), OfficeFloor follows:

* Office being an application that makes decisions on information
* Tasks within the Office as functions/methods (weaved together with *Continuation Injection*)
* Office employees/workers as threads that undertake the functions/methods (assigned via *Thread Injection*)
* Forms being the objects (manage state via *Dependency Injection*)

This allows OfficeFloor to better align to how business processes actually work:

* Workers synchronously working through tasks/functions of the processes
* Workers working asynchronously with each other

In other words, people think/behave synchronously but organise asynchronously.  Hence, both thread models are in play in modelling business processes.  Furthermore, OfficeFloor makes development of asynchronous applications easier.  This is achieved by allowing the developer to avoid asynchronous coding by having synchronous functions co-ordinated asynchronously (just like workers above).

Further to this, graphical configuration is used.  An example configuration is as follows:

![Graphical Configuration](officefloor/tutorials/TransactionHttpServer/src/site/resources/images/transaction-woof.png "OfficeFloor graphical configuration")
