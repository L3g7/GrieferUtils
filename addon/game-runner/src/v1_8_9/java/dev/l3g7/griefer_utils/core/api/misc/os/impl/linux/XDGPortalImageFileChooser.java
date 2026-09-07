package dev.l3g7.griefer_utils.core.api.misc.os.impl.linux;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import dev.l3g7.griefer_utils.core.api.misc.os.impl.linux.XDGPortalImageFileChooser.Gio.SignalCallback;

import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * org.freedesktop.portal.FileChooser.OpenFile DBus call via libgio.
 */
public class XDGPortalImageFileChooser {

	private static final Gio GIO = Native.load("libgio-2.0.so.0", Gio.class);
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
		subscribed = (conn, sender, path, iface, signal, parameters, userData)
			-> GIO.g_variant_get(parameters, "(u@a{sv})", code, results);

		int subscription = GIO.g_dbus_connection_signal_subscribe(
			connection, PORTAL, "org.freedesktop.portal.Request", "Response",
			requestPath, null, 0, subscribed, null, null
		);

		// Build request
		String[] suffixes = ImageIO.getReaderFileSuffixes();
		String slots = String.join(", ", Collections.nCopies(suffixes.length, "(0, %s)"));
		List<Object> values = new ArrayList<>(List.of(title, token));
		for (String suffix : suffixes) values.add("*." + suffix);

		// Call
		Pointer reply = GIO.g_dbus_connection_call_sync(
			connection, PORTAL,
			"/org/freedesktop/portal/desktop", "org.freedesktop.portal.FileChooser", "OpenFile",
			GIO.g_variant_new_parsed("('', %s, {'handle_token': <%s>, 'filters': <@a(sa(us))" + " [('Images', [" + slots + "])]>})", values.toArray()),
			"(o)", 0, -1, null, error
		);
		if (reply == null)
			throw new IllegalStateException(failure("OpenFile failed", error));
		GIO.g_variant_unref(reply);

		// Wait for response
		while (results.getValue() == null)
			GIO.g_main_context_iteration(null, true);
		GIO.g_dbus_connection_signal_unsubscribe(connection, subscription);

		// Parse response
		List<String> uris = List.of();
		PointerByReference strv = new PointerByReference();
		if (code.getValue() == 0 && GIO.g_variant_lookup(results.getValue(), "uris", "^a&s", strv)) {
			uris = List.of(strv.getValue().getStringArray(0));
			GIO.g_free(strv.getValue());
		}
		GIO.g_variant_unref(results.getValue());
		return uris;
	}

	private static String failure(String action, PointerByReference error) {
		Pointer err = error.getValue();
		String message = err.getPointer(8).getString(0);
		GIO.g_error_free(err);
		return action + ": " + message;
	}

	public interface Gio extends Library {
		Pointer g_bus_get_sync(int busType, Pointer cancellable, PointerByReference error);

		String g_dbus_connection_get_unique_name(Pointer connection);

		int g_dbus_connection_signal_subscribe(Pointer connection, String sender, String iface, String member, String objectPath, String arg0, int flags, SignalCallback callback, Pointer userData, Pointer userDataFree);

		void g_dbus_connection_signal_unsubscribe(Pointer connection, int subscriptionId);

		Pointer g_dbus_connection_call_sync(Pointer connection, String busName, String objectPath, String iface, String method, Pointer parameters, String replyType, int flags, int timeoutMsec, Pointer cancellable, PointerByReference error);

		@SuppressWarnings("UnusedReturnValue")
		boolean g_main_context_iteration(Pointer context, boolean mayBlock);

		Pointer g_variant_new_parsed(String format, Object... values);

		void g_variant_get(Pointer value, String format, Object... locations);

		boolean g_variant_lookup(Pointer dictionary, String key, String format, Object... locations);

		void g_variant_unref(Pointer value);

		void g_error_free(Pointer error);

		void g_free(Pointer memory);

		interface SignalCallback extends Callback {
			void invoke(Pointer connection, String sender, String objectPath, String iface,
			            String signal, Pointer parameters, Pointer userData);
		}
	}

}
