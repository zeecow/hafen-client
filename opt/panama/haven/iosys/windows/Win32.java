/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */

package haven.iosys.windows;

import haven.*;
import haven.ffi.*;
import java.lang.invoke.*;
import java.lang.foreign.*;
import java.util.*;
import java.util.function.*;
import java.nio.*;
import java.nio.charset.Charset;
import java.lang.foreign.MemoryLayout.PathElement;
import static haven.ffi.ABI.*;
import static haven.ffi.FUtils.*;
import static java.lang.foreign.ValueLayout.ADDRESS;

public abstract class Win32 {
    public static final int WS_OVERLAPPED = 0x00000000;
    public static final int WS_CAPTION = 0x00C00000;
    public static final int WS_SYSMENU = 0x00080000;
    public static final int WS_THICKFRAME = 0x00040000;
    public static final int WS_MINIMIZEBOX = 0x00020000;
    public static final int WS_MAXIMIZEBOX = 0x00010000;
    public static final int WS_OVERLAPPEDWINDOW = WS_OVERLAPPED | WS_CAPTION | WS_SYSMENU | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX;

    public static final int CW_USEDEFAULT = 0x80000000;

    public static final int PM_NOREMOVE = 0x0000;
    public static final int PM_REMOVE = 0x0001;
    public static final int PM_NOYIELD = 0x0002;

    public static final int CS_OWNDC = 0x0020;

    public static final int SW_HIDE = 0;
    public static final int SW_NORMAL = 1;
    public static final int SW_SHOWMINIMIZED = 2;
    public static final int SW_MAXIMIZE = 3;
    public static final int SW_SHOWNOACTIVATE = 4;
    public static final int SW_SHOW = 5;
    public static final int SW_MINIMIZE = 6;
    public static final int SW_SHOWMINNOACTIVE = 7;
    public static final int SW_SHOWNA = 8;
    public static final int SW_RESTORE = 9;
    public static final int SW_SHOWDEFAULT = 10;
    public static final int SW_FORCEMINIMIZE = 11;

    public static final int WM_SIZE       = 0x0005;
    public static final int WM_CLOSE      = 0x0010;
    public static final int WM_KEYDOWN    = 0x0100;
    public static final int WM_KEYUP      = 0x0101;
    public static final int WM_SYSKEYDOWN = 0x0104;
    public static final int WM_SYSKEYUP   = 0x0105;
    public static final int WM_SYSCOMMAND = 0x0112;
    public static final int WM_USER       = 0x0400;

    public static final int SC_KEYMENU = 0xf100;

    public static final int PFD_DOUBLEBUFFER   = 0x00000001;
    public static final int PFD_DRAW_TO_WINDOW = 0x00000004;
    public static final int PFD_SUPPORT_OPENGL = 0x00000020;
    public static final int PFD_TYPE_RGBA = 0;

    public static class Handle {
	public static final Handle nil = new Handle(MemorySegment.NULL);
	final MemorySegment bits;

	private Handle(MemorySegment bits) {this.bits = bits;}
	static Handle of(MemorySegment bits) {return(nullp(bits) ? nil : new Handle(bits));}

	public int hashCode() {return(Long.hashCode(bits.address()));}
	public boolean equals(Object x) {return((x instanceof Handle) && (((Handle)x).bits.equals(this.bits)));}
	public String toString() {
	    return("$" + Long.toUnsignedString(bits.address(), 16));
	}
    }

    public static interface WndProc {
	public long handle(Handle hwnd, int umsg, long wparam, long lparam);
    }

    public static abstract class WndClassEx extends StructInstance {
	protected WndClassEx(MemorySegment mem) {
	    super(mem);
	}

	MemorySegment mem() {return(mem);}

	public abstract WndClassEx style(int value);
	public abstract WndClassEx lpfnWndProc(WndProc value);
	public abstract WndClassEx hInstance(Handle value);
	public abstract WndClassEx lpszClassName(String value);
    }
    public abstract WndClassEx WndClassEx();

    public static abstract class MSG extends StructInstance {
	protected MSG(MemorySegment mem) {
	    super(mem);
	}

	MemorySegment mem() {return(mem);}
    }
    public abstract MSG MSG();

    public static class KeyboardState {
	final MemorySegment keys;

	public KeyboardState() {
	    keys = Arena.ofAuto().allocate(256);
	}

	public int get(int vk) {
	    return(keys.get(ValueLayout.JAVA_BYTE, vk) & 0xff);
	}

	public KeyboardState set(int vk, int val) {
	    keys.set(ValueLayout.JAVA_BYTE, vk, (byte)val);
	    return(this);
	}
    }

    public static abstract class PIXELFORMATDESCRIPTOR extends StructInstance {
	protected PIXELFORMATDESCRIPTOR(MemorySegment mem) {
	    super(mem);
	}

	MemorySegment mem() {return(mem);}

	public abstract PIXELFORMATDESCRIPTOR nVersion(int value);
	public abstract PIXELFORMATDESCRIPTOR dwFlags(int value);
	public abstract PIXELFORMATDESCRIPTOR iPixelType(int value);
	public abstract PIXELFORMATDESCRIPTOR cColorBits(int value);
	public abstract PIXELFORMATDESCRIPTOR cAlphaBits(int value);
	public abstract PIXELFORMATDESCRIPTOR cAccumBits(int value);
	public abstract PIXELFORMATDESCRIPTOR cDepthBits(int value);
	public abstract PIXELFORMATDESCRIPTOR cStencilBits(int value);
	public abstract PIXELFORMATDESCRIPTOR cAuxBuffers(int value);
	public abstract PIXELFORMATDESCRIPTOR iLayerType(int value);
    }
    public abstract PIXELFORMATDESCRIPTOR PIXELFORMATDESCRIPTOR();

    public abstract int GetLastError();
    public abstract int GetCurrentThreadId();
    public abstract Handle GetModuleHandle(String lpModuleName);
    public abstract int RegisterClassEx(WndClassEx cls);
    public abstract void UnregisterClass(String lpClassName, Handle hInstance);
    public abstract Handle CreateWindowEx(int dwExStyle, String lpClassName, String lpWindowName, int dwStyle, Coord pos, Coord size, Handle hWndParent, Handle hMenu, Handle hInstance);
    public abstract int DestroyWindow(Handle hWnd);
    public abstract boolean GetMessage(MSG lpMsg, Handle hWnd, int wMsgFilterMin, int wMsgFilterMax);
    public abstract boolean PeekMessage(Win32.MSG lpMsg, Handle hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg);
    public abstract long DispatchMessage(Win32.MSG lpMsg);
    public abstract void PostMessage(Handle hWnd, int Msg, long wParam, long lParam);
    public abstract void PostThreadMessage(int idThread, int Msg, long wParam, long lParam);
    public abstract long DefWindowProc(Handle hWnd, int Msg, long wParam, long lParam);
    public abstract boolean ShowWindow(Handle hWnd, int nCmdShow);
    public abstract void SetWindowText(Handle hWnd, String lpString);
    public abstract Handle GetDC(Handle hWnd);
    public abstract boolean ReleaseDC(Handle hWnd, Handle hDC);
    public abstract int GetKeyState(int nVirtKey);
    public abstract void GetKeyboardState(KeyboardState buf);
    public abstract Handle GetKeyboardLayout(int idThread);
    public abstract char[] ToUnicodeEx(int wVirtKey, int wScanCode, KeyboardState lpKeyState, int wFlags, Handle dwhkl);
    public abstract String GetKeyNameText(long lParam);
    public abstract int ChoosePixelFormat(Handle hDC, PIXELFORMATDESCRIPTOR ppfd);
    public abstract void SetPixelFormat(Handle hDC, int format, Win32.PIXELFORMATDESCRIPTOR ppfd);
    public abstract int DescribePixelFormat(Handle hDC, int iPixelFormat, Win32.PIXELFORMATDESCRIPTOR ppfd);
    public abstract void SwapBuffers(Handle hDC);

    public RuntimeException lasterror() {
	return(new StdError(GetLastError()));
    }

    static MemorySegment nhandle(Handle h) {
	if(h == null)
	    return(MemorySegment.NULL);
	return(h.bits);
    }

    static class Win64Unicode extends Win32 {
	static final Charset C_WCHARSET = Charset.forName(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "UTF-16LE" : "UTF-16BE");
	static final MemoryLayout WCHAR = ValueLayout.JAVA_SHORT;
	static final MemoryLayout BOOL = C_INT;
	static final MemoryLayout BYTE = C_CHAR;
	static final MemoryLayout SHORT = C_SHORT;
	static final MemoryLayout UINT = C_INT;
	static final MemoryLayout LONG = C_LONG;
	static final MemoryLayout WORD = C_SHORT;
	static final MemoryLayout DWORD = C_LONG;
	static final MemoryLayout PVOID = ADDRESS;
	static final MemoryLayout LPVOID = ADDRESS;
	static final MemoryLayout LONG_PTR = PTRINT_T;
	static final MemoryLayout INT_PTR = PTRINT_T;
	static final MemoryLayout ULONG_PTR = PTRINT_T;
	static final MemoryLayout UINT_PTR = PTRINT_T;
	static final MemoryLayout HANDLE = PVOID;
	static final MemoryLayout HBRUSH = HANDLE;
	static final MemoryLayout HCURSOR = HANDLE;
	static final MemoryLayout HDC = HANDLE;
	static final MemoryLayout HICON = HANDLE;
	static final MemoryLayout HINSTANCE = HANDLE;
	static final MemoryLayout HKL = HANDLE;
	static final MemoryLayout HMENU = HANDLE;
	static final MemoryLayout HWND = HANDLE;
	static final MemoryLayout ATOM = WORD;
	static final MemoryLayout WNDPROC = HANDLE;
	static final MemoryLayout LPWSTR = ADDRESS;
	static final MemoryLayout LPCWSTR = ADDRESS;
	static final MemoryLayout LRESULT = LONG_PTR;
	static final MemoryLayout WPARAM = UINT_PTR;
	static final MemoryLayout LPARAM = LONG_PTR;
	private final SymbolLookup kernel32 = SymbolLookup.libraryLookup("KERNEL32.DLL", Arena.global());
	private final SymbolLookup user32 = SymbolLookup.libraryLookup("USER32.DLL", Arena.global());
	private final SymbolLookup gdi32 = SymbolLookup.libraryLookup("GDI32.DLL", Arena.global());

	static MemorySegment wstr(Arena st, String str) {
	    if(str == null)
		return(MemorySegment.NULL);
	    return(st.allocateFrom(str, C_WCHARSET));
	}

	private final MethodHandle GetCurrentThreadId = ld.downcallHandle(kernel32.find("GetCurrentThreadId").get(), FunctionDescriptor.of(DWORD));
	public int GetCurrentThreadId() {
	    try {
		return((int)GetCurrentThreadId.invoke());
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle GetLastError = ld.downcallHandle(kernel32.find("GetLastError").get(), FunctionDescriptor.of(DWORD));
	public int GetLastError() {
	    try {
		return((int)GetLastError.invoke());
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle GetModuleHandleW = ld.downcallHandle(kernel32.find("GetModuleHandleW").get(), FunctionDescriptor.of(HINSTANCE, LPCWSTR));
	public Handle GetModuleHandle(String lpModuleName) {
	    try(Arena st = Arena.ofConfined()) {
		MemorySegment rv;
		try {
		    rv = (MemorySegment)GetModuleHandleW.invoke(wstr(st, lpModuleName));
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(nullp(rv))
		    throw(lasterror());
		return(Handle.of(rv));
	    }
	}

	static final StructLayout _WndClassExW = struct(new MemoryLayout[] {
		UINT.withName("cbSize"),
		UINT.withName("style"),
		WNDPROC.withName("lpfnWndProc"),
		C_INT.withName("cbClsExtra"),
		C_INT.withName("cbWndExtra"),
		HINSTANCE.withName("hInstance"),
		HICON.withName("hIcon"),
		HCURSOR.withName("hCursor"),
		HBRUSH.withName("hbrBackground"),
		LPCWSTR.withName("lpszMenuName"),
		LPCWSTR.withName("lpszClassName"),
		HICON.withName("hIconSm"),
	    });
	public static class WndClassExW extends Win32.WndClassEx {
	    private final Arena alloc;

	    private WndClassExW(Arena alloc) {
		super(alloc.allocate(_WndClassExW.byteSize()));
		this.alloc = alloc;
		cbSize.set(mem, 0, (int)_WndClassExW.byteSize());
	    }
	    protected WndClassExW() {
		this(Arena.ofAuto());
	    }

	    protected StructLayout $layout() {return(_WndClassExW);}

	    static long handle(WndProc wndproc, MemorySegment hwnd, int umsg, long wparam, long lparam) {
		return(wndproc.handle(Handle.of(hwnd), umsg, wparam, lparam));
	    }
	    static final MethodHandle handle = slookup(MethodHandles.lookup(), WndClassExW.class, "handle", Long.TYPE,
						       WndProc.class, MemorySegment.class, Integer.TYPE, Long.TYPE, Long.TYPE);

	    private static final VarHandle cbSize = _WndClassExW.varHandle(PathElement.groupElement("cbSize"));
	    private static final VarHandle style = _WndClassExW.varHandle(PathElement.groupElement("style"));
	    public WndClassExW style(int value) {style.set(mem, 0, value); return(this);}
	    private static final VarHandle hInstance = _WndClassExW.varHandle(PathElement.groupElement("hInstance"));
	    public WndClassExW hInstance(Handle value) {hInstance.set(mem, 0, nhandle(value)); return(this);}
	    private static final VarHandle lpszClassName = _WndClassExW.varHandle(PathElement.groupElement("lpszClassName"));
	    public WndClassExW lpszClassName(String value) {
		lpszClassName.set(mem, 0, wstr(alloc, value));
		return(this);
	    }

	    private static final VarHandle lpfnWndProc = _WndClassExW.varHandle(PathElement.groupElement("lpfnWndProc"));
	    private MemorySegment handlerp;
	    public WndClassExW lpfnWndProc(WndProc value) {
		MethodHandle handler = MethodHandles.insertArguments(handle, 0, value);
		handlerp = ld.upcallStub(handler, FunctionDescriptor.of(LRESULT, ADDRESS, UINT, WPARAM, LPARAM), alloc);
		lpfnWndProc.set(mem, 0, handlerp);
		return(this);
	    }
	}

	public WndClassExW WndClassEx() {
	    return(new WndClassExW());
	}

	private final MethodHandle RegisterClassExW = ld.downcallHandle(user32.find("RegisterClassExW").get(), FunctionDescriptor.of(ATOM, ADDRESS));
	public int RegisterClassEx(WndClassEx cls) {
	    try(Arena st = Arena.ofConfined()) {
		int rv;
		try {
		    rv = (short)RegisterClassExW.invoke(cls.mem()) & 0xffff;
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(rv == 0)
		    throw(lasterror());
		return(rv);
	    }
	}

	private final MethodHandle UnregisterClassW = ld.downcallHandle(user32.find("UnregisterClassW").get(), FunctionDescriptor.of(BOOL, LPCWSTR, HINSTANCE));
	public void UnregisterClass(String lpClassName, Handle hInstance) {
	    try(Arena st = Arena.ofConfined()) {
		int rv;
		try {
		    rv = (int)UnregisterClassW.invoke(wstr(st, lpClassName), hInstance.bits);
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(rv == 0)
		    throw(lasterror());
	    }
	}

	private final MethodHandle CreateWindowExW = ld.downcallHandle(user32.find("CreateWindowExW").get(), FunctionDescriptor.of(HWND, DWORD, LPCWSTR, LPCWSTR, DWORD, C_INT, C_INT, C_INT, C_INT, HWND, HMENU, HINSTANCE, LPVOID));
	public Handle CreateWindowEx(int dwExStyle, String lpClassName, String lpWindowName, int dwStyle, Coord pos, Coord size, Handle hWndParent, Handle hMenu, Handle hInstance) {
	    try(Arena st = Arena.ofConfined()) {
		MemorySegment rv;
		try {
		    rv = (MemorySegment)CreateWindowExW.invoke(dwExStyle, wstr(st, lpClassName), wstr(st, lpWindowName), dwStyle,
							       (pos == null) ? CW_USEDEFAULT : pos.x, (pos == null) ? CW_USEDEFAULT : pos.y,
							       (size == null) ? CW_USEDEFAULT : size.x, (size == null) ? CW_USEDEFAULT : size.y,
							       nhandle(hWndParent), nhandle(hMenu), nhandle(hInstance), MemorySegment.NULL);
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(nullp(rv))
		    throw(lasterror());
		return(Handle.of(rv));
	    }
	}

	private final MethodHandle DestroyWindow = ld.downcallHandle(user32.find("DestroyWindow").get(), FunctionDescriptor.of(BOOL, HWND));
	public int DestroyWindow(Handle hWnd) {
	    int rv;
	    try {
		rv = (int)DestroyWindow.invoke(hWnd.bits);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	    return(rv);
	}

	static final StructLayout POINT = struct(new MemoryLayout[] {
		LONG.withName("x"),
		LONG.withName("y"),
	    });
	static final StructLayout _MSG = struct(new MemoryLayout[] {
		HWND.withName("hwnd"),
		UINT.withName("message"),
		WPARAM.withName("wparam"),
		LPARAM.withName("lparam"),
		DWORD.withName("time"),
		POINT.withName("pt"),
		DWORD.withName("lPrivate"),
	    });
	public static class MSG extends Win32.MSG {
	    private MSG(Arena alloc) {
		super(alloc.allocate(_MSG.byteSize()));
	    }
	    protected MSG() {
		this(Arena.ofAuto());
	    }

	    protected StructLayout $layout() {return(_MSG);}
	}

	public MSG MSG() {
	    return(new MSG());
	}

	private final MethodHandle GetMessageW = ld.downcallHandle(user32.find("GetMessageW").get(), FunctionDescriptor.of(BOOL, ADDRESS, HWND, UINT, UINT));
	public boolean GetMessage(Win32.MSG lpMsg, Handle hWnd, int wMsgFilterMin, int wMsgFilterMax) {
	    int rv;
	    try {
		rv = (int)GetMessageW.invoke(lpMsg.mem(), nhandle(hWnd), wMsgFilterMin, wMsgFilterMax);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == -1)
		throw(lasterror());
	    return(rv != 0);
	}

	private final MethodHandle PeekMessageW = ld.downcallHandle(user32.find("PeekMessageW").get(), FunctionDescriptor.of(BOOL, ADDRESS, HWND, UINT, UINT, UINT));
	public boolean PeekMessage(Win32.MSG lpMsg, Handle hWnd, int wMsgFilterMin, int wMsgFilterMax, int wRemoveMsg) {
	    int rv;
	    try {
		rv = (int)PeekMessageW.invoke(lpMsg.mem(), nhandle(hWnd), wMsgFilterMin, wMsgFilterMax, wRemoveMsg);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    return(rv != 0);
	}

	private final MethodHandle DispatchMessageW = ld.downcallHandle(user32.find("DispatchMessageW").get(), FunctionDescriptor.of(LRESULT, ADDRESS));
	public long DispatchMessage(Win32.MSG lpMsg) {
	    try {
		return((long)DispatchMessageW.invoke(lpMsg.mem()));
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle PostMessageW = ld.downcallHandle(user32.find("PostMessageW").get(), FunctionDescriptor.of(BOOL, HWND, UINT, WPARAM, LPARAM));
	public void PostMessage(Handle hWnd, int Msg, long wParam, long lParam) {
	    int rv;
	    try {
		rv = (int)PostMessageW.invoke(nhandle(hWnd), Msg, wParam, lParam);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}

	private final MethodHandle PostThreadMessageW = ld.downcallHandle(user32.find("PostThreadMessageW").get(), FunctionDescriptor.of(BOOL, DWORD, UINT, WPARAM, LPARAM));
	public void PostThreadMessage(int idThread, int Msg, long wParam, long lParam) {
	    int rv;
	    try {
		rv = (int)PostThreadMessageW.invoke(idThread, Msg, wParam, lParam);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}

	private final MethodHandle DefWindowProcW = ld.downcallHandle(user32.find("DefWindowProcW").get(), FunctionDescriptor.of(LRESULT, HWND, UINT, WPARAM, LPARAM));
	public long DefWindowProc(Handle hWnd, int Msg, long wParam, long lParam) {
	    try {
		return((long)DefWindowProcW.invoke(nhandle(hWnd), Msg, wParam, lParam));
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle ShowWindow = ld.downcallHandle(user32.find("ShowWindow").get(), FunctionDescriptor.of(BOOL, HWND, C_INT));
	public boolean ShowWindow(Handle hWnd, int nCmdShow) {
	    try {
		return((int)ShowWindow.invoke(hWnd.bits, nCmdShow) != 0);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle SetWindowTextW = ld.downcallHandle(user32.find("SetWindowTextW").get(), FunctionDescriptor.of(BOOL, HWND, LPCWSTR));
	public void SetWindowText(Handle hWnd, String lpString) {
	    int rv;
	    try(Arena st = Arena.ofConfined()) {
		rv = (int)SetWindowTextW.invoke(hWnd.bits, wstr(st, lpString));
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}

	private final MethodHandle GetDC = ld.downcallHandle(user32.find("GetDC").get(), FunctionDescriptor.of(HDC, HWND));
	public Handle GetDC(Handle hWnd) {
	    MemorySegment rv;
	    try {
		rv = (MemorySegment)GetDC.invoke(hWnd.bits);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(nullp(rv))
		throw(lasterror());
	    return(Handle.of(rv));
	}

	private final MethodHandle ReleaseDC = ld.downcallHandle(user32.find("ReleaseDC").get(), FunctionDescriptor.of(C_INT, HDC, HWND));
	public boolean ReleaseDC(Handle hWnd, Handle hDC) {
	    try {
		return((int)ReleaseDC.invoke(hWnd.bits, hDC.bits) != 0);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle GetKeyState = ld.downcallHandle(user32.find("GetKeyState").get(), FunctionDescriptor.of(SHORT, C_INT));
	public int GetKeyState(int nVirtKey) {
	    try {
		return((int)GetKeyState.invoke(nVirtKey));
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	}

	private final MethodHandle GetKeyboardState = ld.downcallHandle(user32.find("GetKeyboardState").get(), FunctionDescriptor.of(BOOL, ADDRESS));
	public void GetKeyboardState(KeyboardState buf) {
	    int rv;
	    try {
		rv = (int)GetKeyboardState.invoke(buf.keys);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}

	private final MethodHandle GetKeyboardLayout = ld.downcallHandle(user32.find("GetKeyboardLayout").get(), FunctionDescriptor.of(HKL, DWORD));
	public Handle GetKeyboardLayout(int idThread) {
	    MemorySegment rv;
	    try {
		rv = (MemorySegment)GetKeyboardLayout.invoke(idThread);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    return(Handle.of(rv));
	}

	private final MethodHandle ToUnicodeEx = ld.downcallHandle(user32.find("ToUnicodeEx").get(), FunctionDescriptor.of(C_INT, UINT, UINT, ADDRESS, LPWSTR, C_INT, UINT, HKL));
	public char[] ToUnicodeEx(int wVirtKey, int wScanCode, KeyboardState lpKeyState, int wFlags, Handle dwhkl) {
	    try(Arena st = Arena.ofConfined()) {
		MemorySegment buf = st.allocate(WCHAR.byteSize() * 128);
		int rv;
		try {
		    rv = (int)ToUnicodeEx.invoke(wVirtKey, wScanCode, lpKeyState.keys, buf, 128, wFlags, dwhkl.bits);
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(rv < 0)
		    return(null);
		char[] ret = new char[rv];
		for(int i = 0; i < rv; i++)
		    ret[i] = (char)getint(buf, WCHAR.byteSize() * i, WCHAR, false);
		return(ret);
	    }
	}

	private final MethodHandle GetKeyNameTextW = ld.downcallHandle(user32.find("GetKeyNameTextW").get(), FunctionDescriptor.of(C_INT, LONG, LPWSTR, C_INT));
	public String GetKeyNameText(long lParam) {
	    try(Arena st = Arena.ofConfined()) {
		MemorySegment buf = st.allocate(WCHAR.byteSize() * 128);
		int rv;
		try {
		    rv = (int)GetKeyNameTextW.invoke((int)lParam, buf, 128);
		} catch(Throwable e) {
		    throw(new RuntimeException(e));
		}
		if(rv <= 0)
		    return(null);
		char[] ret = new char[rv];
		for(int i = 0; i < rv; i++)
		    ret[i] = (char)getint(buf, WCHAR.byteSize() * i, WCHAR, false);
		return(new String(ret));
	    }
	}

	static final StructLayout _PIXELFORMATDESCRIPTOR = struct(new MemoryLayout[] {
		WORD.withName("nSize"),
		WORD.withName("nVersion"),
		DWORD.withName("dwFlags"),
		BYTE.withName("iPixelType"),
		BYTE.withName("cColorBits"),
		BYTE.withName("cRedBits"),
		BYTE.withName("cRedShift"),
		BYTE.withName("cGreenBits"),
		BYTE.withName("cGreenShift"),
		BYTE.withName("cBlueBits"),
		BYTE.withName("cBlueShift"),
		BYTE.withName("cAlphaBits"),
		BYTE.withName("cAlphaShift"),
		BYTE.withName("cAccumBits"),
		BYTE.withName("cAccumRedBits"),
		BYTE.withName("cAccumGreenBits"),
		BYTE.withName("cAccumBlueBits"),
		BYTE.withName("cAccumAlphaBits"),
		BYTE.withName("cDepthBits"),
		BYTE.withName("cStencilBits"),
		BYTE.withName("cAuxBuffers"),
		BYTE.withName("iLayerType"),
		BYTE.withName("bReserved"),
		DWORD.withName("dwLayerMask"),
		DWORD.withName("dwVisibleMask"),
		DWORD.withName("dwDamageMask"),
	    });
	public static class PIXELFORMATDESCRIPTOR extends Win32.PIXELFORMATDESCRIPTOR {
	    private PIXELFORMATDESCRIPTOR(Arena alloc) {
		super(alloc.allocate(_PIXELFORMATDESCRIPTOR.byteSize()));
		nSize.set(mem, 0, (short)_PIXELFORMATDESCRIPTOR.byteSize());
	    }
	    protected PIXELFORMATDESCRIPTOR() {
		this(Arena.ofAuto());
	    }

	    protected StructLayout $layout() {return(_PIXELFORMATDESCRIPTOR);}

	    private static final VarHandle nSize = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("nSize"));
	    private static final VarHandle nVersion = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("nVersion"));
	    public PIXELFORMATDESCRIPTOR nVersion(int value) {nVersion.set(mem, 0, (short)value); return(this);}
	    private static final VarHandle dwFlags = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("dwFlags"));
	    public PIXELFORMATDESCRIPTOR dwFlags(int value) {dwFlags.set(mem, 0, value); return(this);}
	    private static final VarHandle iPixelType = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("iPixelType"));
	    public PIXELFORMATDESCRIPTOR iPixelType(int value) {iPixelType.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cColorBits = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cColorBits"));
	    public PIXELFORMATDESCRIPTOR cColorBits(int value) {cColorBits.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cAlphaBits = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cAlphaBits"));
	    public PIXELFORMATDESCRIPTOR cAlphaBits(int value) {cAlphaBits.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cAccumBits = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cAccumBits"));
	    public PIXELFORMATDESCRIPTOR cAccumBits(int value) {cAccumBits.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cDepthBits = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cDepthBits"));
	    public PIXELFORMATDESCRIPTOR cDepthBits(int value) {cDepthBits.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cStencilBits = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cStencilBits"));
	    public PIXELFORMATDESCRIPTOR cStencilBits(int value) {cStencilBits.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle cAuxBuffers = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("cAuxBuffers"));
	    public PIXELFORMATDESCRIPTOR cAuxBuffers(int value) {cAuxBuffers.set(mem, 0, (byte)value); return(this);}
	    private static final VarHandle iLayerType = _PIXELFORMATDESCRIPTOR.varHandle(PathElement.groupElement("iLayerType"));
	    public PIXELFORMATDESCRIPTOR iLayerType(int value) {iLayerType.set(mem, 0, (byte)value); return(this);}
	}

	public PIXELFORMATDESCRIPTOR PIXELFORMATDESCRIPTOR() {
	    return(new PIXELFORMATDESCRIPTOR());
	}

	private final MethodHandle ChoosePixelFormat = ld.downcallHandle(gdi32.find("ChoosePixelFormat").get(), FunctionDescriptor.of(C_INT, HDC, ADDRESS));
	public int ChoosePixelFormat(Handle hDC, Win32.PIXELFORMATDESCRIPTOR ppfd) {
	    int rv;
	    try {
		rv = (int)ChoosePixelFormat.invoke(hDC.bits, ppfd.mem());
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	    return(rv);
	}

	private final MethodHandle SetPixelFormat = ld.downcallHandle(gdi32.find("SetPixelFormat").get(), FunctionDescriptor.of(BOOL, HDC, C_INT, ADDRESS));
	public void SetPixelFormat(Handle hDC, int format, Win32.PIXELFORMATDESCRIPTOR ppfd) {
	    int rv;
	    try {
		rv = (int)SetPixelFormat.invoke(hDC.bits, format, ppfd.mem());
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}

	private final MethodHandle DescribePixelFormat = ld.downcallHandle(gdi32.find("DescribePixelFormat").get(), FunctionDescriptor.of(C_INT, HDC, C_INT, UINT, ADDRESS));
	public int DescribePixelFormat(Handle hDC, int iPixelFormat, Win32.PIXELFORMATDESCRIPTOR ppfd) {
	    int rv;
	    try {
		rv = (int)DescribePixelFormat.invoke(hDC.bits, iPixelFormat, (int)ppfd.mem().byteSize(), ppfd.mem());
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	    return(rv);
	}

	private final MethodHandle SwapBuffers = ld.downcallHandle(gdi32.find("SwapBuffers").get(), FunctionDescriptor.of(BOOL, HDC));
	public void SwapBuffers(Handle hDC) {
	    int rv;
	    try {
		rv = (int)SwapBuffers.invoke(hDC.bits);
	    } catch(Throwable e) {
		throw(new RuntimeException(e));
	    }
	    if(rv == 0)
		throw(lasterror());
	}
    }

    private static Win32 instance = null;
    public static Win32 get() {
	if(instance == null) {
	    synchronized(Win32.class) {
		if(instance == null) {
		    instance = new Win64Unicode();
		}
	    }
	}
	return(instance);
    }
}
