/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2003, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.client.test;

import org.martus.client.core.CustomFieldSpecValidator;
import org.martus.common.FieldSpec;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.test.TestCaseEnhanced;

public class TestCustomFieldSpecValidator extends TestCaseEnhanced
{
	public TestCustomFieldSpecValidator(String name)
	{
		super(name);
	}
	
	public void testAllValid() throws Exception
	{
		FieldSpec[] specs = Bulletin.getDefaultPublicFieldSpecs();
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertTrue("not valid?", checker.isValid());
	}

	public void testNull() throws Exception
	{
		FieldSpec[] specs = null;
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
	}
	
	public void testMissingRequiredFields() throws Exception
	{
		FieldSpec[] specs = {};
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
	}

	public void testBlankTag() throws Exception
	{
		FieldSpec[] specs = Bulletin.getDefaultPublicFieldSpecs();
		specs = FieldSpec.addFieldSpec(specs, new FieldSpec(",label"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
	}

	public void testDuplicateTags() throws Exception
	{
		FieldSpec[] specs = Bulletin.getDefaultPublicFieldSpecs();
		specs = FieldSpec.addFieldSpec(specs, new FieldSpec("a"));
		specs = FieldSpec.addFieldSpec(specs, new FieldSpec("a"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
	}

	public void testMissingCustomLabel() throws Exception
	{
		FieldSpec[] specs = Bulletin.getDefaultPublicFieldSpecs();
		specs = FieldSpec.addFieldSpec(specs, new FieldSpec("a,label"));
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertTrue("not valid?", checker.isValid());

		specs = FieldSpec.addFieldSpec(specs, new FieldSpec("b"));
		CustomFieldSpecValidator checker2 = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker2.isValid());
	}

	public void testStandardFieldWithLabel() throws Exception
	{
		FieldSpec[] specs = Bulletin.getDefaultPublicFieldSpecs();
		specs[3] = new FieldSpec(specs[3].getTag() + ",illegal label");
		CustomFieldSpecValidator checker = new CustomFieldSpecValidator(specs);
		assertFalse("valid?", checker.isValid());
	}
}
