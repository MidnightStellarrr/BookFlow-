import java.util.*;

public class AVLTree<T extends Comparable<T>> {
    private class Node {
        T data;
        Node left, right;
        int height;
        
        Node(T data) {
            this.data = data;
            this.height = 1;
        }
    }
    
    private Node root;
    private int size = 0;
    
    public void insert(T data) {
        root = insertRec(root, data);
    }
    
    private Node insertRec(Node node, T data) {
        if (node == null) {
            size++;
            return new Node(data);
        }
        
        int cmp = data.compareTo(node.data);
        if (cmp < 0)
            node.left = insertRec(node.left, data);
        else if (cmp > 0)
            node.right = insertRec(node.right, data);
        else
            return node; // Duplicate
        
        updateHeight(node);
        return balance(node);
    }
    
    public void delete(T data) {
        root = deleteRec(root, data);
    }
    
    private Node deleteRec(Node node, T data) {
        if (node == null) return null;
        
        int cmp = data.compareTo(node.data);
        if (cmp < 0)
            node.left = deleteRec(node.left, data);
        else if (cmp > 0)
            node.right = deleteRec(node.right, data);
        else {
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            
            Node minNode = findMinNode(node.right);
            node.data = minNode.data;
            node.right = deleteRec(node.right, minNode.data);
        }
        
        updateHeight(node);
        return balance(node);
    }
    
    public T findMin() {
        Node minNode = findMinNode(root);
        return minNode != null ? minNode.data : null;
    }
    
    private Node findMinNode(Node node) {
        while (node != null && node.left != null)
            node = node.left;
        return node;
    }
    
    public List<T> inorder() {
        List<T> list = new ArrayList<>();
        inorderRec(root, list);
        return list;
    }
    
    private void inorderRec(Node node, List<T> list) {
        if (node != null) {
            inorderRec(node.left, list);
            list.add(node.data);
            inorderRec(node.right, list);
        }
    }
    
    public List<T> rangeSearch(T start, T end) {
        List<T> result = new ArrayList<>();
        rangeSearchRec(root, start, end, result);
        return result;
    }
    
    private void rangeSearchRec(Node node, T start, T end, List<T> result) {
        if (node == null) return;
        
        if (node.data.compareTo(start) >= 0 && node.data.compareTo(end) <= 0)
            result.add(node.data);
        
        if (node.data.compareTo(start) > 0)
            rangeSearchRec(node.left, start, end, result);
        if (node.data.compareTo(end) < 0)
            rangeSearchRec(node.right, start, end, result);
    }
    
    public boolean checkConflict(T event, int duration) {
        // Simple conflict check based on time overlap
        // Returns true if conflict exists
        List<T> all = inorder();
        for (T e : all) {
            if (e instanceof Event) {
                Event existing = (Event) e;
                Event newEvent = (Event) event;
                if (existing.getDate().equals(newEvent.getDate())) {
                    // Check time overlap
                    if (!(newEvent.getEndTime().compareTo(existing.getTime()) <= 0 ||
                            newEvent.getTime().compareTo(existing.getEndTime()) >= 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public int size() { return size; }
    public int getHeight() { return root != null ? root.height : 0; }
    public boolean isEmpty() { return root == null; }
    
    private void updateHeight(Node node) {
        node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));
    }
    
    private int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }
    
    private int getBalance(Node node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }
    
    private Node balance(Node node) {
        int balance = getBalance(node);
        
        if (balance > 1) {
            if (getBalance(node.left) < 0)
                node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        
        if (balance < -1) {
            if (getBalance(node.right) > 0)
                node.right = rotateRight(node.right);
            return rotateLeft(node);
        }
        
        return node;
    }
    
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;
        
        x.right = y;
        y.left = T2;
        
        updateHeight(y);
        updateHeight(x);
        
        return x;
    }
    
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;
        
        y.left = x;
        x.right = T2;
        
        updateHeight(x);
        updateHeight(y);
        
        return y;
    }
}