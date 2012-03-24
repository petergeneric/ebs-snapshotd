package com.peterphi.aws.snapshotd.util;

import java.util.List;

import com.amazonaws.services.ec2.model.Tag;
import com.peterphi.aws.snapshotd.type.StandardAWSTag;

public class AWSTagHelper
{
	/**
	 * Convenience method to read a tag by its name
	 * 
	 * @param tags
	 * @return
	 */
	public String read(List<Tag> tags, StandardAWSTag name)
	{
		return read(tags, name.getTagName());
	}

	/**
	 * Convenience method to read a tag by its name
	 * 
	 * @param tags
	 * @return
	 */
	public String read(List<Tag> tags, String name)
	{
		if (tags == null)
			throw new IllegalArgumentException("Null tag list provided!");

		for (Tag tag : tags)
		{
			if (name.equals(tag.getKey()))
			{
				return tag.getValue(); // Found the tag
			}
		}

		return null;
	}

	public void write(List<Tag> tags, String name, String value)
	{
		if (tags == null)
			throw new IllegalArgumentException("Null tag list provided!");
		if (name.length() > 100)
			throw new IllegalArgumentException("Name length " + name.length() + " exceeds AWS 100 character tag name limit");
		if (value.length() > 128)
			throw new IllegalArgumentException("Value length " + value.length() + " exceeds AWS 128 character tag value limit");

		// Update the tag if it exists
		for (Tag tag : tags)
		{
			if (name.equals(tag.getKey()))
			{
				tag.setValue(value);
				return;
			}
		}

		// Otherwise, add a new tag
		tags.add(new Tag(name, value));
	}
}
