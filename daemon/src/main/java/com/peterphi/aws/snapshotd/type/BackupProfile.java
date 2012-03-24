package com.peterphi.aws.snapshotd.type;

/**
 * Describes a backup profile
 */
public class BackupProfile
{
	/**
	 * A unique name that identifies this profile separately from all others
	 */
	protected final String name;

	/**
	 * Frequency (in milliseconds) with which backups are created<br />
	 * If this value is 0 then backups are not automatically created
	 */
	protected final long frequency;

	/**
	 * Period (in milliseconds) after which backups can be automatically expired<br />
	 * If this value is 0 then backups are not automatically expired<br />
	 * This value must be == 0 || > <code>frequency</code>
	 */
	protected final long expire;

	/**
	 * The minimum number of backups to keep<br />
	 * If the number of backups is less than this number then backups will not be automatically expired
	 */
	protected final int minimum;

	public BackupProfile(String name, long frequency, long expire, int minimum)
	{
		this.name = name;
		this.frequency = frequency;
		this.expire = expire;
		this.minimum = minimum;
	}

	public String getName()
	{
		return name;
	}

	public long getFrequency()
	{
		return frequency;
	}

	public long getExpire()
	{
		return expire;
	}

	public int getMinimum()
	{
		return minimum;
	}

	@Override
	public String toString()
	{
		return "BackupProfile [name=" + name + "]";
	}
}
