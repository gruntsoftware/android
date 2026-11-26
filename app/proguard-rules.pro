# R8 rules for the Brainwallet release build.
# Shrinking, obfuscation and optimization are all ON (Play Console DEX optimization requirement).
# Only keep what is reached by name at runtime (JNI); library consumer rules cover the rest.

# ---------- Crash reporting ----------
# Keep line numbers for Crashlytics; the plugin uploads mapping.txt automatically.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---------- JNI ----------
# Native method names must match Java_<pkg>_<Class>_<method> symbols.
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.jniwrappers.** { *; }

# Classes the C core looks up via FindClass/GetMethodID/GetFieldID
# (app/src/main/jni/transition/{PeerManager,wallet,JNIPaymentsCore}.c).
-keep class com.brainwallet.wallet.BRPeerManager { *; }
-keep class com.brainwallet.wallet.BRWalletManager { *; }
-keep class com.brainwallet.presenter.entities.BlockEntity { *; }
-keep class com.brainwallet.presenter.entities.PeerEntity { *; }
-keep class com.brainwallet.presenter.entities.TxItem { *; }
-keep class com.brainwallet.presenter.entities.ImportPrivKeyEntity { *; }
-keep class com.brainwallet.presenter.entities.PaymentRequestWrapper { *; }

# ---------- Navigation Compose type-safe routes ----------
# Navigation resolves enum route arguments by their serial (= fully qualified) name via
# Class.forName, e.g. Route.Restore.Source. Obfuscating/removing them crashes NavHost setup:
# "Cannot find class with name com.brainwallet.navigation.Route.Restore.Source?".
# Route is also java.io.Serializable (saved state), so keep names of the whole hierarchy.
-keepnames class com.brainwallet.navigation.Route
-keepnames class com.brainwallet.navigation.Route$** { *; }
-keep enum com.brainwallet.navigation.** { *; }

# ---------- libGDX game exit payload ----------
# GameExitJSON is written by libGDX's reflection-based Json, so its field names ARE the JSON
# keys (bonus_amount, total_score, ...) parsed back in GdxGameView. Renaming them silently
# drops game results because the app parser ignores unknown keys.
-keep class ltd.grunt.brainwallet.gamegdx.models.** { *; }

# ---------- Views inflated from XML ----------
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ---------- Missing optional classes referenced by libraries ----------
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
-dontwarn java.awt.Toolkit
-dontwarn java.beans.Introspector
-dontwarn java.lang.instrument.IllegalClassFormatException
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.MemoryMXBean
-dontwarn java.lang.management.MemoryUsage
-dontwarn javax.imageio.ImageIO
-dontwarn org.eclipse.jetty.jmx.ObjectMBean
-dontwarn org.ietf.jgss.GSSContext
-dontwarn org.ietf.jgss.GSSCredential
-dontwarn org.ietf.jgss.GSSException
-dontwarn org.ietf.jgss.GSSManager
-dontwarn org.ietf.jgss.GSSName
-dontwarn org.ietf.jgss.Oid
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn org.slf4j.impl.StaticMDCBinder
-dontwarn org.slf4j.impl.StaticMarkerBinder
-dontwarn java.lang.reflect.AnnotatedType
-dontwarn com.google.api.client.http.**
-dontwarn org.joda.time.**
-dontwarn com.badlogic.gdx.maps.tiled.AtlasTmxMapLoader$AtlasTiledMapLoaderParameters
-dontwarn com.badlogic.gdx.maps.tiled.TmxMapLoader$Parameters
-dontwarn com.badlogic.gdx.utils.StringBuilder
