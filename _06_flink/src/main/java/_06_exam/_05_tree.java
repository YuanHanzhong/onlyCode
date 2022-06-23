package _06_exam;

import org.w3c.dom.Node;

public class _05_tree {
    /**
     * //      5
     * //    /   \
     * //   3     7
     * //  / \
     * // 1   4
     */
    public static void main(String[] args) {
        Node node = new Node(5);
        node.left = new Node(3);
        node.right=new Node(7);
        node.left.left = new Node(1);
        node.left.right = new Node(4);
    
        //preOrder
        System.out.println("preOrder:\n");
        preOrder(node);
        
        System.out.println("inOrder\n");
        inOrder(node);
        
        System.out.println("postorder:\n");
        postOrder(node);
    }
    
    public static void preOrder(Node node) {
        if (node != null) {
            System.out.println(node.value);
            preOrder(node.left);
            preOrder(node.right);
    
        }
    }
    
    public static void inOrder(Node node) {
        if (node != null) {
            inOrder(node.left);
            System.out.println(node.value);
            inOrder(node.right);
        }
    }
    
    public static void postOrder(Node node) {
        if (node != null) {
            postOrder(node.left);
            postOrder(node.right);
            System.out.println(node.value);
        }
    }
    public static class Node{
        int value;
        Node left;
        Node right;
    
        public Node(int value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }
}
