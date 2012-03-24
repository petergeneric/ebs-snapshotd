EBS Snapshotd
=============
Manages EBS snapshots in an automated fashion.

Principle
---------
Volumes/Snapshot associations are called *Durable Volumes*.
EBS Snapshotd associates a profile with a Durable Volume,
applying rules for:

* when to create a new snapshot
* when to discard existing snapshots

The above decisions are governed by the state of the system
(which is quite conservative, refusing to modify the system
if there are snapshots or volumes in error states) as well
as the following parameters for a Backup Profile:

* The snapshot creation interval (the system will create a snapshot if the oldest snapshot is > this age.). May be set to 0 to disable automatic snapshots.
* The minimum backup snapshots to hold
* The snapshot expire time (snapshots older than this may be deleted, as long as deleting them would not take us below the minimum snapshot count). May be set to 0 to disable automatic snapshot discard.
 
Building
--------
This code is built with Maven (sorry - I hate maven too but it makes it easy to pull in the dependencies). To build, simply run
<code><pre>```mvn clean install```</pre></code>

To produce an eclipse project, run:
<code><pre>```mvn eclipse:clean eclipse:eclipse --downloadJavadocs=true --downloadSources=true```</pre></code>

Running
-------
Once built, extract the one-jar.jar produced (```cp daemon/target/snapshotd-*.one-jar.jar ./ebs-snapshotd.jar```)

Next, create a conf directory and run the following:
<code><pre>
cp daemon/conf/log4j.properties ./conf/log4j.properties
cp daemon/conf/example-backup.properties ./conf/backup.properties
</pre></code>

backup.properties contains a definition of the region (us-east-1 by default) and the backup profile (named "default", taking a snapshot every 1 minute, keeping snapshots for 2 minutes (with a minimum of 3 snapshots kept)).

Next, you should get your AWS access key and secret access key - I recommend generating a set of IAM credentials for this, snapshotd only needs to:

* list EBS Volumes
* list EBS Snapshots
* Create EBS Snapshots
* Set Tags on Snapshots
* Discard EBS Snapshots

Write your access key and secret access key to ```conf/aws.properties``` as:
<code><pre>
accessKey=abc
secretAccessKey=abc
</pre></code>

Finally, run with
<code><pre>
java -jar ebs-snapshotd.jar
</pre></code>

It will display any actions taken