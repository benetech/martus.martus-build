package org.martus.client;

public class SearchTreeNode
{
	public final static int VALUE = 0;
	public final static int OR = 1;
	public final static int AND = 2;

	public SearchTreeNode(String value)
	{
		nodeOp = VALUE;
		nodeValue = value.trim();
	}

	public void convertToOr(String left, String right)
	{
		nodeOp = OR;
		nodeValue = null;
		createChildNodes(left, right);
	}

	public void convertToAnd(String left, String right)
	{
		nodeOp = AND;
		nodeValue = null;
		createChildNodes(left, right);
	}

	public String getValue()
	{
		return nodeValue;
	}

	public int getOperation()
	{
		return nodeOp;
	}

	public SearchTreeNode getLeft()
	{
		return nodeLeft;
	}

	public SearchTreeNode getRight()
	{
		return nodeRight;
	}

	private boolean isValueNode()
	{
		return (getOperation() == VALUE);
	}

	private void createChildNodes(String left, String right)
	{
		nodeLeft = new SearchTreeNode(left);
		nodeRight = new SearchTreeNode(right);
	}

	private String nodeValue;
	private int nodeOp;
	private SearchTreeNode nodeLeft;
	private SearchTreeNode nodeRight;
}
