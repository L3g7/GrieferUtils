package dev.l3g7.griefer_utils.util.win32;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.lwjgl.opengl.Display;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Winuser {
	static {
		Native.register("user32");
	}

	private static native boolean ShowWindow(WinDef.HWND hwnd, int nCmdShow);

	/**
	 * Source: <a href="https://gamedev.stackexchange.com/a/133258">StackExchange</a>
	 */
	private static long getLWJGLHwnd() {
		try {
			Method getImplementation = Display.class.getDeclaredMethod("getImplementation");
			getImplementation.setAccessible(true);
			Object impl = getImplementation.invoke(null);

			Class<?> windowsDisplayClass = Class.forName("org.lwjgl.opengl.WindowsDisplay");
			Method methHwnd = windowsDisplayClass.getDeclaredMethod("getHwnd");
			methHwnd.setAccessible(true);
			return (Long) methHwnd.invoke(impl);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			throw new RuntimeException(e);
		}
	}

	/**
	 * Source: com.sun.jna.platform.WindowUtils.W32WindowUtils.getHWnd(Component)
	 */
	public static void maximizeMinecraft() {
		WinDef.HWND hwnd = new WinDef.HWND();
		hwnd.setPointer(new Pointer(getLWJGLHwnd()));

		ShowWindow(hwnd, WinUser.SW_SHOWMAXIMIZED);
	}

}
