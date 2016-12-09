package com.sfauto.device;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CommonTreeNode<T extends Object> {
	private T data;
	private List<CommonTreeNode> childs;
	private CommonTreeNode parent;
	private int level;

	public CommonTreeNode(T data, CommonTreeNode parent) {
		this.data = data;
		this.parent = parent;
		if (parent == null) {
			level = 0;
		} else {
			level = parent.level + 1;
		}
	}

	public String toString(){
		if(data != null){
			return data.toString();
		}else{
			return "nothing";
		}
	}
	
	public List<CommonTreeNode> getChildren() {
		return childs;
	}

	public CommonTreeNode addChild(T data) {
		if (childs == null) {
			childs = new ArrayList<CommonTreeNode>();
		}
		CommonTreeNode node = new CommonTreeNode(data, this);
		childs.add(node);
		return node;
	}

	public void deleteChild(CommonTreeNode node) {
		if (childs != null) {
			Iterator<CommonTreeNode> it = childs.iterator();
			while (it.hasNext()) {
				CommonTreeNode child = it.next();
				if (child == node) {
					childs.remove(child);
					break;
				}
			}
		}
	}

	public CommonTreeNode getChild(T data) {
		if (childs != null) {
			Iterator<CommonTreeNode> it = childs.iterator();
			while (it.hasNext()) {
				CommonTreeNode child = it.next();
				if (child.data.equals(data)) {
					return child;
				}
			}
		}
		return null;
	}

	public CommonTreeNode getParent() {
		return parent;
	}

	public void setData(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public int getLevel() {
		return level;
	}
}
