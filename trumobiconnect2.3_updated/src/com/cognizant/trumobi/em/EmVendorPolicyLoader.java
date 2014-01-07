

package com.cognizant.trumobi.em;

import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils.Provider;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * A bridge class to the email vendor policy apk.
 *
 * <p>Email vendor policy is a system apk named "com.cognizant.trumobi.em.helper".  When exists, it must
 * contain a class called "com.cognizant.trumobi.em.policy.EmailPolicy" with a static public method
 * <code>Bundle getPolicy(String, Bundle)</code>, which serves vendor specific configurations.
 *
 * <p>A vendor policy apk is optional.  The email application will operate properly when none is
 * found.
 */
public class EmVendorPolicyLoader {
    private static final String POLICY_PACKAGE = "com.cognizant.trumobi.em.policy";
    private static final String POLICY_CLASS = POLICY_PACKAGE + ".EmailPolicy";
    private static final String GET_POLICY_METHOD = "getPolicy";
    private static final Class<?>[] ARGS = new Class<?>[] {String.class, Bundle.class};

    // call keys and i/o bundle keys
    // when there is only one parameter or return value, use call key
    private static final String USE_ALTERNATE_EXCHANGE_STRINGS = "useAlternateExchangeStrings";

    private static final String FIND_PROVIDER = "findProvider";
    private static final String FIND_PROVIDER_IN_URI = "findProvider.inUri";
    private static final String FIND_PROVIDER_IN_USER = "findProvider.inUser";
    private static final String FIND_PROVIDER_OUT_URI = "findProvider.outUri";
    private static final String FIND_PROVIDER_OUT_USER = "findProvider.outUser";
    private static final String FIND_PROVIDER_NOTE = "findProvider.note";

    /** Singleton instance */
    private static EmVendorPolicyLoader sInstance;

    private final Method mPolicyMethod;

    public static EmVendorPolicyLoader getInstance(Context context) {
        if (sInstance == null) {
            // It's okay to instantiate VendorPolicyLoader multiple times.  No need to synchronize.
            sInstance = new EmVendorPolicyLoader(context);
        }
        return sInstance;
    }

    /**
     * For testing only.
     *
     * Replaces the instance with a new instance that loads a specified class.
     */
    public static void injectPolicyForTest(Context context, String apkPackageName, Class<?> clazz) {
        String name = clazz.getName();
        Log.d(Email.LOG_TAG, String.format("Using policy: package=%s name=%s",
                apkPackageName, name));
        sInstance = new EmVendorPolicyLoader(context, apkPackageName, name, true);
    }

    /**
     * For testing only.
     *
     * Clear the instance so that the next {@link #getInstance} call will return a regular,
     * non-injected instance.
     */
    public static void clearInstanceForTest() {
        sInstance = null;
    }

    private EmVendorPolicyLoader(Context context) {
        this(context, POLICY_PACKAGE, POLICY_CLASS, false);
    }

    /**
     * Constructor for testing, where we need to use an alternate package/class name, and skip
     * the system apk check.
     */
    EmVendorPolicyLoader(Context context, String apkPackageName, String className,
            boolean allowNonSystemApk) {
        if (!allowNonSystemApk && !isSystemPackage(context, apkPackageName)) {
            mPolicyMethod = null;
            return;
        }

        Class<?> clazz = null;
        Method method = null;
        try {
            final Context policyContext = context.createPackageContext(apkPackageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            final ClassLoader classLoader = policyContext.getClassLoader();
            clazz = classLoader.loadClass(className);
            method = clazz.getMethod(GET_POLICY_METHOD, ARGS);
        } catch (NameNotFoundException ignore) {
            // Package not found -- it's okay - there's no policy .apk found, which is OK
        } catch (ClassNotFoundException e) {
            // Class not found -- probably not OK, but let's not crash here
            Log.w(Email.LOG_TAG, "VendorPolicyLoader: " + e);
        } catch (NoSuchMethodException e) {
            // Method not found -- probably not OK, but let's not crash here
            Log.w(Email.LOG_TAG, "VendorPolicyLoader: " + e);
        }
        mPolicyMethod = method;
    }

    // Not private for testing
    /* package */ static boolean isSystemPackage(Context context, String packageName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(packageName, 0);
            return (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (NameNotFoundException e) {
            return false; // Package not found.
        }
    }

    /**
     * Calls the getPolicy method in the policy apk, if one exists.  This method never returns null;
     * It returns an empty {@link Bundle} when there is no policy apk (or even if the inner
     * getPolicy returns null).
     */
    // Not private for testing
    /* package */ Bundle getPolicy(String policy, Bundle args) {
        Bundle ret = null;
        if (mPolicyMethod != null) {
            try {
                ret = (Bundle) mPolicyMethod.invoke(null, policy, args);
            } catch (Exception e) {
                Log.w(Email.LOG_TAG, "VendorPolicyLoader", e);
            }
        }
        return (ret != null) ? ret : Bundle.EMPTY;
    }

    /**
     * Returns true if alternate exchange descriptive text is required.
     *
     * Vendor function:
     *  Select: USE_ALTERNATE_EXCHANGE_STRINGS
     *  Params: none
     *  Result: USE_ALTERNATE_EXCHANGE_STRINGS (boolean)
     */
    public boolean useAlternateExchangeStrings() {
        return getPolicy(USE_ALTERNATE_EXCHANGE_STRINGS, null)
                .getBoolean(USE_ALTERNATE_EXCHANGE_STRINGS, false);
    }

    

    /**
     * Returns provider setup information for a given email address
     *
     * Vendor function:
     *  Select: FIND_PROVIDER
     *  Param:  FIND_PROVIDER (String)
     *  Result: FIND_PROVIDER_IN_URI
     *          FIND_PROVIDER_IN_USER
     *          FIND_PROVIDER_OUT_URI
     *          FIND_PROVIDER_OUT_USER
     *          FIND_PROVIDER_NOTE (optional - null is OK)
     *
     * Note, if we get this far, we expect "correct" results from the policy method.  But throwing
     * checked exceptions requires a bunch of upstream changes, so we're going to catch them here
     * and add logging.  Other exceptions may escape here (such as null pointers) to fail fast.
     *
     * @param domain The domain portion of the user's email address
     * @return suitable Provider definition, or null if no match found
     */
    public Provider findProviderForDomain(String domain) {
        Bundle params = new Bundle();
        params.putString(FIND_PROVIDER, domain);
        Bundle out = getPolicy(FIND_PROVIDER, params);
        if (out != null && !out.isEmpty()) {
            try {
                Provider p = new Provider();
                p.id = null;
                p.label = null;
                p.domain = domain;
                p.incomingUriTemplate = new URI(out.getString(FIND_PROVIDER_IN_URI));
                p.incomingUsernameTemplate = out.getString(FIND_PROVIDER_IN_USER);
                p.outgoingUriTemplate = new URI(out.getString(FIND_PROVIDER_OUT_URI));
                p.outgoingUsernameTemplate = out.getString(FIND_PROVIDER_OUT_USER);
                p.note = out.getString(FIND_PROVIDER_NOTE);
                return p;
            } catch (URISyntaxException e) {
                Log.d(Email.LOG_TAG, "uri exception while vendor policy loads " + domain);
            }
        }
        return null;
    }
}
