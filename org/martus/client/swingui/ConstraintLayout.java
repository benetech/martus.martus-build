/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2002, Beneficent
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



/* used with the permission of Jerry Huxtable */
package org.martus.client.swingui;


import java.awt.*;
import java.util.*;

/**
 * A base class for layouts which simplifies the business of building new
 * layouts with constraints.
 */
public class ConstraintLayout implements LayoutManager2 {

	protected final static int PREFERRED = 0;
	protected final static int MINIMUM = 1;
	protected final static int MAXIMUM = 2;

	protected int hMargin = 0;
	protected int vMargin = 0;
	private Hashtable constraints;
	protected boolean includeInvisible = false;

	public void addLayoutComponent(String constraint, Component c) {
		setConstraint(c, constraint);
	}

	public void addLayoutComponent(Component c, Object constraint) {
		setConstraint(c, constraint);
	}

	public void removeLayoutComponent(Component c) {
		if (constraints != null)
			constraints.remove(c);
	}

	public void setConstraint(Component c, Object constraint) {
		if (constraint != null) {
			if (constraints == null)
				constraints = new Hashtable();
			constraints.put(c, constraint);
		} else if (constraints != null)
			constraints.remove(c);
	}

	public Object getConstraint(Component c) {
		if (constraints != null)
			return constraints.get(c);
		return null;
	}

	public void setIncludeInvisible(boolean includeInvisible) {
		this.includeInvisible = includeInvisible;
	}

	public boolean getIncludeInvisible() {
		return includeInvisible;
	}

	protected boolean includeComponent(Component c) {
		return includeInvisible || c.isVisible();
	}

	public Dimension minimumLayoutSize (Container target) {
		return calcLayoutSize(target, MINIMUM);
	}

	public Dimension maximumLayoutSize (Container target) {
		return calcLayoutSize(target, MAXIMUM);
	}

	public Dimension preferredLayoutSize (Container target) {
		return calcLayoutSize(target, PREFERRED);
	}

	public Dimension calcLayoutSize (Container target, int type) {
		Dimension dim = new Dimension(0, 0);
		measureLayout(target, dim, type);
		Insets insets = target.getInsets();
		dim.width += insets.left + insets.right + 2*hMargin;
		dim.height += insets.top + insets.bottom + 2*vMargin;
		return dim;
	}

	public void invalidateLayout(Container target) {
	}

	public float getLayoutAlignmentX(Container parent) {
		return 0.5f;
	}

	public float getLayoutAlignmentY(Container parent) {
		return 0.5f;
	}

	public void layoutContainer(Container target)  {
		measureLayout(target, null, PREFERRED);
	}

	public void measureLayout(Container target, Dimension dimension, int type)  {
	}

	protected static Dimension getComponentSize(Component c, int type) {
		if (type == MINIMUM)
			return c.getMinimumSize();
		if (type == MAXIMUM)
			return c.getMaximumSize();
		return c.getPreferredSize();
	}
}
