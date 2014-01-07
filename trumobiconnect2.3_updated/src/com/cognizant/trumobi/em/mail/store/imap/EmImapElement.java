

package com.cognizant.trumobi.em.mail.store.imap;

/**
 * Class representing "element"s in IMAP responses.
 *
 * <p>Class hierarchy:
 * <pre>
 * ImapElement
 *   |
 *   |-- ImapElement.NONE (for 'index out of range')
 *   |
 *   |-- ImapList (isList() == true)
 *   |   |
 *   |   |-- ImapList.EMPTY
 *   |   |
 *   |   --- ImapResponse
 *   |
 *   --- ImapString (isString() == true)
 *       |
 *       |-- ImapString.EMPTY
 *       |
 *       |-- ImapSimpleString
 *       |
 *       |-- ImapMemoryLiteral
 *       |
 *       --- ImapTempFileLiteral
 * </pre>
 */
public abstract class EmImapElement {
    /**
     * An element that is returned by {@link EmImapList#getElementOrNone} to indicate an index
     * is out of range.
     */
    public static final EmImapElement NONE = new EmImapElement() {
        @Override public void destroy() {
            // Don't call super.destroy().
            // It's a shared object.  We don't want the mDestroyed to be set on this.
        }

        @Override public boolean isList() {
            return false;
        }

        @Override public boolean isString() {
            return false;
        }

        @Override public String toString() {
            return "[NO ELEMENT]";
        }

        @Override
        public boolean equalsForTest(EmImapElement that) {
            return super.equalsForTest(that);
        }
    };

    private boolean mDestroyed = false;

    public abstract boolean isList();

    public abstract boolean isString();

    protected boolean isDestroyed() {
        return mDestroyed;
    }

    /**
     * Clean up the resources used by the instance.
     * It's for removing a temp file used by {@link EmImapTempFileLiteral}.
     */
    public void destroy() {
        mDestroyed = true;
    }

    /**
     * Throws {@link RuntimeException} if it's already destroyed.
     */
    protected final void checkNotDestroyed() {
        if (mDestroyed) {
            throw new RuntimeException("Already destroyed");
        }
    }

    /**
     * Return a string that represents this object; it's purely for the debug purpose.  Don't
     * mistake it for {@link EmImapString#getString}.
     *
     * Abstract to force subclasses to implement it.
     */
    @Override
    public abstract String toString();

    /**
     * The equals implementation that is intended to be used only for unit testing.
     * (Because it may be heavy and has a special sense of "equal" for testing.)
     */
    public boolean equalsForTest(EmImapElement that) {
        if (that == null) {
            return false;
        }
        return this.getClass() == that.getClass(); // Has to be the same class.
    }
}
