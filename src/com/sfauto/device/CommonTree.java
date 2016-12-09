package com.sfauto.device;

import java.util.ArrayList;  
import java.util.Iterator;  
import java.util.List; 

public class CommonTree<T extends CommonTreeNodeObject> {  	  
    private CommonTreeNode root = new CommonTreeNode(null, null);  
  
    public CommonTreeNode getRoot() {  
        return root;  
    }  
    
    public CommonTreeNode find(T obj){
    	return find(root,obj);
    }
    
    public void remove(T obj){
    	CommonTreeNode node = find(obj);
    	removeFromTree(node);
    }
    
    CommonTreeNode find(CommonTreeNode node,T obj){
        T t = (T)node.getData();
        
        if(t == obj){
        	return node;
        }
        
    	List<CommonTreeNode> childs = node.getChildren();  
        if (childs != null) {  
            for (CommonTreeNode n : childs) {  
            	CommonTreeNode tt = find(n,obj);  
    	        if(tt != null){
    	        	return tt;
    	        }
            }  
        }
        return null;
    }

    void removeFromTree(CommonTreeNode node){
        T t = (T)node.getData();
        
        CommonTreeNode parentNode = node.getParent();
        if(parentNode != null){
        	parentNode.deleteChild(node);
        }
        t.destroy();
        t = null;
        
    	List<CommonTreeNode> childs = node.getChildren();      	
        if (childs != null) {  
        	while(childs.size()>0){
        		CommonTreeNode n = childs.remove(0);
        		removeFromTree(n);
        	}
        }
    }
    
//    public static void main(String[] args){  
//        CommonTree<Integer> idTree = new CommonTree<Integer>();  
//        CommonTreeNode root = idTree.getRoot();  
//  
//        root.setData(1);  
//        root.addChild(2);  
//        root.addChild(3);  
//        CommonTreeNode child2 = root.getChild(2);  
//        child2.addChild(4);  
//  
//        //中序  
//        System.out.println("中序:");  
//        printTree(root);  
//  
//        //先序  
//        System.out.println("先序");  
//        printTree2(root);  
//    }  
//  
//    public static void printTree(CommonTreeNode node) {  
//        List<CommonTreeNode> childs = node.getChildren();  
//        CommonTreeNode parent = node.getParent();  
//  
//        String msg = String.format("level:%d node:%s  parent:%s childs:%d", node.getLevel(), node  
//                .getData().toString(), parent == null ? "null" : parent.getData().toString(),  
//                childs == null ? 0 : childs.size());  
//        System.out.println(msg);  
//  
//        if (childs != null) {  
//            for (CommonTreeNode n : childs) {  
//                printTree(n);  
//            }  
//        }  
//    }  
//  
//    public static void printTree2(CommonTreeNode node) {  
//        List<CommonTreeNode> childs = node.getChildren();  
//        CommonTreeNode parent = node.getParent();  
//  
//        if (parent == null) {  
//            String msg = String.format("level:%d node:%s  parent:%s childs:%d", node.getLevel(),  
//                    node.getData().toString(), parent == null ? "null" : parent.getData()  
//                            .toString(), childs == null ? 0 : childs.size());  
//            System.out.println(msg);  
//        }  
//  
//        if (childs != null) {  
//            for (CommonTreeNode n : childs) {  
//                List<CommonTreeNode> childs2 = n.getChildren();  
//                CommonTreeNode parent2 = n.getParent();  
//                String msg = String.format("level:%d node:%s  parent:%s childs:%d", n.getLevel(), n  
//                        .getData().toString(), parent2 == null ? "null" : parent2.getData()  
//                        .toString(), childs2 == null ? 0 : childs2.size());  
//                System.out.println(msg);  
//            }  
//            for (CommonTreeNode n : childs) {  
//                printTree2(n);  
//            }  
//        }  
//    }      
    
}  
