EBS Snapshotd - Manages EBS snapshots in an automated fashion.

Volumes/Snapshot associations are called *Durable Volumes*.
EBS Snapshotd associates a profile with a Durable Volume,
applying rules for:
 - when to create a new snapshot
 - when to discard existing snapshots

The above decisions are governed by the state of the system
(which is quite conservative, refusing to modify the system
if there are snapshots or volumes in error states) as well
as the following parameters for a Backup Profile:
 * The snapshot creation interval (the system will create a snapshot if the oldest snapshot is > this age.). May be set to 0 to disable automatic snapshots.
 * The minimum backup snapshots to hold
 * The snapshot expire time (snapshots older than this may be deleted, as long as deleting them would not take us below the minimum snapshot count). May be set to 0 to disable automatic snapshot discard.
 