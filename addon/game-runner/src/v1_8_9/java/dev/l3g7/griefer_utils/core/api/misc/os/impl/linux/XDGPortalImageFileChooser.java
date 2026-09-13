package dev.l3g7.griefer_utils.core.api.misc.os.impl.linux;

import com.sun.jna.Callback;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import dev.l3g7.griefer_utils.core.api.misc.os.impl.linux.XDGPortalImageFileChooser.GIO.SignalCallback;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

/**
 * org.freedesktop.portal.FileChooser.OpenFile DBus call via libgio.
 */
public class XDGPortalImageFileChooser {

	private static final int G_BUS_TYPE_SESSION = 2;
	private static final String PORTAL = "org.freedesktop.portal.Desktop";

	@SuppressWarnings("FieldCanBeLocal") // No NOT store as local because JNA frees it on GC
	private static SignalCallback subscribed;

	public static List<String> chooseImageFile(String title) {
		// Connect to DBus
		PointerByReference error = new PointerByReference();
		Pointer connection = GIO.g_bus_get_sync(G_BUS_TYPE_SESSION, null, error);
		if (connection == null)
			throw new IllegalStateException(failure("No session bus", error));

		String token = "javafc" + Long.toHexString(System.nanoTime());
		String requestPath = "/org/freedesktop/portal/desktop/request/"
			+ GIO.g_dbus_connection_get_unique_name(connection).substring(1).replace('.', '_')
			+ "/" + token;

		// Start listening
		IntByReference code = new IntByReference();
		PointerByReference results = new PointerByReference();
		subscribed = (conn, sender, path, iface, signal, parameters, userData) -> {
			Pointer response = GIO.g_variant_get_child_value(parameters, 0);
			code.setValue(GIO.g_variant_get_uint32(response));
			GIO.g_variant_unref(response);
			results.setValue(GIO.g_variant_get_child_value(parameters, 1));
		};

		int subscription = GIO.g_dbus_connection_signal_subscribe(
			connection, PORTAL, "org.freedesktop.portal.Request", "Response",
			requestPath, null, 0, subscribed, null, null
		);

		// Build request
		List<String> filters = new ArrayList<>();
		for (String suffix : ImageIO.getReaderFileSuffixes())
			filters.add("(0, " + quote("*." + suffix) + ")");
		String slots = String.join(", ", filters);

		Pointer parameters = GIO.g_variant_parse(null,
			"('', " + quote(title) + ", {'handle_token': <" + quote(token) + ">, 'filters': <@a(sa(us))"
				+ " [('Images', [" + slots + "])]>})",
			null, null, error
		);
		if (parameters == null)
			throw new IllegalStateException(failure("Invalid parameters", error));

		// Call
		Pointer reply = GIO.g_dbus_connection_call_sync(
			connection, PORTAL,
			"/org/freedesktop/portal/desktop", "org.freedesktop.portal.FileChooser", "OpenFile",
			parameters, "(o)", 0, -1, null, error
		);
		GIO.g_variant_unref(parameters); // g_variant_parse returns a non-floating reference, so it isn't consumed by the call
		if (reply == null)
			throw new IllegalStateException(failure("OpenFile failed", error));
		GIO.g_variant_unref(reply);

		// Wait for response
		while (results.getValue() == null)
			GIO.g_main_context_iteration(null, true);
		GIO.g_dbus_connection_signal_unsubscribe(connection, subscription);

		// Parse response
		List<String> uris = List.of();
		Pointer uriVariant = code.getValue() == 0 ? GIO.g_variant_lookup_value(results.getValue(), "uris", "as") : null;
		if (uriVariant != null) {
			Pointer strv = GIO.g_variant_get_strv(uriVariant, null);
			uris = List.of(strv.getStringArray(0));
			GIO.g_free(strv);
			GIO.g_variant_unref(uriVariant);
		}
		GIO.g_variant_unref(results.getValue());
		return uris;
	}

	/**
	 * Quotes a string for the GVariant text format.
	 */
	private static String quote(String value) {
		return "'" + value.replace("\\", "\\\\").replace("'", "\\'") + "'";
	}

	private static String failure(String action, PointerByReference error) {
		Pointer err = error.getValue();
		String message = err.getPointer(8).getString(0);
		GIO.g_error_free(err);
		return action + ": " + message;
	}

	public static class GIO {

		static {
			Native.register("libgio-2.0.so.0");
		}

		public static native Pointer g_bus_get_sync(int busType, Pointer cancellable, PointerByReference error);

		public static native String g_dbus_connection_get_unique_name(Pointer connection);

		public static native int g_dbus_connection_signal_subscribe(Pointer connection, String sender, String iface, String member, String objectPath, String arg0, int flags, SignalCallback callback, Pointer userData, Pointer userDataFree);

		public static native void g_dbus_connection_signal_unsubscribe(Pointer connection, int subscriptionId);

		public static native Pointer g_dbus_connection_call_sync(Pointer connection, String busName, String objectPath, String iface, String method, Pointer parameters, String replyType, int flags, int timeoutMsec, Pointer cancellable, PointerByReference error);

		@SuppressWarnings("UnusedReturnValue")
		public static native boolean g_main_context_iteration(Pointer context, boolean mayBlock);

		public static native Pointer g_variant_parse(String type, String text, String limit, Pointer endptr, PointerByReference error);

		public static native Pointer g_variant_get_child_value(Pointer value, long index);

		public static native int g_variant_get_uint32(Pointer value);

		public static native Pointer g_variant_lookup_value(Pointer dictionary, String key, String expectedType);

		public static native Pointer g_variant_get_strv(Pointer value, Pointer length);

		public static native void g_variant_unref(Pointer value);

		public static native void g_error_free(Pointer error);

		public static native void g_free(Pointer memory);

		public interface SignalCallback extends Callback {
			void invoke(Pointer connection, String sender, String objectPath, String iface,
			            String signal, Pointer parameters, Pointer userData);
		}
	}

}
