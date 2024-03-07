package com.zzh;


import sun.net.www.content.text.plain;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {

    /**
     * 节点数组
     */
    transient Node<K, V>[] table;
    /**
     * table存储的元素数量
     */
    transient int size;


    /**
     * 负载因子
     */
    final float loadFactor;
    /**
     * table扩容阈值
     */
    int threshold;


    /**
     * table数组大小超过64才会转红黑树
     * 需要两个条件同时为true
     */
    static final int MIN_TREEIFY_CAPACITY = 64;
    /**
     * 单个链表的节点数超过8才会转红黑树
     * 需要两个条件同时为true
     */
    static final int TREEIFY_THRESHOLD = 8;
    /**
     * 红黑树退化为链表的节点数阈值
     */
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * 抛出ConcurrentModificationException的条件变量
     */
    transient int modCount;


    public HashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
    }

    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public HashMap(int initialCapacity, float loadFactor) {
        // 参数校验略
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * 将cap的值修正为大于等于cap的最近一个2的整数幂
     * 例如: tableSizeFor(12) = 16
     * 0b开头表示二进制数
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1; // cap - 1 = 11 = n = 0b1011
        n |= n >>> 1; // (0b1011 |= 0b101) = 15 = n = 0b1111
        n |= n >>> 2; // (0b1111 |= 0b11) = 15 = n = 0b1111
        n |= n >>> 4; // (0b1111 |= 0b0) = 15 = n = 0b1111
        n |= n >>> 8; // (0b1111 |= 0b0) = 15 = n = 0b1111
        n |= n >>> 16; // (0b1111 |= 0b0) = 15 = n = 0b1111
        // n+1 = 16
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }


    public V put(K key, V value) {
        int hash = hash(key);
        return putVal(hash, key, value, false);
    }

    static final int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        // hashcode高16位和低16位进行异或操作, 以最大程度的散列
        return h ^ (h >>> 16);
    }

    final V putVal(int hash, K key, V value, boolean onlyIfAbsent) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, i;

        if ((tab = table) == null || (n = tab.length) == 0) {
            n = (tab = resize()).length; // table为空, resize初始化
        }
        i = (n - 1) & hash; // 计算出要插入的位置, 思考为什么不是hash%n
        if ((p = tab[i]) == null) { // 当前i位置还未插入过节点, 则创建新节点
            tab[i] = newNode(hash, key, value, null);
        } else {
            // 当前i位置已存在节点
            Node<K, V> e;
            K k;
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                e = p; // key相同时
            } else if (p instanceof TreeNode) { // 链表已转为红黑树, 执行树中的putVal
                e = ((TreeNode<K, V>) p).putTreeVal(this, tab, hash, key, value);
            } else {
                for (int binCount = 0; ; ++binCount) { // 遍历链表
                    if ((e = p.next) == null) { // e为null, 则p是链表尾节点
                        // 将当前节点插入到p之后
                        p.next = newNode(hash, key, value, null);
                        if (binCount >= TREEIFY_THRESHOLD - 1) {
                            treeifyBin(tab, hash); // 链表树化
                        }
                        break;
                    }
                    // 一次循环中, 找到了与当前节点相同的key
                    if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) break;
                    p = e;
                }
            }
            if (e != null) { // key相同的旧节点
                V oldValue = e.value;
                // 是否需要覆盖数据
                if (!onlyIfAbsent || oldValue == null) e.value = value;
                return oldValue;
            }
        }
        ++modCount;
        if (++size > threshold) resize(); // 是否需要扩容
        return null;
    }

    final Node<K, V>[] resize() {
        Node<K, V>[] oldTab = table;
        int oldCap = (oldTab == null) ? 0 : oldTab.length;
        int oldThr = threshold;
        int newCap, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY) {
                // 容量已达最大值, 无法扩容了, 放开阈值限制, 可以继续在旧table添加元素
                threshold = Integer.MAX_VALUE;
                return oldTab; // 提前返回旧table
            } else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // 两倍容量扩容
        } else if (oldThr > 0) newCap = oldThr;
        else { // 初始化逻辑
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int) (DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }

        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;
        Node<K, V>[] newTab = (Node<K, V>[]) new Node[newCap];
        table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) { //遍历旧table
                Node<K, V> e;
                if ((e = oldTab[j]) != null) {
                    oldTab[j] = null;
                    if (e.next == null) {
                        // 链表只有一个节点, 直接放入新table
                        newTab[e.hash & (newCap - 1)] = e;
                    } else if (e instanceof TreeNode) {
                        ((TreeNode<K, V>) e).split(this, newTab, j, oldCap);
                    } else { // preserve order
                        // 扩容后的位置还是j
                        Node<K, V> loHead = null, loTail = null;
                        // 扩容后的位置为j+oldCap
                        Node<K, V> hiHead = null, hiTail = null;
                        Node<K, V> next;
                        do {
                            next = e.next;
                            // 根据e.hash的最高位是否为0 把原链表拆分为两段
                            if ((e.hash & oldCap) == 0) { // 节点的hash小于oldCap, 链到lo中
                                if (loTail == null) loHead = e;
                                else loTail.next = e;
                                loTail = e;
                            } else { // 节点的hash大于oldCap, 链到hi中
                                if (hiTail == null) hiHead = e;
                                else hiTail.next = e;
                                hiTail = e;
                            }
                        } while ((e = next) != null);

                        // 分别将lo链表和hi链表保存到新table中,
                        if (loTail != null) {
                            loTail.next = null;
                            newTab[j] = loHead;
                        }
                        if (hiTail != null) {
                            hiTail.next = null;
                            newTab[j + oldCap] = hiHead;
                        }
                    }
                }
            }
        }
        return newTab;
    }

    public V remove(Object key) {
        int hash = hash(key);
        Node<K, V> e = removeNode(hash, key, null, false, true);
        return e == null ? null : e.value;
    }

    final Node<K, V> removeNode(int hash, Object key, Object value, boolean matchValue, boolean movable) {
        Node<K, V>[] tab;
        Node<K, V> p;
        int n, index;
        if ((tab = table) != null && (n = tab.length) > 0 && (p = tab[index = (n - 1) & hash]) != null) {
            // 散列表不为空 && table[i]处节点p不为空
            Node<K, V> node = null, e;
            K k;
            V v;

            // 查找节点
            if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))) {
                // 链表头恰好是要删除的节点
                node = p;
            } else if ((e = p.next) != null) {
                if (p instanceof TreeNode) {
                    // 链表已树化
                    node = ((TreeNode<K, V>) p).getTreeNode(hash, key);
                } else {
                    do { // 遍历链表, 寻找要删除的节点
                        if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k)))) {
                            node = e; // 找到节点
                            break;
                        }
                        p = e; // p代表pre
                    } while ((e = e.next) != null); // 转到next
                }
            }

            // 删除节点
            if (node != null && (!matchValue || (v = node.value) == value || (value != null && value.equals(v)))) {
                if (node instanceof TreeNode) { // 树的删除
                    ((TreeNode<K, V>) node).removeTreeNode(this, tab, movable);
                } else if (node == p) { // node是之前链表的头节点
                    tab[index] = node.next;
                } else {
                    // 删除node的引用
                    p.next = node.next;
                }
                ++modCount;
                --size;
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    final void treeifyBin(Node<K, V>[] tab, int hash) {
        int n, index;
        Node<K, V> e;
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            // 若table数组容量小于64, 则还是走扩容,先不执行树化过程, 此举也能将节点较多的链表拆散
            resize();
        else if ((e = tab[index = (n - 1) & hash]) != null) {
            TreeNode<K, V> hd = null, tl = null;
            do {
                // 循环的作用是将链表复制一份出来
                // 新链表的node类型虽然是tree,但暂时还是以链表形式组织在一起
                TreeNode<K, V> p = replacementTreeNode(e, null);
                if (tl == null) hd = p;
                else {
                    p.prev = tl;
                    tl.next = p;
                }
                tl = p;
            } while ((e = e.next) != null);

            // 将新链表头放入tab[index]中
            if ((tab[index] = hd) != null)
                // 新链表树化
                hd.treeify(tab);
        }
    }

    TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<>(hash, key, value, next);
    }


    static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value;
        }

        public final String toString() {
            return key + "=" + value;
        }

        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this) return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                if (Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue())) return true;
            }
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    static final class TreeNode<K, V> extends LinkedHashMap.Entry<K, V> {
        TreeNode<K, V> parent;  // red-black tree links
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;    // needed to unlink next upon deletion
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        /**
         * Returns root of tree containing this node.
         */
        final TreeNode<K, V> root() {
            for (TreeNode<K, V> r = this, p; ; ) {
                if ((p = r.parent) == null) return r;
                r = p;
            }
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = (n - 1) & root.hash;
                TreeNode<K, V> first = (TreeNode<K, V>) tab[index];
                if (root != first) {
                    Node<K, V> rn;
                    tab[index] = root;
                    TreeNode<K, V> rp = root.prev;
                    if ((rn = root.next) != null) ((TreeNode<K, V>) rn).prev = rp;
                    if (rp != null) rp.next = rn;
                    if (first != null) first.prev = root;
                    root.next = first;
                    root.prev = null;
                }
                assert checkInvariants(root);
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key.
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         */
        final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
            TreeNode<K, V> p = this;
            do {
                int ph, dir;
                K pk;
                TreeNode<K, V> pl = p.left, pr = p.right, q;
                if ((ph = p.hash) > h) p = pl;
                else if (ph < h) p = pr;
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p;
                else if (pl == null) p = pr;
                else if (pr == null) p = pl;
                else if ((kc != null || (kc = comparableClassFor(k)) != null) && (dir = compareComparables(kc, k, pk)) != 0)
                    p = (dir < 0) ? pl : pr;
                else if ((q = pr.find(h, k, kc)) != null) return q;
                else p = pl;
            } while (p != null);
            return null;
        }


        final TreeNode<K, V> getTreeNode(int h, Object k) {
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1);
            return d;
        }

        final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null; //红黑树的root节点
            // this即为要树化的链表, 循环遍历链表, 将每个链表节点插入到root树中
            for (TreeNode<K, V> x = this, next; x != null; x = next) {
                // 备份下一节点
                next = (TreeNode<K, V>) x.next;
                x.left = x.right = null;
                if (root == null) { // 根节点颜色必为黑, 红黑树性质
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else { // 非根节点
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K, V> p = root; ; ) { // 遍历红黑树, 找到可插入的位置
                        // 判断当前节点和要插入的节点的大小序,类似Comparable功能的逻辑
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h) dir = -1;
                        else if (ph < h) dir = 1;
                        else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0)
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K, V> xp = p; // 备份p
                        // 二叉树性质: left < p < right
                        // 根据dir结果, 判断是插入到p的左边还是右边, 且插入的位置要是null
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            // 节点插入
                            x.parent = xp;
                            if (dir <= 0) xp.left = x;
                            else xp.right = x;
                            // 保持平衡
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 将树根节点放到tab[i]中,原来存放的是链表的head
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */
        final Node<K, V> untreeify(HashMap<K, V> map) {
            Node<K, V> hd = null, tl = null;
            for (Node<K, V> q = this; q != null; q = q.next) {
                Node<K, V> p = map.replacementNode(q, null);
                if (tl == null) hd = p;
                else tl.next = p;
                tl = p;
            }
            return hd;
        }

        /**
         * Tree version of putVal.
         */
        final TreeNode<K, V> putTreeVal(HashMap<K, V> map, Node<K, V>[] tab, int h, K k, V v) {
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K, V> root = (parent != null) ? root() : this;
            for (TreeNode<K, V> p = root; ; ) {
                int dir, ph;
                K pk;
                if ((ph = p.hash) > h) dir = -1;
                else if (ph < h) dir = 1;
                else if ((pk = p.key) == k || (k != null && k.equals(pk))) return p;
                else if ((kc == null && (kc = comparableClassFor(k)) == null) || (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K, V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null && (q = ch.find(h, k, kc)) != null) || ((ch = p.right) != null && (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }

                TreeNode<K, V> xp = p;
                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    Node<K, V> xpn = xp.next;
                    TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0) xp.left = x;
                    else xp.right = x;
                    xp.next = x;
                    x.parent = x.prev = xp;
                    if (xpn != null) ((TreeNode<K, V>) xpn).prev = x;
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call.
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal. So instead we swap the tree
         * linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(HashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
            int n;
            if (tab == null || (n = tab.length) == 0) return;
            int index = (n - 1) & hash;
            TreeNode<K, V> first = (TreeNode<K, V>) tab[index], root = first, rl;
            TreeNode<K, V> succ = (TreeNode<K, V>) next, pred = prev;
            if (pred == null) tab[index] = first = succ;
            else pred.next = succ;
            if (succ != null) succ.prev = pred;
            if (first == null) return;
            if (root.parent != null) root = root.root();
            if (root == null || (movable && (root.right == null || (rl = root.left) == null || rl.left == null))) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }
            TreeNode<K, V> p = this, pl = left, pr = right, replacement;
            if (pl != null && pr != null) {
                TreeNode<K, V> s = pr, sl;
                while ((sl = s.left) != null) // find successor
                    s = sl;
                boolean c = s.red;
                s.red = p.red;
                p.red = c; // swap colors
                TreeNode<K, V> sr = s.right;
                TreeNode<K, V> pp = p.parent;
                if (s == pr) { // p was s's direct parent
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<K, V> sp = s.parent;
                    if ((p.parent = sp) != null) {
                        if (s == sp.left) sp.left = p;
                        else sp.right = p;
                    }
                    if ((s.right = pr) != null) pr.parent = s;
                }
                p.left = null;
                if ((p.right = sr) != null) sr.parent = p;
                if ((s.left = pl) != null) pl.parent = s;
                if ((s.parent = pp) == null) root = s;
                else if (p == pp.left) pp.left = s;
                else pp.right = s;
                if (sr != null) replacement = sr;
                else replacement = p;
            } else if (pl != null) replacement = pl;
            else if (pr != null) replacement = pr;
            else replacement = p;
            if (replacement != p) {
                TreeNode<K, V> pp = replacement.parent = p.parent;
                if (pp == null) (root = replacement).red = false;
                else if (p == pp.left) pp.left = replacement;
                else pp.right = replacement;
                p.left = p.right = p.parent = null;
            }

            TreeNode<K, V> r = p.red ? root : balanceDeletion(root, replacement);

            if (replacement == p) {  // detach
                TreeNode<K, V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left) pp.left = null;
                    else if (p == pp.right) pp.right = null;
                }
            }
            if (movable) moveRootToFront(tab, r);
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map   the map
         * @param tab   the table for recording bin heads
         * @param index the index of the table being split
         * @param bit   the bit of hash to split on
         */
        final void split(HashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode<K, V> b = this;
            // Relink into lo and hi lists, preserving order
            TreeNode<K, V> loHead = null, loTail = null;
            TreeNode<K, V> hiHead = null, hiTail = null;
            int lc = 0, hc = 0;
            for (TreeNode<K, V> e = b, next; e != null; e = next) {
                next = (TreeNode<K, V>) e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    if ((e.prev = loTail) == null) loHead = e;
                    else loTail.next = e;
                    loTail = e;
                    ++lc;
                } else {
                    if ((e.prev = hiTail) == null) hiHead = e;
                    else hiTail.next = e;
                    hiTail = e;
                    ++hc;
                }
            }

            if (loHead != null) {
                if (lc <= UNTREEIFY_THRESHOLD) tab[index] = loHead.untreeify(map);
                else {
                    tab[index] = loHead;
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD) tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null) hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null) rl.parent = p;
                if ((pp = r.parent = p.parent) == null) (root = r).red = false;
                else if (pp.left == p) pp.left = r;
                else pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null) lr.parent = p;
                if ((pp = l.parent = p.parent) == null) (root = l).red = false;
                else if (pp.right == p) pp.right = l;
                else pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
            // 定义
            // 0. 新节点默认是红色
            // 1. 根节点为黑色
            // 2. 不存在的叶子节点都视为黑色
            // 3. 红节点的父/子都必为黑
            // 4. 节点到所有叶子中的黑节点数量一致

            // 调整策略, 当父节点为红色时(双红缺陷) 策略的调整就是为了保持定义
            // 1. 叔为红: 父和叔都染黑,祖父染红, 然后关注祖父是否引起了失衡(双红缺陷)

            // 2. 叔为黑 叔和节点的方向一致, 即都为左子或右子;
            // 2.1 需要父向外旋转, 即左子时右旋、右子时左旋
            // 2.2 旋转后原父子节点关系变成子父关系, 关注点也由原来的子变原来的父
            // 2.3 然后就会变成策略3

            // 3. 叔为黑 叔和节点的方向不一致
            // 3.1 父染黑、祖父染红
            // 3.2 祖父节点外旋, 即左子时祖父右旋、右子时祖父左旋

            x.red = true; // 符合定义定义0
            // xp, 当前插入节点的父亲
            // xpp:祖父 xppl:左叔父 xppr:右叔父
            for (TreeNode<K, V> xp, xpp, xppl, xppr; ; ) {
                if ((xp = x.parent) == null) { // 新节点被插入成root, 则染黑并返回
                    x.red = false;
                    return x;
                } else if (!xp.red || (xpp = xp.parent) == null) {
                    // 父节点是黑色, 可以直接返回, 符合定义4
                    // (此条件非必要? 若父为红, 则parent必不空)
                    return root;
                }

                // 走到这, 说明父节点为红色, 因为当前节点也为红色, 违反定义3, 需要重平衡

                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) { // 父、右叔父都为红?
                        // 策略1
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        // 继续关注变红了的祖父是否会引起失衡
                        x = xpp;
                    } else { // 父为红, 右叔父为黑
                        if (x == xp.right) {
                            // 策略2, 围绕父节点左旋
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }

                        if (xp != null) {
                            // 策略3, 父染黑、祖父染红
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                // 围绕祖父节点右旋
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                } else {
                    if (xppl != null && xppl.red) { // 父、左叔父都为红?
                        // 策略1
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        // 继续关注变红了的祖父是否会引起失衡
                        x = xpp;
                    } else { // 父为红, 左叔父为黑
                        if (x == xp.left) {
                            // 策略2, 围绕父节点右旋
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }

                        if (xp != null) {
                            // 策略3, 父染黑、祖父染红
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                // 围绕祖父节点左旋
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            for (TreeNode<K, V> xp, xpl, xpr; ; ) {
                if (x == null || x == root) return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                } else if (x.red) {
                    x.red = false;
                    return root;
                } else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null) x = xp;
                    else {
                        TreeNode<K, V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) && (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        } else {
                            if (sr == null || !sr.red) {
                                if (sl != null) sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ? null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null) sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                } else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null) x = xp;
                    else {
                        TreeNode<K, V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) && (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        } else {
                            if (sl == null || !sl.red) {
                                if (sr != null) sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ? null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null) sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tp = t.parent, tl = t.left, tr = t.right, tb = t.prev, tn = (TreeNode<K, V>) t.next;
            if (tb != null && tb.next != t) return false;
            if (tn != null && tn.prev != t) return false;
            if (tp != null && t != tp.left && t != tp.right) return false;
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) return false;
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) return false;
            if (t.red && tl != null && tl.red && tr != null && tr.red) return false;
            if (tl != null && !checkInvariants(tl)) return false;
            if (tr != null && !checkInvariants(tr)) return false;
            return true;
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

}