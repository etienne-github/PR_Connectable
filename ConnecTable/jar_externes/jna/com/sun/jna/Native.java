// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Native.java

package com.sun.jna;

import java.awt.Component;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

// Referenced classes of package com.sun.jna:
//            Pointer, Library, TypeMapper, Structure, 
//            ToNativeConverter, FunctionMapper, FromNativeContext, NativeMapped, 
//            ToNativeContext, NativeLibrary, CallbackReference, Platform, 
//            NativeMappedConverter, FromNativeConverter, Function, Callback, 
//            CallbackThreadInitializer

public final class Native
{
    private static class AWT
    {

        static long getWindowID(Window w)
            throws HeadlessException
        {
            return getComponentID(w);
        }

        static long getComponentID(Object o)
            throws HeadlessException
        {
            if(GraphicsEnvironment.isHeadless())
                throw new HeadlessException("No native windows when headless");
            Component c = (Component)o;
            if(c.isLightweight())
                throw new IllegalArgumentException("Component must be heavyweight");
            if(!c.isDisplayable())
                throw new IllegalStateException("Component must be displayable");
            if(Platform.isX11() && System.getProperty("java.version").startsWith("1.4") && !c.isVisible())
                throw new IllegalStateException("Component must be visible");
            else
                return Native.getWindowHandle0(c);
        }

        private AWT()
        {
        }
    }

    private static class Buffers
    {

        static boolean isBuffer(Class cls)
        {
            return (Native.class$java$nio$Buffer != null ? Native.class$java$nio$Buffer : (Native.class$java$nio$Buffer = Native._mthclass$("java.nio.Buffer"))).isAssignableFrom(cls);
        }

        private Buffers()
        {
        }
    }

    public static interface ffi_callback
    {

        public abstract void invoke(long l, long l1, long l2);
    }


    private static void dispose()
    {
        NativeLibrary.disposeAll();
        nativeLibraryPath = null;
    }

    private static boolean deleteNativeLibrary(String path)
    {
        File flib = new File(path);
        if(flib.delete())
        {
            return true;
        } else
        {
            markTemporaryFile(flib);
            return false;
        }
    }

    private Native()
    {
    }

    private static native void initIDs();

    public static synchronized native void setProtected(boolean flag);

    public static synchronized native boolean isProtected();

    /**
     * @deprecated Method setPreserveLastError is deprecated
     */

    public static synchronized native void setPreserveLastError(boolean flag);

    /**
     * @deprecated Method getPreserveLastError is deprecated
     */

    public static synchronized native boolean getPreserveLastError();

    public static long getWindowID(Window w)
        throws HeadlessException
    {
        return AWT.getWindowID(w);
    }

    public static long getComponentID(Component c)
        throws HeadlessException
    {
        return AWT.getComponentID(c);
    }

    public static Pointer getWindowPointer(Window w)
        throws HeadlessException
    {
        return new Pointer(AWT.getWindowID(w));
    }

    public static Pointer getComponentPointer(Component c)
        throws HeadlessException
    {
        return new Pointer(AWT.getComponentID(c));
    }

    static native long getWindowHandle0(Component component);

    public static Pointer getDirectBufferPointer(Buffer b)
    {
        long peer = _getDirectBufferPointer(b);
        return peer != 0L ? new Pointer(peer) : null;
    }

    private static native long _getDirectBufferPointer(Buffer buffer);

    public static String toString(byte buf[])
    {
        return toString(buf, System.getProperty("jna.encoding"));
    }

    public static String toString(byte buf[], String encoding)
    {
        String s = null;
        if(encoding != null)
            try
            {
                s = new String(buf, encoding);
            }
            catch(UnsupportedEncodingException e) { }
        if(s == null)
            s = new String(buf);
        int term = s.indexOf('\0');
        if(term != -1)
            s = s.substring(0, term);
        return s;
    }

    public static String toString(char buf[])
    {
        String s = new String(buf);
        int term = s.indexOf('\0');
        if(term != -1)
            s = s.substring(0, term);
        return s;
    }

    public static Object loadLibrary(Class interfaceClass)
    {
        return loadLibrary(((String) (null)), interfaceClass);
    }

    public static Object loadLibrary(Class interfaceClass, Map options)
    {
        return loadLibrary(null, interfaceClass, options);
    }

    public static Object loadLibrary(String name, Class interfaceClass)
    {
        return loadLibrary(name, interfaceClass, Collections.EMPTY_MAP);
    }

    public static Object loadLibrary(String name, Class interfaceClass, Map options)
    {
        Library.Handler handler = new Library.Handler(name, interfaceClass, options);
        ClassLoader loader = interfaceClass.getClassLoader();
        Library proxy = (Library)Proxy.newProxyInstance(loader, new Class[] {
            interfaceClass
        }, handler);
        cacheOptions(interfaceClass, options, proxy);
        return proxy;
    }

    private static void loadLibraryInstance(Class cls)
    {
        if(cls != null && !libraries.containsKey(cls))
            try
            {
                Field fields[] = cls.getFields();
                int i = 0;
                do
                {
                    if(i >= fields.length)
                        break;
                    Field field = fields[i];
                    if(field.getType() == cls && Modifier.isStatic(field.getModifiers()))
                    {
                        libraries.put(cls, new WeakReference(field.get(null)));
                        break;
                    }
                    i++;
                } while(true);
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException("Could not access instance of " + cls + " (" + e + ")");
            }
    }

    static Class findEnclosingLibraryClass(Class cls)
    {
        if(cls == null)
            return null;
        Map map = libraries;
        JVM INSTR monitorenter ;
        if(options.containsKey(cls))
            return cls;
        map;
        JVM INSTR monitorexit ;
          goto _L1
        Exception exception;
        exception;
        throw exception;
_L1:
        if((com.sun.jna.Library.class).isAssignableFrom(cls))
            return cls;
        if((com.sun.jna.Callback.class).isAssignableFrom(cls))
            cls = CallbackReference.findCallbackClass(cls);
        Class declaring = cls.getDeclaringClass();
        Class fromDeclaring = findEnclosingLibraryClass(declaring);
        if(fromDeclaring != null)
            return fromDeclaring;
        else
            return findEnclosingLibraryClass(cls.getSuperclass());
    }

    public static Map getLibraryOptions(Class type)
    {
        Map map = libraries;
        JVM INSTR monitorenter ;
        Class interfaceClass = findEnclosingLibraryClass(type);
        if(interfaceClass != null)
            loadLibraryInstance(interfaceClass);
        else
            interfaceClass = type;
        if(!options.containsKey(interfaceClass))
            try
            {
                Field field = interfaceClass.getField("OPTIONS");
                field.setAccessible(true);
                options.put(interfaceClass, field.get(null));
            }
            catch(NoSuchFieldException e) { }
            catch(Exception e)
            {
                throw new IllegalArgumentException("OPTIONS must be a public field of type java.util.Map (" + e + "): " + interfaceClass);
            }
        return (Map)options.get(interfaceClass);
        Exception exception;
        exception;
        throw exception;
    }

    public static TypeMapper getTypeMapper(Class cls)
    {
        Map map = libraries;
        JVM INSTR monitorenter ;
        Class interfaceClass = findEnclosingLibraryClass(cls);
        if(interfaceClass != null)
            loadLibraryInstance(interfaceClass);
        else
            interfaceClass = cls;
        if(!typeMappers.containsKey(interfaceClass))
            try
            {
                Field field = interfaceClass.getField("TYPE_MAPPER");
                field.setAccessible(true);
                typeMappers.put(interfaceClass, field.get(null));
            }
            catch(NoSuchFieldException e)
            {
                Map options = getLibraryOptions(cls);
                if(options != null && options.containsKey("type-mapper"))
                    typeMappers.put(interfaceClass, options.get("type-mapper"));
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException("TYPE_MAPPER must be a public field of type " + (com.sun.jna.TypeMapper.class).getName() + " (" + e + "): " + interfaceClass);
            }
        return (TypeMapper)typeMappers.get(interfaceClass);
        Exception exception;
        exception;
        throw exception;
    }

    public static int getStructureAlignment(Class cls)
    {
        Map map = libraries;
        JVM INSTR monitorenter ;
        Class interfaceClass = findEnclosingLibraryClass(cls);
        if(interfaceClass != null)
            loadLibraryInstance(interfaceClass);
        else
            interfaceClass = cls;
        if(!alignments.containsKey(interfaceClass))
            try
            {
                Field field = interfaceClass.getField("STRUCTURE_ALIGNMENT");
                field.setAccessible(true);
                alignments.put(interfaceClass, field.get(null));
            }
            catch(NoSuchFieldException e)
            {
                Map options = getLibraryOptions(interfaceClass);
                if(options != null && options.containsKey("structure-alignment"))
                    alignments.put(interfaceClass, options.get("structure-alignment"));
            }
            catch(Exception e)
            {
                throw new IllegalArgumentException("STRUCTURE_ALIGNMENT must be a public field of type int (" + e + "): " + interfaceClass);
            }
        Integer value = (Integer)alignments.get(interfaceClass);
        return value == null ? 0 : value.intValue();
        Exception exception;
        exception;
        throw exception;
    }

    static byte[] getBytes(String s)
    {
        try
        {
            return getBytes(s, System.getProperty("jna.encoding"));
        }
        catch(UnsupportedEncodingException e)
        {
            return s.getBytes();
        }
    }

    static byte[] getBytes(String s, String encoding)
        throws UnsupportedEncodingException
    {
        if(encoding != null)
            return s.getBytes(encoding);
        else
            return s.getBytes();
    }

    public static byte[] toByteArray(String s)
    {
        byte bytes[] = getBytes(s);
        byte buf[] = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        return buf;
    }

    public static byte[] toByteArray(String s, String encoding)
        throws UnsupportedEncodingException
    {
        byte bytes[] = getBytes(s, encoding);
        byte buf[] = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, buf, 0, bytes.length);
        return buf;
    }

    public static char[] toCharArray(String s)
    {
        char chars[] = s.toCharArray();
        char buf[] = new char[chars.length + 1];
        System.arraycopy(chars, 0, buf, 0, chars.length);
        return buf;
    }

    static String getNativeLibraryResourcePath(int osType, String arch, String name)
    {
        arch = arch.toLowerCase();
        if("powerpc".equals(arch))
            arch = "ppc";
        else
        if("powerpc64".equals(arch))
            arch = "ppc64";
        String osPrefix;
        switch(osType)
        {
        case 2: // '\002'
            if("i386".equals(arch))
                arch = "x86";
            osPrefix = "win32-" + arch;
            break;

        case 6: // '\006'
            osPrefix = "w32ce-" + arch;
            break;

        case 0: // '\0'
            osPrefix = "darwin";
            break;

        case 1: // '\001'
            if("x86".equals(arch))
                arch = "i386";
            else
            if("x86_64".equals(arch))
                arch = "amd64";
            osPrefix = "linux-" + arch;
            break;

        case 3: // '\003'
            osPrefix = "sunos-" + arch;
            break;

        case 4: // '\004'
        case 5: // '\005'
        default:
            osPrefix = name.toLowerCase();
            if("x86".equals(arch))
                arch = "i386";
            if("x86_64".equals(arch))
                arch = "amd64";
            int space = osPrefix.indexOf(" ");
            if(space != -1)
                osPrefix = osPrefix.substring(0, space);
            osPrefix = osPrefix + "-" + arch;
            break;
        }
        return "/com/sun/jna/" + osPrefix;
    }

    private static void loadNativeLibrary()
    {
        removeTemporaryFiles();
        String libName = System.getProperty("jna.boot.library.name", "jnidispatch");
        String bootPath = System.getProperty("jna.boot.library.path");
        if(bootPath != null)
        {
            StringTokenizer dirs = new StringTokenizer(bootPath, File.pathSeparator);
            do
            {
                if(!dirs.hasMoreTokens())
                    break;
                String dir = dirs.nextToken();
                File file = new File(new File(dir), System.mapLibraryName(libName));
                String path = file.getAbsolutePath();
                if(file.exists())
                    try
                    {
                        System.load(path);
                        nativeLibraryPath = path;
                        return;
                    }
                    catch(UnsatisfiedLinkError ex) { }
                if(Platform.isMac())
                {
                    String orig;
                    String ext;
                    if(path.endsWith("dylib"))
                    {
                        orig = "dylib";
                        ext = "jnilib";
                    } else
                    {
                        orig = "jnilib";
                        ext = "dylib";
                    }
                    path = path.substring(0, path.lastIndexOf(orig)) + ext;
                    if((new File(path)).exists())
                        try
                        {
                            System.load(path);
                            nativeLibraryPath = path;
                            return;
                        }
                        catch(UnsatisfiedLinkError ex)
                        {
                            System.err.println("File found at " + path + " but not loadable: " + ex.getMessage());
                        }
                }
            } while(true);
        }
        try
        {
            if(!Boolean.getBoolean("jna.nosys"))
            {
                System.loadLibrary(libName);
                return;
            }
        }
        catch(UnsatisfiedLinkError e)
        {
            if(Boolean.getBoolean("jna.nounpack"))
                throw e;
        }
        if(!Boolean.getBoolean("jna.nounpack"))
        {
            loadNativeLibraryFromJar();
            return;
        } else
        {
            throw new UnsatisfiedLinkError("Native jnidispatch library not found");
        }
    }

    private static void loadNativeLibraryFromJar()
    {
        boolean unpacked;
        File lib;
        InputStream is;
        FileOutputStream fos;
        String libname = System.mapLibraryName("jnidispatch");
        String arch = System.getProperty("os.arch");
        String name = System.getProperty("os.name");
        String resourceName = getNativeLibraryResourcePath(Platform.getOSType(), arch, name) + "/" + libname;
        URL url = (com.sun.jna.Native.class).getResource(resourceName);
        unpacked = false;
        if(url == null && Platform.isMac() && resourceName.endsWith(".dylib"))
        {
            resourceName = resourceName.substring(0, resourceName.lastIndexOf(".dylib")) + ".jnilib";
            url = (com.sun.jna.Native.class).getResource(resourceName);
        }
        if(url == null)
            throw new UnsatisfiedLinkError("jnidispatch (" + resourceName + ") not found in resource path");
        lib = null;
        if(url.getProtocol().toLowerCase().equals("file"))
        {
            try
            {
                lib = new File(new URI(url.toString()));
            }
            catch(URISyntaxException e)
            {
                lib = new File(url.getPath());
            }
            if(!lib.exists())
                throw new Error("File URL " + url + " could not be properly decoded");
            break MISSING_BLOCK_LABEL_508;
        }
        is = (com.sun.jna.Native.class).getResourceAsStream(resourceName);
        if(is == null)
            throw new Error("Can't obtain jnidispatch InputStream");
        fos = null;
        try
        {
            File dir = getTempDir();
            lib = File.createTempFile("jna", Platform.isWindows() ? ".dll" : null, dir);
            lib.deleteOnExit();
            fos = new FileOutputStream(lib);
            byte buf[] = new byte[1024];
            int count;
            while((count = is.read(buf, 0, buf.length)) > 0) 
                fos.write(buf, 0, count);
            unpacked = true;
        }
        catch(IOException e)
        {
            throw new Error("Failed to create temporary file for jnidispatch library: " + e);
        }
        try
        {
            is.close();
        }
        catch(IOException e) { }
        if(fos != null)
            try
            {
                fos.close();
            }
            catch(IOException e) { }
        break MISSING_BLOCK_LABEL_508;
        Exception exception;
        exception;
        try
        {
            is.close();
        }
        catch(IOException e) { }
        if(fos != null)
            try
            {
                fos.close();
            }
            catch(IOException e) { }
        throw exception;
        System.load(lib.getAbsolutePath());
        nativeLibraryPath = lib.getAbsolutePath();
        if(unpacked)
            deleteNativeLibrary(lib.getAbsolutePath());
        return;
    }

    private static native int sizeof(int i);

    private static native String getNativeVersion();

    private static native String getAPIChecksum();

    public static int getLastError()
    {
        return ((Integer)lastError.get()).intValue();
    }

    public static native void setLastError(int i);

    static void updateLastError(int e)
    {
        lastError.set(new Integer(e));
    }

    public static Library synchronizedLibrary(Library library)
    {
        Class cls = library.getClass();
        if(!Proxy.isProxyClass(cls))
            throw new IllegalArgumentException("Library must be a proxy class");
        InvocationHandler ih = Proxy.getInvocationHandler(library);
        if(!(ih instanceof Library.Handler))
        {
            throw new IllegalArgumentException("Unrecognized proxy handler: " + ih);
        } else
        {
            Library.Handler handler = (Library.Handler)ih;
            InvocationHandler newHandler = new InvocationHandler(handler, library) {

                public Object invoke(Object proxy, Method method, Object args[])
                    throws Throwable
                {
                    NativeLibrary nativelibrary = handler.getNativeLibrary();
                    JVM INSTR monitorenter ;
                    return handler.invoke(library, method, args);
                    Exception exception;
                    exception;
                    throw exception;
                }

            
            {
                super();
            }
            }
;
            return (Library)Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), newHandler);
        }
    }

    public static String getWebStartLibraryPath(String libName)
    {
        if(System.getProperty("javawebstart.version") == null)
            return null;
        try
        {
            ClassLoader cl = (com.sun.jna.Native.class).getClassLoader();
            Method m = (Method)AccessController.doPrivileged(new PrivilegedAction() {

                public Object run()
                {
                    try
                    {
                        Method m = (Native.class$java$lang$ClassLoader != null ? Native.class$java$lang$ClassLoader : (Native.class$java$lang$ClassLoader = Native._mthclass$("java.lang.ClassLoader"))).getDeclaredMethod("findLibrary", new Class[] {
                            Native.class$java$lang$String != null ? Native.class$java$lang$String : (Native.class$java$lang$String = Native._mthclass$("java.lang.String"))
                        });
                        m.setAccessible(true);
                        return m;
                    }
                    catch(Exception e)
                    {
                        return null;
                    }
                }

            }
);
            String libpath = (String)m.invoke(cl, new Object[] {
                libName
            });
            if(libpath != null)
                return (new File(libpath)).getParent();
        }
        catch(Exception e)
        {
            return null;
        }
        return null;
    }

    static void markTemporaryFile(File file)
    {
        try
        {
            File marker = new File(file.getParentFile(), file.getName() + ".x");
            marker.createNewFile();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    static File getTempDir()
    {
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File jnatmp = new File(tmp, "jna");
        jnatmp.mkdirs();
        return jnatmp.exists() ? jnatmp : tmp;
    }

    static void removeTemporaryFiles()
    {
        File dir = getTempDir();
        FilenameFilter filter = new FilenameFilter() {

            public boolean accept(File dir, String name)
            {
                return name.endsWith(".x") && name.indexOf("jna") != -1;
            }

        }
;
        File files[] = dir.listFiles(filter);
        for(int i = 0; files != null && i < files.length; i++)
        {
            File marker = files[i];
            String name = marker.getName();
            name = name.substring(0, name.length() - 2);
            File target = new File(marker.getParentFile(), name);
            if(!target.exists() || target.delete())
                marker.delete();
        }

    }

    public static int getNativeSize(Class type, Object value)
    {
        if(type.isArray())
        {
            int len = Array.getLength(value);
            if(len > 0)
            {
                Object o = Array.get(value, 0);
                return len * getNativeSize(type.getComponentType(), o);
            } else
            {
                throw new IllegalArgumentException("Arrays of length zero not allowed: " + type);
            }
        }
        if((com.sun.jna.Structure.class).isAssignableFrom(type) && !(com.sun.jna.Structure$ByReference.class).isAssignableFrom(type))
        {
            if(value == null)
                value = Structure.newInstance(type);
            return ((Structure)value).size();
        }
        try
        {
            return getNativeSize(type);
        }
        catch(IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The type \"" + type.getName() + "\" is not supported: " + e.getMessage());
        }
    }

    public static int getNativeSize(Class cls)
    {
        if((com.sun.jna.NativeMapped.class).isAssignableFrom(cls))
            cls = NativeMappedConverter.getInstance(cls).nativeType();
        if(cls == Boolean.TYPE || cls == (java.lang.Boolean.class))
            return 4;
        if(cls == Byte.TYPE || cls == (java.lang.Byte.class))
            return 1;
        if(cls == Short.TYPE || cls == (java.lang.Short.class))
            return 2;
        if(cls == Character.TYPE || cls == (java.lang.Character.class))
            return WCHAR_SIZE;
        if(cls == Integer.TYPE || cls == (java.lang.Integer.class))
            return 4;
        if(cls == Long.TYPE || cls == (java.lang.Long.class))
            return 8;
        if(cls == Float.TYPE || cls == (java.lang.Float.class))
            return 4;
        if(cls == Double.TYPE || cls == (java.lang.Double.class))
            return 8;
        if((com.sun.jna.Structure.class).isAssignableFrom(cls))
            if((com.sun.jna.Structure$ByValue.class).isAssignableFrom(cls))
                return Structure.newInstance(cls).size();
            else
                return POINTER_SIZE;
        if((com.sun.jna.Pointer.class).isAssignableFrom(cls) || Platform.HAS_BUFFERS && Buffers.isBuffer(cls) || (com.sun.jna.Callback.class).isAssignableFrom(cls) || (java.lang.String.class) == cls || (com.sun.jna.WString.class) == cls)
            return POINTER_SIZE;
        else
            throw new IllegalArgumentException("Native size for type \"" + cls.getName() + "\" is unknown");
    }

    public static boolean isSupportedNativeType(Class cls)
    {
        if((com.sun.jna.Structure.class).isAssignableFrom(cls))
            return true;
        try
        {
            return getNativeSize(cls) != 0;
        }
        catch(IllegalArgumentException e)
        {
            return false;
        }
    }

    public static void setCallbackExceptionHandler(Callback.UncaughtExceptionHandler eh)
    {
        callbackExceptionHandler = eh != null ? eh : DEFAULT_HANDLER;
    }

    public static Callback.UncaughtExceptionHandler getCallbackExceptionHandler()
    {
        return callbackExceptionHandler;
    }

    public static void register(String libName)
    {
        register(getNativeClass(getCallingClass()), NativeLibrary.getInstance(libName));
    }

    public static void register(NativeLibrary lib)
    {
        register(getNativeClass(getCallingClass()), lib);
    }

    static Class getNativeClass(Class cls)
    {
        Method methods[] = cls.getDeclaredMethods();
        for(int i = 0; i < methods.length; i++)
            if((methods[i].getModifiers() & 0x100) != 0)
                return cls;

        int idx = cls.getName().lastIndexOf("$");
        if(idx != -1)
        {
            String name = cls.getName().substring(0, idx);
            try
            {
                return getNativeClass(Class.forName(name, true, cls.getClassLoader()));
            }
            catch(ClassNotFoundException e) { }
        }
        throw new IllegalArgumentException("Can't determine class with native methods from the current context (" + cls + ")");
    }

    static Class getCallingClass()
    {
        Class context[] = (new SecurityManager() {

            public Class[] getClassContext()
            {
                return super.getClassContext();
            }

        }
).getClassContext();
        if(context.length < 4)
            throw new IllegalStateException("This method must be called from the static initializer of a class");
        else
            return context[3];
    }

    public static void setCallbackThreadInitializer(Callback cb, CallbackThreadInitializer initializer)
    {
        CallbackReference.setCallbackThreadInitializer(cb, initializer);
    }

    public static void unregister()
    {
        unregister(getNativeClass(getCallingClass()));
    }

    public static void unregister(Class cls)
    {
        synchronized(registeredClasses)
        {
            if(registeredClasses.containsKey(cls))
            {
                unregister(cls, (long[])(long[])registeredClasses.get(cls));
                registeredClasses.remove(cls);
                registeredLibraries.remove(cls);
            }
        }
    }

    private static native void unregister(Class class1, long al[]);

    private static String getSignature(Class cls)
    {
        if(cls.isArray())
            return "[" + getSignature(cls.getComponentType());
        if(cls.isPrimitive())
        {
            if(cls == Void.TYPE)
                return "V";
            if(cls == Boolean.TYPE)
                return "Z";
            if(cls == Byte.TYPE)
                return "B";
            if(cls == Short.TYPE)
                return "S";
            if(cls == Character.TYPE)
                return "C";
            if(cls == Integer.TYPE)
                return "I";
            if(cls == Long.TYPE)
                return "J";
            if(cls == Float.TYPE)
                return "F";
            if(cls == Double.TYPE)
                return "D";
        }
        return "L" + replace(".", "/", cls.getName()) + ";";
    }

    static String replace(String s1, String s2, String str)
    {
        StringBuffer buf = new StringBuffer();
        do
        {
            int idx = str.indexOf(s1);
            if(idx == -1)
            {
                buf.append(str);
                break;
            }
            buf.append(str.substring(0, idx));
            buf.append(s2);
            str = str.substring(idx + s1.length());
        } while(true);
        return buf.toString();
    }

    private static int getConversion(Class type, TypeMapper mapper)
    {
        if(type == (java.lang.Boolean.class))
            type = Boolean.TYPE;
        else
        if(type == (java.lang.Byte.class))
            type = Byte.TYPE;
        else
        if(type == (java.lang.Short.class))
            type = Short.TYPE;
        else
        if(type == (java.lang.Character.class))
            type = Character.TYPE;
        else
        if(type == (java.lang.Integer.class))
            type = Integer.TYPE;
        else
        if(type == (java.lang.Long.class))
            type = Long.TYPE;
        else
        if(type == (java.lang.Float.class))
            type = Float.TYPE;
        else
        if(type == (java.lang.Double.class))
            type = Double.TYPE;
        else
        if(type == (java.lang.Void.class))
            type = Void.TYPE;
        if(mapper != null && (mapper.getFromNativeConverter(type) != null || mapper.getToNativeConverter(type) != null))
            return 21;
        if((com.sun.jna.Pointer.class).isAssignableFrom(type))
            return 1;
        if((java.lang.String.class) == type)
            return 2;
        if((com.sun.jna.WString.class).isAssignableFrom(type))
            return 18;
        if(Platform.HAS_BUFFERS && Buffers.isBuffer(type))
            return 5;
        if((com.sun.jna.Structure.class).isAssignableFrom(type))
            return !(com.sun.jna.Structure$ByValue.class).isAssignableFrom(type) ? 3 : 4;
        if(type.isArray())
            switch(type.getName().charAt(1))
            {
            case 90: // 'Z'
                return 13;

            case 66: // 'B'
                return 6;

            case 83: // 'S'
                return 7;

            case 67: // 'C'
                return 8;

            case 73: // 'I'
                return 9;

            case 74: // 'J'
                return 10;

            case 70: // 'F'
                return 11;

            case 68: // 'D'
                return 12;
            }
        if(type.isPrimitive())
            return type != Boolean.TYPE ? 0 : 14;
        if((com.sun.jna.Callback.class).isAssignableFrom(type))
            return 15;
        if((com.sun.jna.IntegerType.class).isAssignableFrom(type))
            return 19;
        if((com.sun.jna.PointerType.class).isAssignableFrom(type))
            return 20;
        return !(com.sun.jna.NativeMapped.class).isAssignableFrom(type) ? -1 : 17;
    }

    public static void register(Class cls, NativeLibrary lib)
    {
        Method methods[] = cls.getDeclaredMethods();
        java.util.List mlist = new ArrayList();
        TypeMapper mapper = (TypeMapper)lib.getOptions().get("type-mapper");
        for(int i = 0; i < methods.length; i++)
            if((methods[i].getModifiers() & 0x100) != 0)
                mlist.add(methods[i]);

        long handles[] = new long[mlist.size()];
        for(int i = 0; i < handles.length; i++)
        {
            Method method = (Method)mlist.get(i);
            String sig = "(";
            Class rclass = method.getReturnType();
            Class ptypes[] = method.getParameterTypes();
            long atypes[] = new long[ptypes.length];
            long closure_atypes[] = new long[ptypes.length];
            int cvt[] = new int[ptypes.length];
            ToNativeConverter toNative[] = new ToNativeConverter[ptypes.length];
            FromNativeConverter fromNative = null;
            int rcvt = getConversion(rclass, mapper);
            boolean throwLastError = false;
            long rtype;
            long closure_rtype;
            switch(rcvt)
            {
            case -1: 
                throw new IllegalArgumentException(rclass + " is not a supported return type (in method " + method.getName() + " in " + cls + ")");

            case 21: // '\025'
                fromNative = mapper.getFromNativeConverter(rclass);
                closure_rtype = Structure.FFIType.get(rclass).peer;
                rtype = Structure.FFIType.get(fromNative.nativeType()).peer;
                break;

            case 17: // '\021'
            case 19: // '\023'
            case 20: // '\024'
                closure_rtype = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                rtype = Structure.FFIType.get(NativeMappedConverter.getInstance(rclass).nativeType()).peer;
                break;

            case 3: // '\003'
                closure_rtype = rtype = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                break;

            case 4: // '\004'
                closure_rtype = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                rtype = Structure.FFIType.get(rclass).peer;
                break;

            case 0: // '\0'
            case 1: // '\001'
            case 2: // '\002'
            case 5: // '\005'
            case 6: // '\006'
            case 7: // '\007'
            case 8: // '\b'
            case 9: // '\t'
            case 10: // '\n'
            case 11: // '\013'
            case 12: // '\f'
            case 13: // '\r'
            case 14: // '\016'
            case 15: // '\017'
            case 16: // '\020'
            case 18: // '\022'
            default:
                closure_rtype = rtype = Structure.FFIType.get(rclass).peer;
                break;
            }
            for(int t = 0; t < ptypes.length; t++)
            {
                Class type = ptypes[t];
                sig = sig + getSignature(type);
                cvt[t] = getConversion(type, mapper);
                if(cvt[t] == -1)
                    throw new IllegalArgumentException(type + " is not a supported argument type (in method " + method.getName() + " in " + cls + ")");
                if(cvt[t] == 17 || cvt[t] == 19)
                    type = NativeMappedConverter.getInstance(type).nativeType();
                else
                if(cvt[t] == 21)
                    toNative[t] = mapper.getToNativeConverter(type);
                switch(cvt[t])
                {
                case 4: // '\004'
                case 17: // '\021'
                case 19: // '\023'
                case 20: // '\024'
                    atypes[t] = Structure.FFIType.get(type).peer;
                    closure_atypes[t] = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                    break;

                case 21: // '\025'
                    if(type.isPrimitive())
                        closure_atypes[t] = Structure.FFIType.get(type).peer;
                    else
                        closure_atypes[t] = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                    atypes[t] = Structure.FFIType.get(toNative[t].nativeType()).peer;
                    break;

                case 0: // '\0'
                    closure_atypes[t] = atypes[t] = Structure.FFIType.get(type).peer;
                    break;

                default:
                    closure_atypes[t] = atypes[t] = Structure.FFIType.get(com.sun.jna.Pointer.class).peer;
                    break;
                }
            }

            sig = sig + ")";
            sig = sig + getSignature(rclass);
            Class etypes[] = method.getExceptionTypes();
            int e = 0;
            do
            {
                if(e >= etypes.length)
                    break;
                if((com.sun.jna.LastErrorException.class).isAssignableFrom(etypes[e]))
                {
                    throwLastError = true;
                    break;
                }
                e++;
            } while(true);
            String name = method.getName();
            FunctionMapper fmapper = (FunctionMapper)lib.getOptions().get("function-mapper");
            if(fmapper != null)
                name = fmapper.getFunctionName(lib, method);
            Function f = lib.getFunction(name, method);
            try
            {
                handles[i] = registerMethod(cls, method.getName(), sig, cvt, closure_atypes, atypes, rcvt, closure_rtype, rtype, rclass, f.peer, f.getCallingConvention(), throwLastError, toNative, fromNative);
            }
            catch(NoSuchMethodError e)
            {
                throw new UnsatisfiedLinkError("No method " + method.getName() + " with signature " + sig + " in " + cls);
            }
        }

        synchronized(registeredClasses)
        {
            registeredClasses.put(cls, handles);
            registeredLibraries.put(cls, lib);
        }
        cacheOptions(cls, lib.getOptions(), null);
    }

    private static void cacheOptions(Class cls, Map libOptions, Object proxy)
    {
        synchronized(libraries)
        {
            if(!libOptions.isEmpty())
                options.put(cls, libOptions);
            if(libOptions.containsKey("type-mapper"))
                typeMappers.put(cls, libOptions.get("type-mapper"));
            if(libOptions.containsKey("structure-alignment"))
                alignments.put(cls, libOptions.get("structure-alignment"));
            if(proxy != null)
                libraries.put(cls, new WeakReference(proxy));
            if(!cls.isInterface() && (com.sun.jna.Library.class).isAssignableFrom(cls))
            {
                Class ifaces[] = cls.getInterfaces();
                int i = 0;
                do
                {
                    if(i >= ifaces.length)
                        break;
                    if((com.sun.jna.Library.class).isAssignableFrom(ifaces[i]))
                    {
                        cacheOptions(ifaces[i], libOptions, proxy);
                        break;
                    }
                    i++;
                } while(true);
            }
        }
    }

    private static native long registerMethod(Class class1, String s, String s1, int ai[], long al[], long al1[], int i, long l, long l1, Class class2, long l2, int j, 
            boolean flag, ToNativeConverter atonativeconverter[], FromNativeConverter fromnativeconverter);

    private static NativeMapped fromNative(Class cls, Object value)
    {
        return (NativeMapped)NativeMappedConverter.getInstance(cls).fromNative(value, new FromNativeContext(cls));
    }

    private static Class nativeType(Class cls)
    {
        return NativeMappedConverter.getInstance(cls).nativeType();
    }

    private static Object toNative(ToNativeConverter cvt, Object o)
    {
        return cvt.toNative(o, new ToNativeContext());
    }

    private static Object fromNative(FromNativeConverter cvt, Object o, Class cls)
    {
        return cvt.fromNative(o, new FromNativeContext(cls));
    }

    public static native long ffi_prep_cif(int i, int j, long l, long l1);

    public static native void ffi_call(long l, long l1, long l2, long l3);

    public static native long ffi_prep_closure(long l, ffi_callback ffi_callback1);

    public static native void ffi_free_closure(long l);

    static native int initialize_ffi_type(long l);

    public static void main(String args[])
    {
        String DEFAULT_TITLE = "Java Native Access (JNA)";
        String DEFAULT_VERSION = "3.4.0";
        String DEFAULT_BUILD = "3.4.0 (package information missing)";
        Package pkg = (com.sun.jna.Native.class).getPackage();
        String title = pkg == null ? "Java Native Access (JNA)" : pkg.getSpecificationTitle();
        if(title == null)
            title = "Java Native Access (JNA)";
        String version = pkg == null ? "3.4.0" : pkg.getSpecificationVersion();
        if(version == null)
            version = "3.4.0";
        title = title + " API Version " + version;
        System.out.println(title);
        version = pkg == null ? "3.4.0 (package information missing)" : pkg.getImplementationVersion();
        if(version == null)
            version = "3.4.0 (package information missing)";
        System.out.println("Version: " + version);
        System.out.println(" Native: " + getNativeVersion() + " (" + getAPIChecksum() + ")");
        System.exit(0);
    }

    static synchronized native void freeNativeCallback(long l);

    static synchronized native long createNativeCallback(Callback callback, Method method, Class aclass[], Class class1, int i, boolean flag);

    static native int invokeInt(long l, int i, Object aobj[]);

    static native long invokeLong(long l, int i, Object aobj[]);

    static native void invokeVoid(long l, int i, Object aobj[]);

    static native float invokeFloat(long l, int i, Object aobj[]);

    static native double invokeDouble(long l, int i, Object aobj[]);

    static native long invokePointer(long l, int i, Object aobj[]);

    private static native void invokeStructure(long l, int i, Object aobj[], long l1, long l2);

    static Structure invokeStructure(long fp, int callFlags, Object args[], Structure s)
    {
        invokeStructure(fp, callFlags, args, s.getPointer().peer, s.getTypeInfo().peer);
        return s;
    }

    static native Object invokeObject(long l, int i, Object aobj[]);

    static native long open(String s);

    static native void close(long l);

    static native long findSymbol(long l, String s);

    static native long indexOf(long l, byte byte0);

    static native void read(long l, byte abyte0[], int i, int j);

    static native void read(long l, short aword0[], int i, int j);

    static native void read(long l, char ac[], int i, int j);

    static native void read(long l, int ai[], int i, int j);

    static native void read(long l, long al[], int i, int j);

    static native void read(long l, float af[], int i, int j);

    static native void read(long l, double ad[], int i, int j);

    static native void write(long l, byte abyte0[], int i, int j);

    static native void write(long l, short aword0[], int i, int j);

    static native void write(long l, char ac[], int i, int j);

    static native void write(long l, int ai[], int i, int j);

    static native void write(long l, long al[], int i, int j);

    static native void write(long l, float af[], int i, int j);

    static native void write(long l, double ad[], int i, int j);

    static native byte getByte(long l);

    static native char getChar(long l);

    static native short getShort(long l);

    static native int getInt(long l);

    static native long getLong(long l);

    static native float getFloat(long l);

    static native double getDouble(long l);

    static Pointer getPointer(long addr)
    {
        long peer = _getPointer(addr);
        return peer != 0L ? new Pointer(peer) : null;
    }

    private static native long _getPointer(long l);

    static native String getString(long l, boolean flag);

    static native void setMemory(long l, long l1, byte byte0);

    static native void setByte(long l, byte byte0);

    static native void setShort(long l, short word0);

    static native void setChar(long l, char c);

    static native void setInt(long l, int i);

    static native void setLong(long l, long l1);

    static native void setFloat(long l, float f);

    static native void setDouble(long l, double d);

    static native void setPointer(long l, long l1);

    static native void setString(long l, String s, boolean flag);

    public static native long malloc(long l);

    public static native void free(long l);

    public static native ByteBuffer getDirectByteBuffer(long l, long l1);

    public static void detach(boolean detach)
    {
        setLastError(detach ? -1 : -2);
    }

    private static final String VERSION = "3.4.0";
    private static final String VERSION_NATIVE = "3.4.0";
    private static String nativeLibraryPath = null;
    private static Map typeMappers = new WeakHashMap();
    private static Map alignments = new WeakHashMap();
    private static Map options = new WeakHashMap();
    private static Map libraries = new WeakHashMap();
    private static final Callback.UncaughtExceptionHandler DEFAULT_HANDLER;
    private static Callback.UncaughtExceptionHandler callbackExceptionHandler;
    public static final int POINTER_SIZE = sizeof(0);
    public static final int LONG_SIZE = sizeof(1);
    public static final int WCHAR_SIZE = sizeof(2);
    public static final int SIZE_T_SIZE = sizeof(3);
    private static final int TYPE_VOIDP = 0;
    private static final int TYPE_LONG = 1;
    private static final int TYPE_WCHAR_T = 2;
    private static final int TYPE_SIZE_T = 3;
    private static final int THREAD_NOCHANGE = 0;
    private static final int THREAD_DETACH = -1;
    private static final int THREAD_LEAVE_ATTACHED = -2;
    private static final Object finalizer;
    private static final ThreadLocal lastError;
    private static Map registeredClasses;
    private static Map registeredLibraries;
    private static Object unloader;
    static final int CB_HAS_INITIALIZER = 1;
    private static final int CVT_UNSUPPORTED = -1;
    private static final int CVT_DEFAULT = 0;
    private static final int CVT_POINTER = 1;
    private static final int CVT_STRING = 2;
    private static final int CVT_STRUCTURE = 3;
    private static final int CVT_STRUCTURE_BYVAL = 4;
    private static final int CVT_BUFFER = 5;
    private static final int CVT_ARRAY_BYTE = 6;
    private static final int CVT_ARRAY_SHORT = 7;
    private static final int CVT_ARRAY_CHAR = 8;
    private static final int CVT_ARRAY_INT = 9;
    private static final int CVT_ARRAY_LONG = 10;
    private static final int CVT_ARRAY_FLOAT = 11;
    private static final int CVT_ARRAY_DOUBLE = 12;
    private static final int CVT_ARRAY_BOOLEAN = 13;
    private static final int CVT_BOOLEAN = 14;
    private static final int CVT_CALLBACK = 15;
    private static final int CVT_FLOAT = 16;
    private static final int CVT_NATIVE_MAPPED = 17;
    private static final int CVT_WSTRING = 18;
    private static final int CVT_INTEGER_TYPE = 19;
    private static final int CVT_POINTER_TYPE = 20;
    private static final int CVT_TYPE_MAPPER = 21;
    static Class class$java$lang$ClassLoader; /* synthetic field */
    static Class class$java$nio$Buffer; /* synthetic field */

    static 
    {
        DEFAULT_HANDLER = new Callback.UncaughtExceptionHandler() {

            public void uncaughtException(Callback c, Throwable e)
            {
                System.err.println("JNA: Callback " + c + " threw the following exception:");
                e.printStackTrace();
            }

        }
;
        callbackExceptionHandler = DEFAULT_HANDLER;
        loadNativeLibrary();
        initIDs();
        if(Boolean.getBoolean("jna.protected"))
            setProtected(true);
        String version = getNativeVersion();
        if(!"3.4.0".equals(version))
        {
            String LS = System.getProperty("line.separator");
            throw new Error(LS + LS + "There is an incompatible JNA native library installed on this system." + LS + "To resolve this issue you may do one of the following:" + LS + " - remove or uninstall the offending library" + LS + " - set the system property jna.nosys=true" + LS + " - set jna.boot.library.path to include the path to the version of the " + LS + "   jnidispatch library included with the JNA jar file you are using" + LS);
        } else
        {
            setPreserveLastError("true".equalsIgnoreCase(System.getProperty("jna.preserve_last_error", "true")));
            finalizer = new Object() {

                protected void finalize()
                {
                    Native.dispose();
                }

            }
;
            lastError = new ThreadLocal() {

                protected synchronized Object initialValue()
                {
                    return new Integer(0);
                }

            }
;
            registeredClasses = new HashMap();
            registeredLibraries = new HashMap();
            unloader = new Object() {

                protected void finalize()
                {
                    synchronized(Native.registeredClasses)
                    {
                        for(Iterator i = Native.registeredClasses.entrySet().iterator(); i.hasNext(); i.remove())
                        {
                            java.util.Map.Entry e = (java.util.Map.Entry)i.next();
                            Native.unregister((Class)e.getKey(), (long[])(long[])e.getValue());
                        }

                    }
                }

            }
;
        }
    }



}
