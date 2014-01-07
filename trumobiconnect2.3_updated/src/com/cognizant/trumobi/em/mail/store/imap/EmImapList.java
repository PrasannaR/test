

package com.cognizant.trumobi.em.mail.store.imap;

import java.util.ArrayList;

/**
 * Class represents an IMAP list.
 */
public class EmImapList extends EmImapElement {
    /**
     * {@link EmImapList} representing an empty list.
     */
    public static final EmImapList EMPTY = new EmImapList() {
        @Override public void destroy() {
            // Don't call super.destroy().
            // It's a shared object.  We don't want the mDestroyed to be set on this.
        }

        @Override void add(EmImapElement e) {
            throw new RuntimeException();
        }
    };

    private ArrayList<EmImapElement> mList = new ArrayList<EmImapElement>();

    /* package */ void add(EmImapElement e) {
        if (e == null) {
            throw new RuntimeException("Can't add null");
        }
        mList.add(e);
    }

    @Override
    public final boolean isString() {
        return false;
    }

    @Override
    public final boolean isList() {
        return true;
    }

    public final int size() {
        return mList.size();
    }

    public final boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Return true if the element at {@code index} exists, is string, and equals to {@code s}.
     * (case insensitive)
     */
    public final boolean is(int index, String s) {
        return is(index, s, false);
    }

    /**
     * Same as {@link #is(int, String)}, but does the prefix match if {@code prefixMatch}.
     */
    public final boolean is(int index, String s, boolean prefixMatch) {
        if (!prefixMatch) {
            return getStringOrEmpty(index).is(s);
        } else {
            return getStringOrEmpty(index).startsWith(s);
        }
    }

    /**
     * Return the element at {@code index}.
     * If {@code index} is out of range, returns {@link EmImapElement#NONE}.
     */
    public final EmImapElement getElementOrNone(int index) {
        return (index >= mList.size()) ? EmImapElement.NONE : mList.get(index);
    }

    /**
     * Return the element at {@code index} if it's a list.
     * If {@code index} is out of range or not a list, returns {@link EmImapList#EMPTY}.
     */
    public final EmImapList getListOrEmpty(int index) {
        EmImapElement el = getElementOrNone(index);
        return el.isList() ? (EmImapList) el : EMPTY;
    }

    /**
     * Return the element at {@code index} if it's a string.
     * If {@code index} is out of range or not a string, returns {@link EmImapString#EMPTY}.
     */
    public final EmImapString getStringOrEmpty(int index) {
        EmImapElement el = getElementOrNone(index);
        return el.isString() ? (EmImapString) el : EmImapString.EMPTY;
    }

    /**
     * Return an element keyed by {@code key}.  Return null if not found.  {@code key} has to be
     * at an even index.
     */
    /* package */ final EmImapElement getKeyedElementOrNull(String key, boolean prefixMatch) {
        for (int i = 1; i < size(); i += 2) {
            if (is(i-1, key, prefixMatch)) {
                return mList.get(i);
            }
        }
        return null;
    }

    /**
     * Return an {@link EmImapList} keyed by {@code key}.
     * Return {@link EmImapList#EMPTY} if not found.
     */
    public final EmImapList getKeyedListOrEmpty(String key) {
        return getKeyedListOrEmpty(key, false);
    }

    /**
     * Return an {@link EmImapList} keyed by {@code key}.
     * Return {@link EmImapList#EMPTY} if not found.
     */
    public final EmImapList getKeyedListOrEmpty(String key, boolean prefixMatch) {
        EmImapElement e = getKeyedElementOrNull(key, prefixMatch);
        return (e != null) ? ((EmImapList) e) : EmImapList.EMPTY;
    }

    /**
     * Return an {@link EmImapString} keyed by {@code key}.
     * Return {@link EmImapString#EMPTY} if not found.
     */
    public final EmImapString getKeyedStringOrEmpty(String key) {
        return getKeyedStringOrEmpty(key, false);
    }

    /**
     * Return an {@link EmImapString} keyed by {@code key}.
     * Return {@link EmImapString#EMPTY} if not found.
     */
    public final EmImapString getKeyedStringOrEmpty(String key, boolean prefixMatch) {
        EmImapElement e = getKeyedElementOrNull(key, prefixMatch);
        return (e != null) ? ((EmImapString) e) : EmImapString.EMPTY;
    }

    /**
     * Return true if it contains {@code s}.
     */
    public final boolean contains(String s) {
        for (int i = 0; i < size(); i++) {
            if (getStringOrEmpty(i).is(s)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        if (mList != null) {
            for (EmImapElement e : mList) {
                e.destroy();
            }
            mList = null;
        }
        super.destroy();
    }

    @Override
    public String toString() {
        return mList.toString();
    }

    /**
     * Return the text representations of the contents concatenated with ",".
     */
    public final String flatten() {
        return flatten(new StringBuilder()).toString();
    }

    /**
     * Returns text representations (i.e. getString()) of contents joined together with
     * "," as the separator.
     *
     * Only used for building the capability string passed to vendor policies.
     *
     * We can't use toString(), because it's for debugging (meaning the format may change any time),
     * and it won't expand literals.
     */
    private final StringBuilder flatten(StringBuilder sb) {
        sb.append('[');
        for (int i = 0; i < mList.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            final EmImapElement e = getElementOrNone(i);
            if (e.isList()) {
                getListOrEmpty(i).flatten(sb);
            } else if (e.isString()) {
                sb.append(getStringOrEmpty(i).getString());
            }
        }
        sb.append(']');
        return sb;
    }

    @Override
    public boolean equalsForTest(EmImapElement that) {
        if (!super.equalsForTest(that)) {
            return false;
        }
        EmImapList thatList = (EmImapList) that;
        if (size() != thatList.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!mList.get(i).equalsForTest(thatList.getElementOrNone(i))) {
                return false;
            }
        }
        return true;
    }
}
