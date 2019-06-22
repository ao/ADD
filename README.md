# Autonomous Distributed Database (ADD)

![ADD Dashboard](artwork/dash1.png?raw=true "ADD Dashboard")

This software application proves the potential for an autonomous distributed database system.

ADD runs on any machine through the JVM and requires zero configuration or management to setup or maintain.

Simply start ADD on any number of machines on a controlled network where each machine is a member of the same subnet. Each instance will automatically connect to each other and create a distributed database. 

Data will be replicated across the network and when a new node joins, it will automatically be given the existing databases and tables layout along with all replication information.

If one of the instances dies, the other nodes will check back and wait for a short recovery before reallocating the database pieces that were on that node to other nodes across the network.  

## How do I interact with it?
Once ADD is running, you simply connect to `http://<localhost_or_node_ip>:1985/dashboard` to get going.

## Requirements
This project was built on IntelliJ IDEA under JDK 11 runtime.

## Is there a JAR available?
Yes, take a look at the [release page](https://github.com/ao/ADD/releases)

Currently version 0.0.1 is the only version, so [grab it here](https://github.com/ao/ADD/releases/download/0.0.1/ADD_0.0.1.zip)

Unzip it and then simply run `java -jar ADD.jar`

## Build it yourself?
Yes, of course you can!

`git clone https://github.com/ao/ADD.git`

`Open in IntelliJ IDEA.`

`Edit configurations..`

`+ Application`

Set the `classpath` to `ADD` and the `Main class` to `gl.ao.add.ADD`

`Run the application!`

## Problems?
[Create an issue](https://github.com/ao/ADD/issues/new) if you need help