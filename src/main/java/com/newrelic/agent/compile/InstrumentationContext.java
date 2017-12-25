//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.newrelic.agent.compile;

import com.newrelic.agent.compile.ClassData;
import com.newrelic.agent.compile.ClassMethod;
import com.newrelic.agent.compile.ClassRemapperConfig;
import com.newrelic.agent.compile.Log;
import com.newrelic.agent.compile.Shim;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class InstrumentationContext {
    private static final String[] ANDROID_8_MISSING_CLASS_WHITE_LIST = new String[]{"android.view.View$AccessibilityDelegate", "android.view.accessibility.AccessibilityNodeProvider"};
    private static final HashMap<Integer, Set<String>> MISSING_CLASS_WHITE_LIST = new HashMap<Integer, Set<String>>() {
        {
            this.put(8, new HashSet<String>(Arrays.asList(InstrumentationContext.ANDROID_8_MISSING_CLASS_WHITE_LIST)));
        }
    };
    private final WeakHashMap<String, Class<?>> cache = new WeakHashMap<String, Class<?>>();
    private final ClassRemapperConfig config;
    private Shim shim;
    private final Log log;
    private boolean classModified;
    private String className;
    private String superClassName;
    private final ArrayList<String> tags = new ArrayList<String>();
    private HashMap<String, String> tracedMethods;
    private HashMap<String, String> skippedMethods;
    private final HashMap<String, ArrayList<String>> tracedMethodParameters = new HashMap<String, ArrayList<String>>();

    public InstrumentationContext(ClassRemapperConfig config, Log log) {
        this.config = config;
        this.log = log;
        this.tracedMethods = new HashMap<String, String>();
        this.skippedMethods = new HashMap<String, String>();
    }

    public Log getLog() {
        return this.log;
    }

    public void reset() {
        this.classModified = false;
        this.className = null;
        this.superClassName = null;
        this.shim = null;
        this.tags.clear();
    }

    public void markModified() {
        this.classModified = true;
    }

    public boolean isClassModified() {
        return this.classModified;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void addUniqueTag(String tag) {
        while(this.tags.remove(tag)) {
            ;
        }

        this.addTag(tag);
    }

    public void addTracedMethod(String name, String desc) {
        this.log.debug("Will trace method [" + this.className + "#" + name + ":" + desc + "] as requested");
        this.tracedMethods.put(this.className + "#" + name, desc);
    }

    public void addSkippedMethod(String name, String desc) {
        this.log.debug("Will skip all tracing in method [" + this.className + "#" + name + ":" + desc + "] as requested");
        this.skippedMethods.put(this.className + "#" + name, desc);
    }

    public void addTracedMethodParameter(String methodName, String parameterName, String parameterClass, String parameterValue) {
        this.log.debug("Adding traced method parameter [" + parameterName + "] for method [" + methodName + "]");
        String name = this.className + "#" + methodName;
        if(!this.tracedMethodParameters.containsKey(name)) {
            this.tracedMethodParameters.put(name, new ArrayList<String>());
        }

        ArrayList<String> methodParameters = this.tracedMethodParameters.get(name);
        methodParameters.add(parameterName);
        methodParameters.add(parameterClass);
        methodParameters.add(parameterValue);
    }

    public ArrayList<String> getTracedMethodParameters(String methodName) {
        return this.tracedMethodParameters.get(this.className + "#" + methodName);
    }

    public boolean isTracedMethod(String name, String desc) {
        return this.searchMethodMap(this.tracedMethods, name, desc);
    }

    public boolean isSkippedMethod(String name, String desc) {
        return this.searchMethodMap(this.skippedMethods, name, desc);
    }

    private boolean searchMethodMap(Map<String, String> map, String name, String desc) {
        String descToMatch = map.get(this.className + "#" + name);
        return descToMatch != null && desc.equals(desc);
    }

    public List<String> getTags() {
        return this.tags;
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public String getFriendlyClassName() {
        return this.className.replaceAll("/", ".");
    }

    public String getFriendlySuperClassName() {
        return this.superClassName.replaceAll("/", ".");
    }

    public String getSimpleClassName() {
        return this.className.contains("/")?this.className.substring(this.className.lastIndexOf("/") + 1):this.className;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public String getSuperClassName() {
        return this.superClassName;
    }

    public void setShim(Shim shim) {
        this.shim = shim;
    }

    public Shim getShim() {
        return this.shim;
    }

    public ClassData newClassData(byte[] mainClassBytes) {
        return this.shim != null?new ClassData(mainClassBytes, this.shim.getClassName(), this.shim.getBytes(), this.isClassModified()):new ClassData(mainClassBytes, this.isClassModified());
    }

    public ClassMethod getMethodWrapper(ClassMethod method) {
        return this.config.getMethodWrapper(method);
    }

    public Collection<ClassMethod> getCallSiteReplacements(String className, String methodName, String methodDesc) {
        return this.config.getCallSiteReplacements(className, methodName, methodDesc);
    }
}
