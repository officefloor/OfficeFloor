 [![Download OfficeFloor](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/officefloor/files/latest/download)

 [![Download BlockingFramework](https://img.shields.io/sourceforge/dm/officefloor.svg)](https://sourceforge.net/projects/officefloor/files/latest/download)
 [![Website](https://img.shields.io/website-up-down-green-red/http/officefloor.net.svg?label=http://officefloor.net)](http://officefloor.net)

[![Waffle.io - Columns and their card count](https://badge.waffle.io/sagenschneider/OfficeFloor.svg?columns=all)](https://waffle.io/sagenschneider/OfficeFloor)

 [![Build Status](https://travis-ci.org/sagenschneider/OfficeFloor.svg?branch=master)](https://travis-ci.org/sagenschneider/OfficeFloor)
 [![Codacy Badge](https://api.codacy.com/project/badge/Grade/48a33c29fe5c423fbba190010994925f)](https://www.codacy.com/app/daniel_77/OfficeFloor?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sagenschneider/OfficeFloor&amp;utm_campaign=Badge_Grade)
 [![codecov](https://codecov.io/gh/sagenschneider/OfficeFloor/branch/master/graph/badge.svg)](https://codecov.io/gh/sagenschneider/OfficeFloor)
 [![Dependabot Status](https://api.dependabot.com/badges/status?host=github&repo=sagenschneider/OfficeFloor)](https://dependabot.com)

 [![Maven Central](https://img.shields.io/maven-central/v/net.officefloor/officefloor.svg)](https://search.maven.org/search?q=a:officefloor)
 [![GitHub](https://img.shields.io/github/license/sagenschneider/OfficeFloor.svg)](http://officefloor.net/pricing.html)
 [![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](http://officefloor.net/pricing.html)


# OfficeFloor

OfficeFloor - true inversion of control framework

> Inversion of Control = Dependency Injection + Continuation Injection + Thread Injection

More information available at [http://officefloor.net](http://officefloor.net)


[![Download OfficeFloor](https://a.fsdn.com/con/app/sf-download-button)](https://sourceforge.net/projects/officefloor/files/latest/download)


# Inversion of Control

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

