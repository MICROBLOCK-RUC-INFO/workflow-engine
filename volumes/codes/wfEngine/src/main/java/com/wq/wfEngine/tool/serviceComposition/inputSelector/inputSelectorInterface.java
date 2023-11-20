package com.wq.wfEngine.tool.serviceComposition.inputSelector;

import java.util.Map;

public interface inputSelectorInterface<T, E> {
    public static final String needIterate="$$$$$";
    public E select(T root,String sentence);
}
