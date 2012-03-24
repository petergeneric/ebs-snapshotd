package com.github.petergeneric.aws.elasticsnap.daemon.type;

/**
 * Describes the AWS Tags in use by this application
 */
public enum StandardAWSTag
{
	/**
	 * Identifies the BackupProfile for a Volume (and/or Snapshot)
	 */
	BACKUP_PROFILE("snapshotd:profile");

	private final String tagName;

	private StandardAWSTag(String name)
	{
		this.tagName = name;
	}

	public String getTagName()
	{
		return this.tagName;
	}
}
