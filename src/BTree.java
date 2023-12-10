import javax.management.RuntimeErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BTree <K extends Comparable<K> , V> implements IBTree<K,V>{
    private IBTreeNode<K,V> root;
    private int minimumDegree;
    private int maximumDegree;

    public BTree(int minimumDegree){
        if(minimumDegree <  2){
            throw new RuntimeErrorException(new Error());
        }
        root = null;
        this.minimumDegree = minimumDegree;
        this.maximumDegree = 2 * minimumDegree - 1;
    }

    @Override
    public IBTreeNode<K, V> getRoot() {
        return root;
    }

    @Override
    public int getMinimumDegree() {
        return minimumDegree;
    }

    @Override
    public V search(K key) {
        if(key == null){
            //System.out.println("null key");
            throw new RuntimeErrorException(new Error());
        }
        if(this.root == null){
            //System.out.println("null root");
            return null;
        }
        BTreeNode<K,V> temp = (BTreeNode<K, V>) this.root;
        //Traverse the tree until we get the leaf to search in it
        //If we find the required key while traversing we return it
        //Else we search for it in the leaves
        while(!temp.isLeaf()){
            int index=0;
            while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
                index++;
            }
            if(index < temp.getNumOfKeys() && (temp.getKeys().get(index)).compareTo(key) == 0){
                return temp.getValues().get(index);
            }
            temp = (BTreeNode<K, V>) temp.getChildren().get(index);
        }
        int index=0;
        while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
            index++;
        }
        if(index < temp.getNumOfKeys() && (temp.getKeys().get(index)).compareTo(key) == 0){
            return temp.getValues().get(index);
        }
        return null;
    }
    public void print(BTreeNode<K, V> temp){
        if(root == null){
            System.out.println("tree is empty");
            return;
        }
        for (int i = 0; i < temp.getNumOfKeys(); i++) {
            System.out.print(temp.getKeys().get(i) + " ");
        }
        if (!temp.isLeaf()) {
            for (int i = 0; i < temp.getNumOfKeys() + 1; i++) {
                System.out.println(" ");
                print((BTreeNode<K, V>) temp.getChildren().get(i));
            }
        }
    }
    public void show(){
        print((BTreeNode<K, V>) this.root);
    }

    public void updateInserted(K key ,V value){
        BTreeNode<K,V> temp = (BTreeNode<K, V>) root;
        while(!temp.isLeaf()){
            int index=0;
            while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
                index++;
            }
            if(index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) == 0){
               temp.getValues().set(index,value);
            }
            temp = (BTreeNode<K, V>) temp.getChildren().get(index);
        }
        int index=0;
        while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
            index++;
        }
        if(index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) == 0){
             temp.getValues().set(index,value);
        }
    }
    @Override
    public boolean delete(K key) {
        if(key == null){
            throw new RuntimeErrorException(new Error());
        }

        if(this.root == null){
            return false;
        }

        return deleteNode(this.root,key);
    }

    private boolean deleteNode(IBTreeNode<K,V> temp ,K key) {
        List<K> keys = temp.getKeys();
        int i = 0;
        while (!temp.isLeaf()) {
            while (i < keys.size() && key.compareTo(keys.get(i)) > 0) {
                i++;
            }
            if (i < keys.size() && key.compareTo(keys.get(i)) == 0) {
                break;
            } else {
                temp = temp.getChildren().get(i);
                keys = temp.getKeys();
                i = 0;
            }
        }
        if (temp.isLeaf()) {
            i = 0;
            while (i < keys.size() && key.compareTo(keys.get(i)) > 0) {
                i++;
            }
            if (!(i < keys.size() && key.compareTo(keys.get(i)) == 0)) {
                return false;
            }
        }
        // case one :: delete key from leaf node
        if (temp.isLeaf() && keys.size() >= this.minimumDegree) {
            keys.remove(i);
            temp.getValues().remove(i);
            temp.setNumOfKeys(keys.size());
        } else if (temp.isLeaf() && keys.size() <= this.minimumDegree - 1) {
            BTreeNode<K, V> curparent = (BTreeNode<K, V>) ((BTreeNode<K, V>) temp).getParent();
            // remove from root
            if (curparent == null) {
                keys.remove(i);
                temp.getValues().remove(i);
                temp.setNumOfKeys(keys.size());
                if (keys.size() == 0) {
                    this.root = null;
                }
            } else {
                List<IBTreeNode<K, V>> chlidlist = curparent.getChildren();
                int index = ((BTreeNode<K, V>) temp).getIndex();
                if (index - 1 >= 0 && chlidlist.get(index - 1).getKeys().size() >= minimumDegree) {
                    int replace = chlidlist.get(index - 1).getKeys().size();
                    K replacingKey = chlidlist.get(index - 1).getKeys().remove(replace - 1);
                    V replacingVal = chlidlist.get(index - 1).getValues().remove(replace - 1);
                    chlidlist.get(index - 1).setNumOfKeys(replace - 1);
                    K parentKey = curparent.getKeys().get(index - 1);
                    V parentVal = curparent.getValues().get(index - 1);
                    temp.getKeys().remove(i);
                    temp.getKeys().add(0, parentKey);
                    temp.getValues().remove(i);
                    temp.getValues().add(0, parentVal);
                    curparent.getKeys().set(index - 1, replacingKey);
                    curparent.getValues().set(index - 1, replacingVal);
                } else if (index + 1 < chlidlist.size() && chlidlist.get(index + 1).getKeys().size() >= minimumDegree) {
                    int replace = chlidlist.get(index + 1).getKeys().size();
                    K replacingKey = chlidlist.get(index + 1).getKeys().remove(0);
                    V replacingVal = chlidlist.get(index + 1).getValues().remove(0);
                    chlidlist.get(index + 1).setNumOfKeys(replace - 1);
                    K parentKey = curparent.getKeys().get(index);
                    V parentVal = curparent.getValues().get(index);
                    temp.getKeys().remove(i);
                    temp.getKeys().add(parentKey);
                    temp.getValues().remove(i);
                    temp.getValues().add(parentVal);
                    curparent.getKeys().set(index, replacingKey);
                    curparent.getValues().set(index, replacingVal);
                } else {
                    temp.getKeys().remove(i);
                    temp.getValues().remove(i);
                    temp.setNumOfKeys(temp.getKeys().size());
                    if (curparent.getKeys().size() >= minimumDegree || curparent.getParent() == null) {
                        merge(index, curparent);
                    } else {
                        merge(index, curparent);
                        fixBalance((BTreeNode<K, V>) curparent);
                    }
                }
            }
        } else if (!temp.isLeaf()) {
            IBTreeNode<K, V> predecessor = getPredessor((BTreeNode<K, V>) temp, i);
            if (predecessor.getKeys().size() >= minimumDegree) {
                K predecessorKey = predecessor.getKeys().get(predecessor.getKeys().size() - 1);
                V predecessorValue = predecessor.getValues().get(predecessor.getValues().size() - 1);
                deleteNode(predecessor, predecessorKey);
                temp.getKeys().set(i, predecessorKey);
                temp.getValues().set(i, predecessorValue);
            } else {
                IBTreeNode<K, V> successor = null;
                boolean successorFound = false;
                try {
                    successor = getSuccessor((BTreeNode<K, V>) temp, i + 1);
                    successorFound = true;
                } catch (IndexOutOfBoundsException e) {
                    successorFound = false;
                }
                if (successor.getKeys().size() >= minimumDegree && successorFound) {
                    K successorKey = successor.getKeys().get(0);
                    V successorValue = successor.getValues().get(0);
                    deleteNode(successor, successorKey);
                    temp.getKeys().set(i, successorKey);
                    temp.getValues().set(i, successorValue);
                }
                else {
                    K predecessorKey = predecessor.getKeys().get(predecessor.getKeys().size() - 1);
                    V predecessorValue = predecessor.getValues().get(predecessor.getValues().size() - 1);
                    temp.getKeys().set(i, predecessorKey);
                    temp.getValues().set(i, predecessorValue);
                    deleteNode(predecessor, predecessorKey);
                }
            }
        }
        return true;
    }
    private BTreeNode<K, V> getPredessor(BTreeNode<K, V> temp, int index) {
        BTreeNode<K, V> z = (BTreeNode<K, V>) temp.getChildren().get(index);
        while (!z.isLeaf()) {
            z = (BTreeNode<K, V>) z.getChildren().get(z.getChildren().size() - 1);
        }
        return z;
    }
    private BTreeNode<K, V> getSuccessor(BTreeNode<K, V> temp, int index) {
        BTreeNode<K, V> z = (BTreeNode<K, V>) temp.getChildren().get(index);
        while (!z.isLeaf()) {
            z = (BTreeNode<K, V>) z.getChildren().get(0);
        }
        return z;
    }

    public boolean merge(int index ,IBTreeNode<K, V> parent){
        if(parent.getKeys().size() >= minimumDegree || ((BTreeNode<K,V>)parent).getParent() == null){
            List<IBTreeNode<K,V>> chlidlist = parent.getChildren();
            if(index - 1 >= 0 && chlidlist.get(index-1).getKeys().size() <= minimumDegree-1){
                K parentKey = parent.getKeys().remove(index-1);
                V parentVal = parent.getValues().remove(index-1);
                parent.setNumOfKeys(parent.getKeys().size());
                List<K> prevSibKeys = chlidlist.get(index-1).getKeys();
                List<K> currKeys = chlidlist.get(index).getKeys();
                List<K> keys = new ArrayList<>();
                keys.addAll(prevSibKeys);
                keys.add(parentKey);
                keys.addAll(currKeys);
                List<V> prevSibVals = chlidlist.get(index-1).getValues();
                List<V> currVals = chlidlist.get(index).getValues();
                List<V> values = new ArrayList<>();
                values.addAll(prevSibVals);
                values.add(parentVal);
                values.addAll(currVals);
                chlidlist.get(index).setKeys(keys);
                chlidlist.get(index).setValues(values);
                chlidlist.get(index).setNumOfKeys(keys.size());
                List<IBTreeNode<K, V>> child = new ArrayList<>();
                child.addAll(chlidlist.get(index-1).getChildren());
                child.addAll(chlidlist.get(index).getChildren());
                chlidlist.get(index).setChildren(child);
                chlidlist.remove(index-1);
                if (parent == this.root) {
                    if (this.root.getKeys().size() == 0) {
                        this.root = parent.getChildren().get(0);
                        ((BTreeNode<K, V>) this.root).setParent(null);
                        if (parent.getChildren().get(0).isLeaf()) {
                            this.root.setLeaf(true);
                        }
                    }
                }
                return true;
            }
            else if(index + 1 < chlidlist.size() && chlidlist.get(index+1).getKeys().size() <= minimumDegree-1){
                K parentKey = parent.getKeys().remove(index);
                V parentVal = parent.getValues().remove(index);
                parent.setNumOfKeys(parent.getKeys().size());
                List<K> prevSibKeys = chlidlist.get(index).getKeys();
                List<K> currKeys = chlidlist.get(index+1).getKeys();
                List<K> keys = new ArrayList<>();
                keys.addAll(prevSibKeys);
                keys.add(parentKey);
                keys.addAll(currKeys);
                List<V> prevSibVals = chlidlist.get(index).getValues();
                List<V> currVals = chlidlist.get(index+1).getValues();
                List<V> values = new ArrayList<>();
                values.addAll(prevSibVals);
                values.add(parentVal);
                values.addAll(currVals);
                chlidlist.get(index).setKeys(keys);
                chlidlist.get(index).setValues(values);
                chlidlist.get(index).setNumOfKeys(keys.size());
                List<IBTreeNode<K, V>> child = new ArrayList<>();
                child.addAll(chlidlist.get(index).getChildren());
                child.addAll(chlidlist.get(index+1).getChildren());
                chlidlist.get(index).setChildren(child);
                chlidlist.remove(index+1);
                if (parent == this.root) {
                    if (this.root.getKeys().size() == 0) {
                        this.root = parent.getChildren().get(0);
                        ((BTreeNode<K, V>) this.root).setParent(null);
                        if (parent.getChildren().get(0).isLeaf()) {
                            this.root.setLeaf(true);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void insert(K key, V value) {
        if(key == null || value == null){
            throw new RuntimeErrorException(new Error());
        }
        if(root == null){
            root = new BTreeNode<K,V>();
            List<K> keys = new ArrayList<K>();
            List<V> values = new ArrayList<V>();
            keys.add(key);
            values.add(value);
            root.setKeys(keys);
            root.setNumOfKeys(keys.size());
            root.setValues(values);
            return;
        }
        else{
            BTreeNode<K,V> temp = (BTreeNode<K, V>) root;
            while(!temp.isLeaf()){
                int index=0;
                while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
                    index++;
                }
                if(index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) == 0){
                    return;
                }
                temp = (BTreeNode<K, V>) temp.getChildren().get(index);
            }
            int index=0;
            while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
                index++;
            }
            if(index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) == 0){
                return;
            }
            insertNode(temp,key,value);
        }
    }
    private void insertNode(BTreeNode<K, V> temp, K key, V value){
        if(temp == null || temp.getNumOfKeys() < this.maximumDegree){
            int index=0;
            while (index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) > 0){
                index++;
            }
            if(index < temp.getNumOfKeys() && key.compareTo(temp.getKeys().get(index)) == 0){
                return;
            }
            temp.getKeys().add(index,key);
            temp.getValues().add(index,value);
            temp.setNumOfKeys(temp.getNumOfKeys()+1);
            return;
        }
        else {
            int split = (temp.getNumOfKeys() / 2 );
            List<K> rightSubtreeKeys = new ArrayList<K>(temp.getKeys().subList(split + 1,temp.getNumOfKeys()));
            List<K> leftSubtreeKeys = new ArrayList<K>(temp.getKeys().subList(0,split));
            List<V> rightSubtreeVals = new ArrayList<V>(temp.getValues().subList(split + 1,temp.getNumOfKeys()));
            List<V> leftSubtreeVals = new ArrayList<V>(temp.getValues().subList(0,split));
            K splitKey = temp.getKeys().get(split);
            V splitVal = temp.getValues().get(split);
            if(!temp.isLeaf()){
                if(temp.getParent() == null){
                    this.root = new BTreeNode<K,V>();
                    this.root.setKeys(new ArrayList<K>());
                    this.root.getKeys().add(splitKey);
                    this.root.getValues().add(splitVal);
                    BTreeNode<K, V> right = new BTreeNode<K,V>();
                    right.setLeaf(false);
                    right.setNumOfKeys(rightSubtreeKeys.size());
                    right.setKeys(rightSubtreeKeys);
                    right.setValues(rightSubtreeVals);
                    right.setParent(this.root);
                    BTreeNode<K, V> left = new BTreeNode<K,V>();
                    left.setNumOfKeys(leftSubtreeKeys.size());
                    left.setLeaf(false);
                    left.setKeys(leftSubtreeKeys);
                    left.setValues(leftSubtreeVals);
                    left.setParent(this.root);
                    this.root.getChildren().add(0,left);
                    this.root.getChildren().add(1,right);
                    this.root.setLeaf(false);
                    this.root.setNumOfKeys(1);
                    if(key.compareTo(temp.getKeys().get(split)) < 0){
                        List<IBTreeNode<K, V>> leftChild = new ArrayList<>(temp.getChildren().subList(0, split + 2));
                        List<IBTreeNode<K, V>> rightChild = new ArrayList<>(temp.getChildren().subList(split + 2, temp.getChildren().size()));
                        int index = 0;
                        while(index < left.getNumOfKeys() &&  key.compareTo(left.getKeys().get(index)) > 0){
                            index++;
                        }
                        left.setNumOfKeys(left.getNumOfKeys() + 1);
                        left.getKeys().add(index,key);
                        left.getValues().add(index,value);
                        right.setChildren(rightChild);
                        left.setChildren(leftChild);
                    }
                    else {
                        List<IBTreeNode<K, V>> leftChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(0, split + 1));
                        List<IBTreeNode<K, V>> rightChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(split + 1, temp.getChildren().size()));
                        int index = 0;
                        while(index < right.getNumOfKeys() &&  key.compareTo(right.getKeys().get(index)) > 0){
                            index++;
                        }
                        right.setNumOfKeys(right.getNumOfKeys() + 1);
                        right.getKeys().add(index,key);
                        right.getValues().add(index,value);
                        right.setChildren(rightChild);
                        left.setChildren(leftChild);
                    }
                }
                else{
                    int indx = temp.getIndex();
                    BTreeNode<K,V> parent = (BTreeNode<K, V>) temp.getParent();
                    parent.getChildren().remove(indx);
                    BTreeNode<K, V> right = new BTreeNode<K,V>();
                    right.setLeaf(false);
                    right.setNumOfKeys(rightSubtreeKeys.size());
                    right.setKeys(rightSubtreeKeys);
                    right.setValues(rightSubtreeVals);
                    right.setParent(parent);
                    BTreeNode<K, V> left = new BTreeNode<K,V>();
                    left.setNumOfKeys(leftSubtreeKeys.size());
                    left.setLeaf(false);
                    left.setKeys(leftSubtreeKeys);
                    left.setValues(leftSubtreeVals);
                    left.setParent(parent);
                    parent.getChildren().add(indx,left);
                    parent.getChildren().add(indx+1,right);
                    if(key.compareTo(temp.getKeys().get(split)) > 0){
                        List<IBTreeNode<K, V>> leftChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(0, split + 1));
                        List<IBTreeNode<K, V>> rightChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(split + 1, temp.getChildren().size()));
                        int index = 0;
                        while(index < right.getNumOfKeys() &&  key.compareTo(right.getKeys().get(index)) > 0){
                            index++;
                        }
                        right.setNumOfKeys(right.getNumOfKeys() + 1);
                        right.getKeys().add(index,key);
                        right.getValues().add(index,value);
                        right.setChildren(rightChild);
                        left.setChildren(leftChild);
                    }
                    else {
                        List<IBTreeNode<K, V>> leftChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(0, split + 2));
                        List<IBTreeNode<K, V>> rightChild = new ArrayList<IBTreeNode<K, V>>(temp.getChildren().subList(split + 2, temp.getChildren().size()));
                        int index = 0;
                        while(index < left.getNumOfKeys() &&  key.compareTo(left.getKeys().get(index)) > 0){
                            index++;
                        }
                        left.setNumOfKeys(left.getNumOfKeys() + 1);
                        left.getKeys().add(index,key);
                        left.getValues().add(index,value);
                        right.setChildren(rightChild);
                        left.setChildren(leftChild);
                    }
                    insertNode(parent,splitKey,splitVal);
                }
            }
            else {
                if(temp.getParent() == null){
                    this.root = new BTreeNode<K,V>();
                    this.root.setKeys(new ArrayList<K>());
                    this.root.getKeys().add(splitKey);
                    this.root.getValues().add(splitVal);
                    BTreeNode<K, V> right = new BTreeNode<K,V>();
                    right.setLeaf(true);
                    right.setNumOfKeys(rightSubtreeKeys.size());
                    right.setKeys(rightSubtreeKeys);
                    right.setValues(rightSubtreeVals);
                    right.setParent(this.root);
                    BTreeNode<K, V> left = new BTreeNode<K,V>();
                    left.setNumOfKeys(leftSubtreeKeys.size());
                    left.setLeaf(true);
                    left.setKeys(leftSubtreeKeys);
                    left.setValues(leftSubtreeVals);
                    left.setParent(this.root);
                    this.root.getChildren().add(0,left);
                    this.root.getChildren().add(1,right);
                    this.root.setLeaf(false);
                    this.root.setNumOfKeys(1);
                    if(key.compareTo(temp.getKeys().get(split)) > 0){
                        int index = 0;
                        while(index < right.getNumOfKeys() &&  key.compareTo(right.getKeys().get(index)) > 0){
                            index++;
                        }
                        if (index < right.getNumOfKeys() && right.getKeys().get(index).compareTo(key) == 0) {
                            return;
                        }
                        right.setNumOfKeys(right.getNumOfKeys() + 1);
                        right.getKeys().add(index,key);
                        right.getValues().add(index,value);
                    }
                    else {
                        int index = 0;
                        while(index < left.getNumOfKeys() &&  key.compareTo(left.getKeys().get(index)) > 0){
                            index++;
                        }
                        if (index < right.getNumOfKeys() && right.getKeys().get(index).compareTo(key) == 0) {
                            return;
                        }
                        left.setNumOfKeys(left.getNumOfKeys() + 1);
                        left.getKeys().add(index,key);
                        left.getValues().add(index,value);
                    }
                }
                else {
                    int indx = temp.getIndex();
                    BTreeNode<K,V> parent = (BTreeNode<K, V>) temp.getParent();
                    parent.getChildren().remove(indx);
                    BTreeNode<K, V> right = new BTreeNode<K,V>();
                    right.setLeaf(true);
                    right.setNumOfKeys(rightSubtreeKeys.size());
                    right.setKeys(rightSubtreeKeys);
                    right.setValues(rightSubtreeVals);
                    right.setParent(parent);
                    BTreeNode<K, V> left = new BTreeNode<K,V>();
                    left.setNumOfKeys(leftSubtreeKeys.size());
                    left.setLeaf(true);
                    left.setKeys(leftSubtreeKeys);
                    left.setValues(leftSubtreeVals);
                    left.setParent(parent);
                    parent.getChildren().add(indx,left);
                    parent.getChildren().add(indx+1,right);
                    if(key.compareTo(temp.getKeys().get(split)) > 0){
                        int index = 0;
                        while(index < right.getNumOfKeys() &&  key.compareTo(right.getKeys().get(index)) > 0){
                            index++;
                        }
                        if (index < right.getNumOfKeys() && right.getKeys().get(index).compareTo(key) == 0) {
                            return;
                        }
                        right.setNumOfKeys(right.getNumOfKeys() + 1);
                        right.getKeys().add(index,key);
                        right.getValues().add(index,value);
                    }
                    else {
                        int index = 0;
                        while(index < left.getNumOfKeys() &&  key.compareTo(left.getKeys().get(index)) > 0){
                            index++;
                        }
                        if (index < right.getNumOfKeys() && right.getKeys().get(index).compareTo(key) == 0) {
                            return;
                        }
                        left.setNumOfKeys(left.getNumOfKeys() + 1);
                        left.getKeys().add(index,key);
                        left.getValues().add(index,value);
                    }
                    insertNode(parent,splitKey,splitVal);
                }
            }
        }
    }
    private boolean rebalance(int index ,IBTreeNode<K, V> parent){
        List<IBTreeNode<K,V>> chlidlist = parent.getChildren();
        if(index - 1 >= 0 && chlidlist.get(index-1).getKeys().size() >= minimumDegree){
            int replace = chlidlist.get(index-1).getKeys().size();
            K replacingKey = chlidlist.get(index-1).getKeys().remove(replace-1);
            V replacingVal = chlidlist.get(index-1).getValues().remove(replace-1);
            chlidlist.get(index-1).setNumOfKeys(chlidlist.get(index-1).getKeys().size());
            IBTreeNode<K, V> borrowedChild = chlidlist.get(index - 1).getChildren().remove(replace);
            K parentKey = parent.getKeys().get(index-1);
            V parentVal = parent.getValues().get(index-1);
            parent.getKeys().set(index-1,replacingKey);
            parent.getValues().set(index-1,replacingVal);
            chlidlist.get(index).getKeys().add(0,parentKey);
            chlidlist.get(index).getValues().add(0,parentVal);
            chlidlist.get(index).setNumOfKeys(chlidlist.get(index).getKeys().size());
            List<IBTreeNode<K,V>> newChild = chlidlist.get(index).getChildren();
            newChild.add(0,borrowedChild);
            chlidlist.get(index).setChildren(newChild);
            return true;
        }
        else if(index+1 < chlidlist.size() && chlidlist.get(index+1).getKeys().size() >= minimumDegree){
            K replacingKey = chlidlist.get(index+1).getKeys().remove(0);
            V replacingVal = chlidlist.get(index+1).getValues().remove(0);
            chlidlist.get(index+1).setNumOfKeys(chlidlist.get(index+1).getKeys().size());
            IBTreeNode<K, V> borrowedChild = chlidlist.get(index + 1).getChildren().remove(0);
            K parentKey = parent.getKeys().get(index);
            V parentVal = parent.getValues().get(index);
            parent.getKeys().set(index,replacingKey);
            parent.getValues().set(index,replacingVal);
            chlidlist.get(index).getKeys().add(parentKey);
            chlidlist.get(index).getValues().add(parentVal);
            chlidlist.get(index).setNumOfKeys(chlidlist.get(index).getKeys().size());
            List<IBTreeNode<K,V>> newChild = chlidlist.get(index).getChildren();
            newChild.add(borrowedChild);
            chlidlist.get(index).setChildren(newChild);
            return true;
        }
        return false;
    }
    private void fixBalance(BTreeNode<K,V> parent){
        Stack<BTreeNode<K, V>> stack = new Stack<>();
        stack.push(parent);
        BTreeNode<K,V> node;
        while (!stack.isEmpty()) {
            node = stack.peek();
            if (node.getKeys().size() <= minimumDegree - 1) {
                if (!rebalance(node.getIndex(),node.getParent())) {
                    if (!merge(node.getIndex(),node.getParent())) {
                        if (((BTreeNode<K, V>) node.getParent()).getParent() == null) {
                            break;
                        } else {
                            stack.push(((BTreeNode<K, V>) node.getParent()));
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        stack.pop();
        while (!stack.isEmpty()) {
            node = stack.pop();
            merge(node.getIndex(),node.getParent());
        }
    }

}